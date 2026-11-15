function toDate(value) {
  if (!value) {
    return null;
  }
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }
  const normalized = typeof value === "string" && value.length === 10 ? `${value}T00:00:00` : value;
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
}

function safeText(...values) {
  return values
    .filter((item) => item !== null && item !== undefined && item !== "")
    .join(" ")
    .toLowerCase();
}

function round(value) {
  return Math.round(Number(value || 0));
}

function clamp(value, min = 0, max = 100) {
  return Math.min(max, Math.max(min, round(value)));
}

export function deriveMedicationCategory(record) {
  const text = safeText(
    record?.planTitle,
    record?.planContent,
    record?.targetGroup,
    record?.feedback,
    record?.doctorAdvice,
    record?.warningType
  );

  const keywordGroups = [
    { label: "降压类用药", keywords: ["高血压", "降压", "测压", "血压"] },
    { label: "降糖类用药", keywords: ["糖尿病", "降糖", "血糖", "胰岛素"] },
    { label: "心脑血管用药", keywords: ["冠心病", "心脏", "脑卒中", "心血管"] },
    { label: "呼吸系统用药", keywords: ["呼吸", "慢阻肺", "吸入", "肺"] },
    { label: "骨科康复用药", keywords: ["骨科", "术后", "止痛", "关节"] },
  ];

  const matched = keywordGroups.find((item) => item.keywords.some((keyword) => text.includes(keyword.toLowerCase())));
  return matched?.label || "常规用药";
}

export function deriveReminderPeriod(record) {
  const reminderAt = toDate(record?.lastReminderTime || record?.createTime || record?.recordDate);
  if (!reminderAt) {
    return "未设置时段";
  }
  const hour = reminderAt.getHours();
  if (hour >= 5 && hour < 11) {
    return "晨间提醒";
  }
  if (hour >= 11 && hour < 14) {
    return "午间提醒";
  }
  if (hour >= 14 && hour < 18) {
    return "下午提醒";
  }
  if (hour >= 18 && hour < 22) {
    return "晚间提醒";
  }
  return "夜间提醒";
}

export function groupMedicationRecords(records = []) {
  const groups = new Map();
  records
    .filter((item) => item?.moduleType === "medication")
    .forEach((item) => {
      const category = deriveMedicationCategory(item);
      const period = deriveReminderPeriod(item);
      const key = `${category}|${period}`;
      const reminderAt = toDate(item.lastReminderTime || item.createTime || item.recordDate);
      if (!groups.has(key)) {
        groups.set(key, {
          key,
          category,
          period,
          count: 0,
          pendingCount: 0,
          feedbackCount: 0,
          completedCount: 0,
          latestReminderTime: reminderAt,
          records: [],
        });
      }
      const current = groups.get(key);
      current.count += 1;
      current.records.push(item);
      if (!current.latestReminderTime || (reminderAt && reminderAt > current.latestReminderTime)) {
        current.latestReminderTime = reminderAt;
      }
      if (item.status === "2") {
        current.completedCount += 1;
      } else if (item.medicationResult) {
        current.feedbackCount += 1;
      } else {
        current.pendingCount += 1;
      }
    });

  return Array.from(groups.values()).sort((a, b) => {
    if (b.pendingCount !== a.pendingCount) {
      return b.pendingCount - a.pendingCount;
    }
    return (b.latestReminderTime?.getTime() || 0) - (a.latestReminderTime?.getTime() || 0);
  });
}

export function calculateRehabStats(plans = [], records = [], healthDataList = []) {
  const rehabPlans = plans.filter((item) => item?.moduleType === "rehab");
  const rehabRecords = records.filter((item) => item?.moduleType === "rehab");
  const rehabUploads = healthDataList.filter((item) => item?.rehabPhoto);

  let totalTasks = rehabRecords.length;
  let completedTasks = rehabRecords.filter((item) => item?.status === "2" || item?.rehabPhoto || item?.feedback).length;

  if (!totalTasks && rehabUploads.length) {
    totalTasks = rehabUploads.length;
    completedTasks = rehabUploads.length;
  }
  if (!totalTasks && rehabPlans.length) {
    totalTasks = rehabPlans.length;
  }

  const progressPercent = totalTasks ? clamp((completedTasks / totalTasks) * 100) : 0;
  const history = rehabRecords
    .slice()
    .sort((a, b) => (toDate(b.recordDate || b.createTime)?.getTime() || 0) - (toDate(a.recordDate || a.createTime)?.getTime() || 0))
    .slice(0, 10);

  return {
    totalTasks,
    completedTasks,
    activePlans: rehabPlans.filter((item) => item?.status === "0").length,
    progressPercent,
    uploadCount: rehabUploads.length,
    history,
  };
}

function calculateMetricPenalty(latest) {
  if (!latest) {
    return 0;
  }
  let penalty = 0;
  if (latest.temperature && (latest.temperature < 36 || latest.temperature > 37.5)) {
    penalty += 8;
  }
  if (latest.bloodPressureSys && latest.bloodPressureDia) {
    if (latest.bloodPressureSys > 160 || latest.bloodPressureDia > 100) {
      penalty += 18;
    } else if (latest.bloodPressureSys > 140 || latest.bloodPressureDia > 90) {
      penalty += 10;
    }
  }
  if (latest.heartRate && (latest.heartRate < 55 || latest.heartRate > 110)) {
    penalty += 8;
  }
  if (latest.bloodSugar && latest.bloodSugar > 7.8) {
    penalty += 12;
  }
  return penalty;
}

function calculateStabilityScore(latest, warnings) {
  const unreadWarnings = warnings.filter((item) => item?.isRead === "0").length;
  const warningPenalty = warnings.length * 4 + unreadWarnings * 6;
  const metricPenalty = calculateMetricPenalty(latest);
  return clamp(100 - warningPenalty - metricPenalty);
}

export function calculatePatientPortrait({ archive, records = [], plans = [], warnings = [], healthData = [] }) {
  const medicationRecords = records.filter((item) => item?.moduleType === "medication");
  const takenCount = medicationRecords.filter((item) => item?.medicationResult === "taken").length;
  const delayedCount = medicationRecords.filter((item) => item?.medicationResult === "delayed").length;
  const rehabStats = calculateRehabStats(plans, records, healthData);
  const followupResponded = records.filter((item) => item?.status && item.status !== "0").length;
  const latestData = healthData
    .slice()
    .sort((a, b) => (toDate(b.measureTime)?.getTime() || 0) - (toDate(a.measureTime)?.getTime() || 0))[0];

  const medicationScore = medicationRecords.length
    ? clamp(((takenCount + delayedCount * 0.7) / medicationRecords.length) * 100)
    : 85;
  const followupScore = records.length ? clamp((followupResponded / records.length) * 100) : 85;
  const rehabScore = rehabStats.totalTasks ? rehabStats.progressPercent : 80;
  const stabilityScore = calculateStabilityScore(latestData, warnings);

  let riskScore = 88;
  if (archive?.riskLevel === "高风险") {
    riskScore = 52;
  } else if (archive?.riskLevel === "中风险") {
    riskScore = 72;
  } else if (archive?.riskLevel === "重点随访") {
    riskScore = 64;
  }
  riskScore = clamp(riskScore - warnings.filter((item) => item?.warningLevel === "3").length * 8);

  const dimensions = [
    { label: "用药依从", value: medicationScore },
    { label: "康复执行", value: rehabScore },
    { label: "指标稳定", value: stabilityScore },
    { label: "风险控制", value: riskScore },
    { label: "随访反馈", value: followupScore },
  ];

  const overallScore = clamp(dimensions.reduce((sum, item) => sum + item.value, 0) / dimensions.length);
  let levelText = "稳定";
  let levelType = "success";
  if (overallScore < 65) {
    levelText = "重点干预";
    levelType = "danger";
  } else if (overallScore < 85) {
    levelText = "持续关注";
    levelType = "warning";
  }

  const summaryTags = [
    archive?.diseaseType || "未标注病种",
    archive?.riskLevel || "未评估风险",
    warnings.length ? `${warnings.length} 条预警` : "近期无预警",
    rehabStats.totalTasks ? `康复 ${rehabStats.progressPercent}%` : "暂无康复任务",
  ];

  return {
    overallScore,
    levelText,
    levelType,
    dimensions,
    summaryTags,
  };
}

export function buildBodyChangeNotes(healthData = [], warnings = [], archive = null) {
  const sorted = healthData
    .slice()
    .sort((a, b) => (toDate(a.measureTime)?.getTime() || 0) - (toDate(b.measureTime)?.getTime() || 0));
  const latest = sorted[sorted.length - 1];
  const first = sorted[0];
  const notes = [];

  if (archive?.riskLevel) {
    notes.push(`当前风险等级：${archive.riskLevel}`);
  }
  if (latest?.bloodPressureSys && latest?.bloodPressureDia) {
    notes.push(`最近血压 ${latest.bloodPressureSys}/${latest.bloodPressureDia} mmHg`);
  }
  if (latest?.bloodSugar) {
    notes.push(`最近血糖 ${latest.bloodSugar} mmol/L`);
  }
  if (first?.weight && latest?.weight && latest.weight !== first.weight) {
    const diff = (latest.weight - first.weight).toFixed(1);
    notes.push(`体重较初次记录变化 ${diff > 0 ? "+" : ""}${diff} kg`);
  }
  if (warnings.length) {
    const latestWarning = warnings
      .slice()
      .sort((a, b) => (toDate(b.createTime)?.getTime() || 0) - (toDate(a.createTime)?.getTime() || 0))[0];
    if (latestWarning?.warningMsg) {
      notes.push(`最新预警：${latestWarning.warningMsg}`);
    }
  }

  return notes.slice(0, 5);
}

export function summarizeFollowupProgress(records = []) {
  const total = records.length;
  const pending = records.filter((item) => item?.status === "0").length;
  const feedback = records.filter((item) => item?.status === "1").length;
  const completed = records.filter((item) => item?.status === "2").length;
  const completionPercent = total ? clamp(((feedback + completed) / total) * 100) : 0;
  return {
    total,
    pending,
    feedback,
    completed,
    completionPercent,
  };
}

export function sortByDateDesc(list = [], fieldNames = []) {
  return list.slice().sort((a, b) => {
    const left = fieldNames.map((field) => toDate(a?.[field])).find(Boolean);
    const right = fieldNames.map((field) => toDate(b?.[field])).find(Boolean);
    return (right?.getTime() || 0) - (left?.getTime() || 0);
  });
}

export function formatMedicationResult(value) {
  const labels = {
    taken: "已服药",
    delayed: "延后服药",
    missed: "未服药",
  };
  return labels[value] || "未反馈";
}

