<template>
  <div class="app-container care-overview-page">
    <section class="overview-hero">
      <div>
        <span class="eyebrow">{{ isPatient ? "Patient View" : "Doctor View" }}</span>
        <h2>{{ selectedPatientLabel }}状态总览</h2>
        <p>整合健康趋势、用药提醒、康复训练与随访变化。医生端除患者端可见内容外，额外展示状态画像与风险转译。</p>
      </div>
      <div class="hero-score">
        <el-progress type="dashboard" :percentage="portrait.overallScore" :color="scoreColor" :stroke-width="10">
          <template #default>
            <strong>{{ portrait.overallScore }}</strong>
            <span>{{ portrait.levelText }}</span>
          </template>
        </el-progress>
      </div>
    </section>

    <div v-if="isPatient" class="patient-doctor-card panel-card mb16">
      <div class="panel-title">我的主治医生</div>
      <div v-if="myDoctor">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="医生姓名">{{ myDoctor.nickName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ myDoctor.phonenumber || "未填写" }}</el-descriptions-item>
          <el-descriptions-item label="关联时间">
            {{ (myDoctor.params && myDoctor.params.associationTime) ? myDoctor.params.associationTime : "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="简介">{{ myDoctor.remark || "暂无简介" }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt16 text-right">
          <el-button type="warning" plain icon="Refresh" @click="showDoctorSelector = !showDoctorSelector">更换医生</el-button>
        </div>
      </div>
      <el-empty v-else description="您还没有关联主治医生">
        <el-button type="primary" icon="Search" @click="showDoctorSelector = true">去选择医生</el-button>
      </el-empty>
    </div>

    <div v-if="isPatient && showDoctorSelector" class="panel-card mb16">
      <div class="panel-title">选择主治医生</div>
      <el-form :model="doctorQuery" :inline="true">
        <el-form-item label="医生姓名">
          <el-input v-model="doctorQuery.nickName" placeholder="请输入医生姓名" @keyup.enter="getDoctorList" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="doctorQuery.phonenumber" placeholder="请输入手机号" @keyup.enter="getDoctorList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getDoctorList">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="doctorList">
        <el-table-column label="医生姓名" prop="nickName" />
        <el-table-column label="手机号" prop="phonenumber" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="confirmSelect(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-tabs v-if="!isPatient" v-model="activeTab" class="mb16">
      <el-tab-pane label="患者状态总览" name="overview" />
      <el-tab-pane label="医患关联管理" name="association" />
    </el-tabs>

    <div v-if="!isPatient && activeTab === 'overview'" class="doctor-layout">
      <aside class="patient-sidebar">
        <div class="sidebar-header">
          <span class="eyebrow">Patient List</span>
          <h3>患者列表</h3>
          <p>选择患者后，医生端查看其完整过程数据和状态画像。</p>
        </div>
        <el-input v-model="patientKeyword" placeholder="搜索患者姓名" clearable class="mb16" />
        <div class="patient-list">
          <button
            v-for="item in filteredPatients"
            :key="item.userId"
            type="button"
            class="patient-entry"
            :class="{ active: selectedPatientId === item.userId }"
            @click="selectPatient(item)"
          >
            <strong>{{ item.nickName }}</strong>
            <span>{{ item.phonenumber || item.userName }}</span>
            <small>{{ item.params?.associationTime || "已关联" }}</small>
          </button>
        </div>
      </aside>

      <main class="doctor-main">
        <overview-panels />
      </main>
    </div>

    <div v-if="isPatient" class="patient-main">
      <overview-panels />
    </div>

    <div v-if="!isPatient && activeTab === 'association'" class="panel-card">
      <div class="toolbar-row">
        <el-button type="success" plain icon="Link" @click="handleBind">
          {{ isDoctor ? "关联现有患者" : "新增医患关联" }}
        </el-button>
        <div class="toolbar-spacer"></div>
        <el-input v-model="queryParams.nickName" placeholder="患者姓名" clearable style="width: 180px" />
        <el-input v-model="queryParams.phonenumber" placeholder="手机号" clearable style="width: 180px" />
        <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="patientList">
        <el-table-column label="头像" align="center" prop="avatar" width="100">
          <template #default="scope">
            <image-preview :src="scope.row.avatar" :width="50" :height="50" />
          </template>
        </el-table-column>
        <el-table-column label="关联医生" align="center" prop="params.doctorName" v-if="!isDoctor" />
        <el-table-column label="患者账号" align="center" prop="userName" />
        <el-table-column label="患者姓名" align="center" prop="nickName" />
        <el-table-column label="手机号" align="center" prop="phonenumber" />
        <el-table-column label="关联时间" align="center" width="180">
          <template #default="scope">
            <el-tag type="info">
              {{ (scope.row.params && scope.row.params.associationTime) ? scope.row.params.associationTime : parseTime(scope.row.createTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Connection" @click="handleUnbind(scope.row)">解绑</el-button>
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
    </div>

    <el-dialog :title="isDoctor ? '关联现有患者' : '医患关联管理'" v-model="bindOpen" width="700px" append-to-body>
      <div v-if="!isDoctor" class="mb16">
        <span class="select-label">选择医生:</span>
        <el-select
          v-model="selectedDoctorId"
          placeholder="请先选择医生"
          filterable
          remote
          :remote-method="searchDoctors"
          @change="loadUnboundForDoctor"
          style="width: 300px"
        >
          <el-option v-for="item in doctorOptions" :key="item.userId" :label="item.nickName" :value="item.userId" />
        </el-select>
      </div>

      <el-table :data="unboundList" @selection-change="handleSelectionChange" height="400px">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="患者姓名" align="center" prop="nickName" />
        <el-table-column label="患者账号" align="center" prop="userName" />
        <el-table-column label="手机号" align="center" prop="phonenumber" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitBind">确定</el-button>
          <el-button @click="bindOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MyDoctor">
import { computed, defineComponent, getCurrentInstance, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import * as echarts from "echarts";
import { listArchive } from "@/api/health/archive";
import { listData } from "@/api/health/data";
import { listWarning } from "@/api/health/warning";
import { listPlan } from "@/api/followup/plan";
import { listRecord } from "@/api/followup/record";
import {
  listDoctors,
  getMyDoctor,
  selectDoctor,
  listMyPatients,
  listUnboundPatients,
  bindPatient,
  unbindPatient,
} from "@/api/system/doctorPatient";
import useUserStore from "@/store/modules/user";
import {
  buildBodyChangeNotes,
  calculatePatientPortrait,
  calculateRehabStats,
  groupMedicationRecords,
  sortByDateDesc,
  summarizeFollowupProgress,
} from "@/utils/care-overview";

const { proxy } = getCurrentInstance();
const { followup_record_status } = proxy.useDict("followup_record_status");
const userStore = useUserStore();

const isPatient = computed(() => !userStore.roles.includes("doctor") && !userStore.roles.includes("admin"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));
const activeTab = ref("overview");
const loading = ref(false);
const overviewLoading = ref(false);
const showDoctorSelector = ref(false);
const bindOpen = ref(false);
const selectedDoctorId = ref(null);
const ids = ref([]);
const total = ref(0);
const doctorList = ref([]);
const patientList = ref([]);
const unboundList = ref([]);
const doctorOptions = ref([]);
const myDoctor = ref(null);
const patientKeyword = ref("");
const selectedPatientId = ref(isPatient.value ? userStore.id : null);
const doctorQuery = reactive({ nickName: "", phonenumber: "" });
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  nickName: "",
  phonenumber: "",
  params: { doctorName: undefined },
});

const archive = ref(null);
const healthData = ref([]);
const plans = ref([]);
const records = ref([]);
const warnings = ref([]);
const portraitChartRef = ref(null);
let portraitChart = null;

const filteredPatients = computed(() => {
  const keyword = (patientKeyword.value || "").trim();
  if (!keyword) {
    return patientList.value;
  }
  return patientList.value.filter((item) => `${item.nickName || ""}${item.userName || ""}${item.phonenumber || ""}`.includes(keyword));
});

const portrait = computed(() => calculatePatientPortrait({
  archive: archive.value,
  records: records.value,
  plans: plans.value,
  warnings: warnings.value,
  healthData: healthData.value,
}));

const scoreColor = computed(() => {
  if (portrait.value.levelType === "danger") {
    return "#dc2626";
  }
  if (portrait.value.levelType === "warning") {
    return "#f59e0b";
  }
  return "#059669";
});

const medicationGroups = computed(() => groupMedicationRecords(records.value));
const rehabStats = computed(() => calculateRehabStats(plans.value, records.value, healthData.value));
const followupProgress = computed(() => summarizeFollowupProgress(records.value));
const followupHistory = computed(() => sortByDateDesc(records.value, ["recordDate", "updateTime", "createTime"]).slice(0, 8));
const warningHistory = computed(() => sortByDateDesc(warnings.value, ["createTime"]).slice(0, 8));
const bodyChangeNotes = computed(() => buildBodyChangeNotes(healthData.value, warnings.value, archive.value));
const activeAutoPlans = computed(() => plans.value.filter((item) => item.status === "0" && item.cronExpression).length);
const selectedPatientLabel = computed(() => {
  if (isPatient.value) {
    return "我的";
  }
  return patientList.value.find((item) => item.userId === selectedPatientId.value)?.nickName || "患者";
});

const overviewCards = computed(() => [
  { label: "综合状态分", value: `${portrait.value.overallScore}`, note: portrait.value.levelText },
  { label: "预警数量", value: `${warnings.value.length} 条`, note: warningHistory.value[0]?.warningType || "近期无预警" },
  { label: "用药分组", value: `${medicationGroups.value.length} 类`, note: `${medicationGroups.value.reduce((s, i) => s + i.pendingCount, 0)} 条待反馈` },
  { label: "康复完成率", value: `${rehabStats.value.progressPercent}%`, note: `完成 ${rehabStats.value.completedTasks}/${rehabStats.value.totalTasks || 0}` },
  { label: "随访完成率", value: `${followupProgress.value.completionPercent}%`, note: `待处理 ${followupProgress.value.pending} 条` },
  { label: "自动发布任务", value: `${activeAutoPlans.value} 个`, note: "基于计划 Cron 自动派发" },
]);

function warningLevelText(level) {
  const map = { 1: "低危", 2: "中危", 3: "高危" };
  return map[level] || "低危";
}

function warningTagType(level) {
  const map = { 1: "info", 2: "warning", 3: "danger" };
  return map[level] || "info";
}

function renderPortraitChart() {
  if (!portraitChartRef.value) {
    return;
  }
  portraitChart?.dispose();
  portraitChart = echarts.init(portraitChartRef.value);
  portraitChart.setOption({
    tooltip: {},
    radar: {
      radius: "68%",
      axisName: { color: "#164e63" },
      splitLine: { lineStyle: { color: "rgba(8, 145, 178, 0.12)" } },
      splitArea: { areaStyle: { color: ["rgba(236, 254, 255, 0.8)", "rgba(255, 255, 255, 1)"] } },
      indicator: portrait.value.dimensions.map((item) => ({ name: item.label, max: 100 })),
    },
    series: [{
      type: "radar",
      data: [{
        value: portrait.value.dimensions.map((item) => item.value),
        areaStyle: { color: "rgba(8, 145, 178, 0.18)" },
        lineStyle: { color: scoreColor.value, width: 3 },
        itemStyle: { color: scoreColor.value },
      }],
    }],
  });
}

function resizeChart() {
  portraitChart?.resize();
}

function getDoctorList() {
  loading.value = true;
  listDoctors(doctorQuery).then((response) => {
    doctorList.value = response.rows || [];
  }).finally(() => {
    loading.value = false;
  });
}

function fetchMyDoctor() {
  getMyDoctor().then((response) => {
    myDoctor.value = response.data;
    if (!myDoctor.value) {
      showDoctorSelector.value = true;
      getDoctorList();
    }
  });
}

function confirmSelect(row) {
  proxy.$modal.confirm(`是否确认关联 [${row.nickName}] 为主治医生？`).then(() => selectDoctor({ doctorId: row.userId })).then(() => {
    proxy.$modal.msgSuccess("关联成功");
    showDoctorSelector.value = false;
    fetchMyDoctor();
  }).catch(() => {});
}

function getList() {
  loading.value = true;
  listMyPatients(queryParams).then((response) => {
    patientList.value = response.rows || [];
    total.value = response.total || 0;
    if (!selectedPatientId.value && patientList.value.length) {
      selectPatient(patientList.value[0]);
    }
  }).finally(() => {
    loading.value = false;
  });
}

function loadOverview(patientId) {
  if (!patientId) {
    archive.value = null;
    healthData.value = [];
    plans.value = [];
    records.value = [];
    warnings.value = [];
    return;
  }
  overviewLoading.value = true;
  Promise.all([
    listArchive({ pageNum: 1, pageSize: 10, userId: patientId }),
    listData({ pageNum: 1, pageSize: 200, userId: patientId }),
    listPlan({ pageNum: 1, pageSize: 200, patientId }),
    listRecord({ pageNum: 1, pageSize: 200, patientId }),
    listWarning({ pageNum: 1, pageSize: 200, userId: patientId }),
  ]).then(([archiveRes, dataRes, planRes, recordRes, warningRes]) => {
    archive.value = (archiveRes.rows || [])[0] || null;
    healthData.value = dataRes.rows || [];
    plans.value = planRes.rows || [];
    records.value = recordRes.rows || [];
    warnings.value = warningRes.rows || [];
    nextTick(renderPortraitChart);
  }).finally(() => {
    overviewLoading.value = false;
  });
}

function selectPatient(item) {
  selectedPatientId.value = item.userId;
  loadOverview(item.userId);
}

function handleBind() {
  ids.value = [];
  selectedDoctorId.value = null;
  if (isDoctor.value) {
    listUnboundPatients().then((response) => {
      unboundList.value = response.data || [];
    });
  } else {
    searchDoctors("");
  }
  bindOpen.value = true;
}

function searchDoctors(query) {
  listDoctors({ nickName: query, pageSize: 50 }).then((res) => {
    doctorOptions.value = res.rows || [];
  });
}

function loadUnboundForDoctor() {
  if (!selectedDoctorId.value) {
    return;
  }
  listUnboundPatients({ doctorId: selectedDoctorId.value }).then((response) => {
    unboundList.value = response.data || [];
  });
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.userId);
}

function submitBind() {
  if (!ids.value.length) {
    proxy.$modal.msgWarning("请选择要绑定的患者");
    return;
  }
  if (!isDoctor.value && !selectedDoctorId.value) {
    proxy.$modal.msgWarning("请选择医生");
    return;
  }
  const doctorId = isDoctor.value ? null : selectedDoctorId.value;
  Promise.all(ids.value.map((id) => bindPatient({ patientId: id, doctorId }))).then(() => {
    proxy.$modal.msgSuccess("绑定成功");
    bindOpen.value = false;
    getList();
  });
}

function handleUnbind(row) {
  proxy.$modal.confirm(`是否确认解绑患者 ${row.nickName}？`).then(() => {
    const doctorId = !isDoctor.value && row.params && row.params.doctorId ? row.params.doctorId : undefined;
    return unbindPatient(row.userId, doctorId);
  }).then(() => {
    proxy.$modal.msgSuccess("解绑成功");
    getList();
  }).catch(() => {});
}

const OverviewPanels = defineComponent({
  name: "OverviewPanels",
  setup() {
    return () => h("div", { class: "overview-panels", "v-loading": overviewLoading.value }, [
      h("div", { class: "card-grid" }, overviewCards.value.map((item) => h("div", { class: "metric-card", key: item.label }, [
        h("span", item.label),
        h("strong", item.value),
        h("small", item.note),
      ]))),
      h("div", { class: "two-col-grid mb16" }, [
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "状态画像"),
          h("div", { ref: portraitChartRef, class: "portrait-chart" }),
          h("div", { class: "portrait-tags" }, portrait.value.summaryTags.map((tag) => h("span", { class: "mini-tag", key: tag }, tag))),
        ]),
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "身体状态变化"),
          h("ul", { class: "note-list" }, bodyChangeNotes.value.map((item, index) => h("li", { key: index }, item))),
        ]),
      ]),
      h("div", { class: "two-col-grid mb16" }, [
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "用药提醒分类"),
          h("div", { class: "group-stack" }, medicationGroups.value.slice(0, 8).map((group) => h("div", { class: "group-item", key: group.key }, [
            h("div", [
              h("strong", group.category),
              h("span", group.period),
              h("small", `待反馈 ${group.pendingCount} / 已完成 ${group.completedCount}`),
            ]),
            h("span", { class: "count-pill" }, `${group.count} 条`),
          ]))),
        ]),
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "康复训练完成情况"),
          h("div", { class: "rehab-progress" }, [
            h("div", { class: "rehab-figure" }, `${rehabStats.value.progressPercent}%`),
            h("div", { class: "rehab-meta" }, [
              h("strong", `完成 ${rehabStats.value.completedTasks}/${rehabStats.value.totalTasks || 0} 次训练`),
              h("span", `自动发布任务 ${activeAutoPlans.value} 个`),
              h("span", `历史记录 ${rehabStats.value.history.length} 条`),
              h("span", `随访完成率 ${followupProgress.value.completionPercent}%`),
            ]),
          ]),
        ]),
      ]),
      h("div", { class: "two-col-grid" }, [
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "随访变化记录"),
          h("div", { class: "history-stack" }, followupHistory.value.map((item) => h("div", { class: "history-row", key: item.recordId }, [
            h("strong", item.planTitle || "随访计划"),
            h("span", `${proxy.parseTime(item.recordDate, "{y}-{m}-{d}")} / ${item.feedback || item.doctorAdvice || "待处理"}`),
          ]))),
        ]),
        h("div", { class: "panel-card" }, [
          h("div", { class: "panel-title" }, "重点预警"),
          h("div", { class: "history-stack" }, warningHistory.value.map((item) => h("div", { class: "history-row", key: item.warningId }, [
            h("strong", `${item.warningType} · ${warningLevelText(item.warningLevel)}`),
            h("span", item.warningMsg || "-"),
          ]))),
        ]),
      ]),
    ]);
  },
});

const overviewPanels = OverviewPanels;

watch(portrait, () => nextTick(renderPortraitChart), { deep: true });

onMounted(() => {
  if (isPatient.value) {
    fetchMyDoctor();
    loadOverview(userStore.id);
  } else {
    getList();
  }
  window.addEventListener("resize", resizeChart);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  portraitChart?.dispose();
});
</script>

<style scoped lang="scss">
.care-overview-page {
  background:
    radial-gradient(circle at top left, rgba(34, 211, 238, 0.16), transparent 24%),
    radial-gradient(circle at top right, rgba(5, 150, 105, 0.12), transparent 24%),
    #ecfeff;
}

.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.text-right { text-align: right; }
.eyebrow {
  color: #0891b2; font-size: 12px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase;
}

.overview-hero, .panel-card, .metric-card, .patient-sidebar, .patient-entry {
  border: 1px solid rgba(8, 145, 178, 0.12);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 45px rgba(8, 145, 178, 0.08);
}

.overview-hero {
  display: flex; justify-content: space-between; gap: 24px; margin-bottom: 16px; padding: 24px 28px; border-radius: 28px;
}
.overview-hero h2 { margin: 6px 0 10px; color: #164e63; }
.overview-hero p { margin: 0; color: #4b6470; }
.hero-score { min-width: 170px; }
.hero-score :deep(strong) { display: block; color: #164e63; font-size: 30px; }
.hero-score :deep(span) { color: #52707c; font-size: 13px; }

.toolbar-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin-bottom: 16px; }
.toolbar-spacer { flex: 1; }
.select-label { display: inline-block; width: 72px; font-weight: 700; color: #164e63; }

.doctor-layout { display: grid; grid-template-columns: 290px minmax(0, 1fr); gap: 18px; }
.patient-sidebar { padding: 18px; border-radius: 24px; }
.sidebar-header h3 { margin: 6px 0 10px; color: #164e63; }
.sidebar-header p { margin: 0 0 14px; color: #4b6470; }
.patient-list { display: grid; gap: 10px; }
.patient-entry { padding: 14px 16px; border-radius: 18px; text-align: left; cursor: pointer; transition: 0.22s ease; }
.patient-entry strong { display: block; color: #164e63; }
.patient-entry span, .patient-entry small { display: block; margin-top: 4px; color: #52707c; }
.patient-entry:hover, .patient-entry.active { border-color: rgba(5, 150, 105, 0.24); background: linear-gradient(135deg, rgba(34, 211, 238, 0.14), rgba(5, 150, 105, 0.12)); }

.card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; }
.metric-card { min-height: 126px; padding: 18px 20px; border-radius: 22px; }
.metric-card span { color: #52707c; font-size: 13px; }
.metric-card strong { display: block; margin: 12px 0 8px; color: #164e63; font-size: 28px; }
.metric-card small { color: #64748b; }

.two-col-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.panel-card { padding: 18px 20px; border-radius: 24px; }
.panel-title { margin-bottom: 14px; color: #164e63; font-size: 16px; font-weight: 700; }
.portrait-chart { width: 100%; height: 320px; }
.portrait-tags { display: flex; flex-wrap: wrap; gap: 10px; }
.mini-tag, .count-pill {
  display: inline-flex; align-items: center; padding: 6px 12px; border-radius: 999px; background: rgba(8, 145, 178, 0.1); color: #164e63; font-size: 12px;
}
.group-stack, .history-stack, .note-list { display: grid; gap: 12px; }
.group-item, .history-row {
  display: flex; justify-content: space-between; gap: 14px; padding: 14px 16px; border-radius: 18px; background: linear-gradient(135deg, rgba(8, 145, 178, 0.08), rgba(5, 150, 105, 0.08));
}
.group-item strong, .history-row strong, .rehab-meta strong { display: block; color: #164e63; }
.group-item span, .group-item small, .history-row span, .note-list li, .rehab-meta span { color: #52707c; font-size: 13px; }
.note-list { padding-left: 18px; margin: 0; }
.rehab-progress { display: flex; align-items: center; gap: 24px; min-height: 220px; }
.rehab-figure {
  display: flex; align-items: center; justify-content: center; width: 118px; height: 118px; border-radius: 50%;
  background: linear-gradient(135deg, #0891b2, #059669); color: #fff; font-size: 30px; font-weight: 700;
}
.rehab-meta { display: grid; gap: 10px; }

@media (max-width: 1200px) {
  .doctor-layout, .two-col-grid, .card-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .overview-hero, .rehab-progress { flex-direction: column; align-items: flex-start; }
}
</style>
