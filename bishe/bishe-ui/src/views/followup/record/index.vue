<template>
  <div class="app-container record-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="医生姓名" prop="doctorName" v-if="!isDoctor">
        <el-input
          v-model="queryParams.doctorName"
          placeholder="请输入医生姓名"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="患者姓名" prop="patientName" v-if="!isPatient">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 180px">
          <el-option v-for="dict in followup_record_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div v-if="medicationGroups.length" class="medication-board">
      <section class="board-hero">
        <div class="hero-content">
          <div class="hero-header">
            <el-icon class="hero-icon"><Monitor /></el-icon>
            <span class="eyebrow">MEDICATION MONITORING</span>
          </div>
          <h2>用药提醒分类看板</h2>
          <p>智能分类与动态跟踪，保障用药精准闭环。</p>
        </div>
        <div class="board-kpis">
          <div class="kpi-card blue">
            <div class="kpi-icon"><el-icon><Files /></el-icon></div>
            <div class="kpi-body">
              <strong>{{ medicationSummary.total }}</strong>
              <span>提醒总数</span>
            </div>
          </div>
          <div class="kpi-card orange">
            <div class="kpi-icon"><el-icon><Clock /></el-icon></div>
            <div class="kpi-body">
              <strong>{{ medicationSummary.pending }}</strong>
              <span>待处理</span>
            </div>
          </div>
          <div class="kpi-card green">
            <div class="kpi-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="kpi-body">
              <strong>{{ medicationSummary.responded }}</strong>
              <span>已完成</span>
            </div>
          </div>
        </div>
      </section>

      <el-row :gutter="20" class="board-grid">
        <el-col :xs="24" :sm="12" :lg="8" v-for="group in medicationGroups" :key="group.key">
          <el-card shadow="hover" class="group-card-v2">
            <template #header>
              <div class="card-header">
                <div class="header-main">
                  <el-icon class="category-icon"><FolderOpened /></el-icon>
                  <span class="category-title">{{ group.category }}</span>
                </div>
                <el-tag :type="group.pendingCount > 0 ? 'danger' : 'success'" effect="dark" round size="small">
                  {{ group.count }} 条
                </el-tag>
              </div>
            </template>
            
            <div class="card-content">
              <div class="info-row">
                <el-tag size="small" :type="getPeriodType(group.period)" effect="plain">
                  <el-icon><component :is="getPeriodIcon(group.period)" /></el-icon>
                  {{ group.period }}
                </el-tag>
              </div>

              <div class="stats-grid">
                <div class="stat-box">
                  <div class="stat-label">待反馈</div>
                  <div class="stat-value danger">{{ group.pendingCount }}</div>
                </div>
                <div class="stat-box">
                  <div class="stat-label">已反馈</div>
                  <div class="stat-value primary">{{ group.feedbackCount }}</div>
                </div>
                <div class="stat-box">
                  <div class="stat-label">已完成</div>
                  <div class="stat-value success">{{ group.completedCount }}</div>
                </div>
              </div>

              <el-progress 
                :percentage="getProgressValue(group)" 
                :color="getProgressColor(group)"
                :stroke-width="8"
                :show-text="false"
                class="card-progress"
              />
               
              <div class="card-footer-v2">
                <el-icon><Clock /></el-icon>
                <span class="time-text">{{ group.latestReminderTime ? parseTime(group.latestReminderTime, '{m}-{d} {h}:{i}') : '暂无记录' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="history-section">
        <div class="section-header">
          <div class="title-with-icon">
            <div class="icon-box"><el-icon><History /></el-icon></div>
            <span>用药提醒历史</span>
          </div>
          <el-button link type="primary">查看更多</el-button>
        </div>
        <el-table :data="medicationHistory" size="default" class="custom-table shadow-sm">
          <el-table-column label="计划标题" prop="planTitle" min-width="180">
            <template #default="scope">
              <span class="bold-text">{{ scope.row.planTitle }}</span>
            </template>
          </el-table-column>
          <el-table-column label="药物类别" min-width="120">
            <template #default="scope">
              <el-tag effect="light" round size="small">{{ deriveMedicationCategory(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提醒时段" min-width="120">
            <template #default="scope">
              <span class="period-text">{{ deriveReminderPeriod(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="服药结果" min-width="120">
            <template #default="scope">
              <el-tag :type="getResultTagType(scope.row.medicationResult)" size="small">
                {{ formatMedicationResult(scope.row.medicationResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提醒次数" prop="reminderCount" width="100" align="center">
            <template #default="scope">
              <span class="count-num">{{ scope.row.reminderCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最近提醒" min-width="160" align="center">
            <template #default="scope">
              <span class="time-text">{{ scope.row.lastReminderTime ? parseTime(scope.row.lastReminderTime, '{y}-{m}-{d} {h}:{i}') : '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-row :gutter="10" class="mb16" v-if="!isPatient">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-if="isDoctor || $auth.hasPermi('followup:record:add')">
          新增记录
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-if="isDoctor || $auth.hasPermi('followup:record:edit')">
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isDoctor">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-if="$auth.hasPermi('followup:record:remove')">
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isDoctor">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-if="$auth.hasPermi('followup:record:export')">
          导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="!isPatient" />
      <el-table-column label="所属计划" prop="planTitle" min-width="190">
        <template #default="scope">
          <div>{{ scope.row.planTitle || "-" }}</div>
          <div class="sub-text">{{ formatModuleType(scope.row.moduleType) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="医生" prop="doctorName" width="110" />
      <el-table-column label="患者" prop="patientName" width="110" />
      <el-table-column label="服药结果" prop="medicationResult" width="120">
        <template #default="scope">
          {{ formatMedicationResult(scope.row.medicationResult) }}
        </template>
      </el-table-column>
      <el-table-column label="提醒次数" prop="reminderCount" width="100" align="center">
        <template #default="scope">
          {{ scope.row.reminderCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="最近提醒" prop="lastReminderTime" width="160" align="center">
        <template #default="scope">
          <span>{{ scope.row.lastReminderTime ? parseTime(scope.row.lastReminderTime, "{y}-{m}-{d} {h}:{i}") : "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="患者反馈" prop="feedback" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="医生建议" prop="doctorAdvice" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="康复拍照" prop="rehabPhoto" width="100" align="center">
        <template #default="scope">
          <image-preview :src="scope.row.rehabPhoto" :width="48" :height="48" />
        </template>
      </el-table-column>
      <el-table-column label="随访日期" prop="recordDate" width="120" align="center">
        <template #default="scope">
          <span>{{ parseTime(scope.row.recordDate, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <dict-tag :options="followup_record_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">
            {{ isPatient ? "反馈" : isDoctor ? "填写" : "编辑" }}
          </el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-if="!isPatient && !isDoctor && $auth.hasPermi('followup:record:remove')"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="110px">
        <el-alert
          v-if="selectedPlan"
          type="success"
          :closable="false"
          show-icon
          class="mb16"
          :title="`${selectedPlan.planTitle || '随访计划'} / ${formatModuleType(selectedPlan.moduleType)} / 目标分组 ${selectedPlan.targetGroup || '未设置'}`"
        />
        <el-alert
          v-if="showMedicationRuleTip"
          type="info"
          :closable="false"
          show-icon
          class="mb16"
          title="用药提醒记录在患者未反馈服药结果时，会按 2 小时一次自动补发提醒，最多 3 次。"
        />

        <el-form-item label="随访计划" prop="planId" v-if="!isPatient">
          <el-select v-model="form.planId" placeholder="请选择随访计划" style="width: 100%" @change="handlePlanChange">
            <el-option v-for="item in planOptions" :key="item.planId" :label="formatPlanTitle(item)" :value="item.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属计划" v-else>
          <el-input :model-value="selectedPlan?.planTitle || form.planTitle || '未绑定计划'" disabled />
        </el-form-item>
        <el-form-item label="责任医生" v-if="selectedPlan?.doctorName || form.doctorName">
          <el-input :model-value="selectedPlan?.doctorName || form.doctorName" disabled />
        </el-form-item>
        <el-form-item label="患者姓名" v-if="selectedPlan?.patientName || form.patientName">
          <el-input :model-value="selectedPlan?.patientName || form.patientName" disabled />
        </el-form-item>

        <el-form-item label="服药结果" prop="medicationResult" v-if="allowMedicationFeedback">
          <el-radio-group v-model="form.medicationResult" :disabled="!isPatient && !isDoctor">
            <el-radio v-for="item in medicationResults" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服药结果" v-else>
          <el-input model-value="-" disabled />
        </el-form-item>

        <el-form-item label="患者反馈" prop="feedback" v-if="showFeedbackField">
          <el-input
            v-model="form.feedback"
            type="textarea"
            :rows="3"
            placeholder="请输入患者反馈，如症状、执行情况、未服药原因"
            :disabled="isDoctor && !isPatient"
          />
        </el-form-item>
        <el-form-item label="医生建议" prop="doctorAdvice">
          <el-input
            v-model="form.doctorAdvice"
            type="textarea"
            :rows="3"
            placeholder="请输入医生建议或到院提醒"
            :disabled="isPatient"
          />
        </el-form-item>
        <el-form-item label="康复拍照" prop="rehabPhoto" v-if="showPhotoField">
          <image-upload v-model="form.rehabPhoto" />
        </el-form-item>
        <el-form-item label="随访日期" prop="recordDate" v-if="!isPatient">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择随访日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status" :disabled="isPatient">
            <el-radio v-for="dict in followup_record_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Record">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs } from "vue";
import { listPlan } from "@/api/followup/plan";
import { addRecord, delRecord, getRecord, listRecord, updateRecord } from "@/api/followup/record";
import useUserStore from "@/store/modules/user";
import {
  deriveMedicationCategory,
  deriveReminderPeriod,
  formatMedicationResult,
  groupMedicationRecords,
  sortByDateDesc,
} from "@/utils/care-overview";

const { proxy } = getCurrentInstance();
const { followup_record_status } = proxy.useDict("followup_record_status");
const userStore = useUserStore();

const medicationResults = [
  { label: "已服药", value: "taken" },
  { label: "延后服药", value: "delayed" },
  { label: "未服药", value: "missed" },
];

const isPatient = computed(() => !userStore.roles.includes("admin") && !userStore.roles.includes("doctor"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));

const recordList = ref([]);
const planOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const showAllMedicationHistory = ref(false);

const medicationGroups = computed(() => groupMedicationRecords(recordList.value));
const medicationHistorySource = computed(() => sortByDateDesc(
  recordList.value.filter((item) => item.moduleType === "medication"),
  ["lastReminderTime", "createTime", "recordDate"]
));
const medicationHistory = computed(() => {
  if (showAllMedicationHistory.value) {
    return medicationHistorySource.value;
  }
  return medicationHistorySource.value.slice(0, 12);
});
const medicationSummary = computed(() => {
  const medicationRecords = recordList.value.filter((item) => item.moduleType === "medication");
  return {
    total: medicationRecords.length,
    pending: medicationRecords.filter((item) => !item.medicationResult && item.status !== "2").length,
    responded: medicationRecords.filter((item) => item.medicationResult || item.status === "2").length,
  };
});

function getPeriodIcon(period) {
  const map = {
    "晨间提醒": "Sunny",
    "午间提醒": "Sunrise",
    "下午提醒": "Sun",
    "晚间提醒": "Moon",
    "夜间提醒": "MoonNight",
  };
  return map[period] || "Bell";
}

function getPeriodType(period) {
  const map = {
    "晨间提醒": "warning",
    "午间提醒": "primary",
    "下午提醒": "success",
    "晚间提醒": "info",
    "夜间提醒": "danger",
  };
  return map[period] || "primary";
}

function getProgressValue(group) {
  if (!group.count) return 0;
  return Math.round(((group.feedbackCount + group.completedCount) / group.count) * 100);
}

function getProgressColor(group) {
  const val = getProgressValue(group);
  if (val === 100) return "#67C23A";
  if (group.pendingCount > 0) return "#F56C6C";
  return "#409EFF";
}

function getProgressWidth(group) {
  if (!group.count) return "0%";
  return `${((group.feedbackCount + group.completedCount) / group.count) * 100}%`;
}

function getResultTagType(result) {
  const map = {
    taken: "success",
    delayed: "warning",
    missed: "danger",
  };
  return map[result] || "info";
}

function syncMedicationHistoryButtonText() {
  const button = document.querySelector(".history-section .el-button");
  if (!button) {
    return;
  }
  button.textContent = showAllMedicationHistory.value ? "收起" : "查看更多";
}

function handleMedicationHistoryButtonClick(event) {
  const button = event.target.closest(".history-section .el-button");
  if (!button) {
    return;
  }
  event.preventDefault();
  showAllMedicationHistory.value = !showAllMedicationHistory.value;
  syncMedicationHistoryButtonText();
}

const createForm = () => ({
  recordId: null,
  planId: null,
  patientId: null,
  doctorId: isDoctor.value ? userStore.id : null,
  planTitle: "",
  patientName: "",
  doctorName: "",
  moduleType: "",
  requireFeedback: "N",
  requirePhoto: "N",
  feedback: "",
  doctorAdvice: "",
  medicationResult: "",
  rehabPhoto: "",
  reminderCount: 0,
  lastReminderTime: "",
  recordDate: "",
  status: "0",
});

const data = reactive({
  form: createForm(),
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    doctorName: "",
    patientName: "",
    status: "",
  },
  rules: {
    planId: [{ required: true, message: "请选择随访计划", trigger: "change" }],
    recordDate: [{ required: true, message: "请选择随访日期", trigger: "change" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

const selectedPlan = computed(() => {
  if (!form.value.planId) {
    return null;
  }
  return planOptions.value.find((item) => item.planId === form.value.planId) || null;
});

const allowMedicationFeedback = computed(() => {
  return selectedPlan.value?.moduleType === "medication" || selectedPlan.value?.requireFeedback === "Y" || form.value.requireFeedback === "Y";
});

const showMedicationRuleTip = computed(() => {
  return selectedPlan.value?.moduleType === "medication" || form.value.moduleType === "medication";
});

const showFeedbackField = computed(() => {
  return selectedPlan.value?.requireFeedback === "Y" || form.value.requireFeedback === "Y";
});

const showPhotoField = computed(() => {
  return selectedPlan.value?.requirePhoto === "Y" || form.value.requirePhoto === "Y";
});

function formatPlanTitle(plan) {
  if (!plan) {
    return "";
  }
  return `${plan.planTitle || "未命名计划"} / ${plan.patientName || "-"}`;
}

function formatModuleType(value) {
  const map = {
    medication: "用药提醒",
    measurement: "指标测量",
    rehab: "康复训练",
    revisit: "复诊通知",
  };
  return map[value] || value || "未设置";
}

function getPlanList() {
  return listPlan({ pageNum: 1, pageSize: 200 }).then((response) => {
    planOptions.value = response.rows || [];
  });
}

function fillPlanData(plan) {
  if (!plan) {
    return;
  }
  form.value.patientId = plan.patientId;
  form.value.doctorId = plan.doctorId;
  form.value.patientName = plan.patientName;
  form.value.doctorName = plan.doctorName;
  form.value.planTitle = plan.planTitle;
  form.value.moduleType = plan.moduleType;
  form.value.requireFeedback = plan.requireFeedback || "N";
  form.value.requirePhoto = plan.requirePhoto || "N";
  if (!allowMedicationFeedback.value) {
    form.value.medicationResult = "";
  }
  if (!showFeedbackField.value) {
    form.value.feedback = "";
  }
  if (!showPhotoField.value) {
    form.value.rehabPhoto = "";
  }
}

function handlePlanChange(planId) {
  const plan = planOptions.value.find((item) => item.planId === planId);
  fillPlanData(plan);
}

function getList() {
  loading.value = true;
  listRecord(queryParams.value)
    .then((response) => {
      recordList.value = response.rows || [];
      total.value = response.total || 0;
      if (recordList.value.filter((item) => item.moduleType === "medication").length <= 12) {
        showAllMedicationHistory.value = false;
      }
    })
    .finally(() => {
      loading.value = false;
      nextTick(() => {
        syncMedicationHistoryButtonText();
      });
    });
}

function reset() {
  form.value = createForm();
  proxy.resetForm("recordRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.recordId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  getPlanList().then(() => {
    open.value = true;
    title.value = "新增随访记录";
  });
}

function handleUpdate(row) {
  reset();
  const recordId = row?.recordId || ids.value[0];
  if (!recordId) {
    proxy.$modal.msgWarning("请选择一条记录");
    return;
  }
  Promise.all([getRecord(recordId), getPlanList()]).then(([recordResponse]) => {
    form.value = { ...createForm(), ...recordResponse.data };
    const matchedPlan = planOptions.value.find((item) => item.planId === form.value.planId);
    if (matchedPlan) {
      fillPlanData(matchedPlan);
      form.value = { ...form.value, ...recordResponse.data };
    }
    open.value = true;
    title.value = isPatient.value ? "反馈提醒结果" : "编辑随访记录";
  });
}

function submitForm() {
  proxy.$refs.recordRef.validate((valid) => {
    if (!valid) {
      return;
    }
    if (!allowMedicationFeedback.value) {
      form.value.medicationResult = "";
    }
    if (!showFeedbackField.value) {
      form.value.feedback = "";
    }
    if (!showPhotoField.value) {
      form.value.rehabPhoto = "";
    }
    if (isPatient.value) {
      form.value.status = "1";
    }
    const request = form.value.recordId ? updateRecord(form.value) : addRecord(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.recordId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const recordIds = row?.recordId ? [row.recordId] : ids.value;
  if (!recordIds.length) {
    proxy.$modal.msgWarning("请选择要删除的记录");
    return;
  }
  proxy.$modal
    .confirm(`是否确认删除随访记录编号为“${recordIds.join(",")}”的数据项？`)
    .then(() => delRecord(recordIds.join(",")))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

function handleExport() {
  proxy.download("followup/record/export", { ...queryParams.value }, `followup_record_${new Date().getTime()}.xlsx`);
}

onMounted(() => {
  document.addEventListener("click", handleMedicationHistoryButtonClick);
  syncMedicationHistoryButtonText();
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleMedicationHistoryButtonClick);
});

getPlanList();
getList();
</script>

<style scoped lang="scss">
.record-page {
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.14), transparent 24%),
    radial-gradient(circle at top right, rgba(5, 150, 105, 0.12), transparent 20%);
}

.mb16 {
  margin-bottom: 16px;
}

.medication-board {
  margin-bottom: 24px;
}

.board-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
  padding: 30px;
  border-radius: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
  border: 1px solid #e0f2fe;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}

.hero-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.hero-icon {
  font-size: 20px;
  color: #0ea5e9;
}

.eyebrow {
  font-size: 12px;
  font-weight: 800;
  color: #0ea5e9;
  letter-spacing: 1px;
}

.board-hero h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.board-hero p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.board-kpis {
  display: flex;
  gap: 16px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 16px;
  min-width: 180px;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1);
}

.kpi-card.blue { background: #eff6ff; border: 1px solid #dbeafe; }
.kpi-card.orange { background: #fff7ed; border: 1px solid #ffedd5; }
.kpi-card.green { background: #f0fdf4; border: 1px solid #dcfce7; }

.kpi-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 24px;
}

.blue .kpi-icon { background: #3b82f6; color: #fff; }
.orange .kpi-icon { background: #f97316; color: #fff; }
.green .kpi-icon { background: #22c55e; color: #fff; }

.kpi-body strong {
  display: block;
  font-size: 24px;
  line-height: 1;
  color: #1e293b;
  margin-bottom: 4px;
}

.kpi-body span {
  font-size: 13px;
  color: #64748b;
}

.medication-board {
  margin-bottom: 24px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.group-card-v2 {
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-card__header) {
    padding: 14px 18px;
    background: #fcfdfe;
    border-bottom: 1px solid #f0f2f5;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header-main {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    color: #1e293b;
    font-size: 16px;
  }
  
  .category-icon {
    color: #409EFF;
    font-size: 18px;
  }
  
  .info-row {
    margin-bottom: 15px;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin: 15px 0;
    text-align: center;
  }
  
  .stat-box {
    padding: 10px 4px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #f1f5f9;
  }
  
  .stat-label {
    font-size: 11px;
    color: #94a3b8;
    margin-bottom: 4px;
    font-weight: 500;
  }
  
  .stat-value {
    font-size: 18px;
    font-weight: 700;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
    
    &.danger { color: #F56C6C; }
    &.primary { color: #409EFF; }
    &.success { color: #67C23A; }
  }
  
  .card-progress {
    margin: 18px 0;
  }
  
  .card-footer-v2 {
    display: flex;
    align-items: center;
    gap: 6px;
    padding-top: 12px;
    border-top: 1px solid #f1f5f9;
    font-size: 12px;
    color: #94a3b8;
  }
}

.history-section {
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-with-icon {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.icon-box {
  width: 36px;
  height: 36px;
  background: #f0f9ff;
  color: #0ea5e9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.bold-text {
  font-weight: 600;
  color: #334155;
}

.period-text {
  font-size: 13px;
  color: #64748b;
}

.count-num {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
  color: #0ea5e9;
}

.time-text {
  font-size: 13px;
  color: #94a3b8;
}

.custom-table {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
}

.shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

@media (max-width: 1200px) {
  .board-hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .board-kpis {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 10px;
  }
}

.sub-text {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 992px) {
  .board-hero {
    flex-direction: column;
  }

  .board-kpis {
    min-width: 0;
  }
}
</style>
