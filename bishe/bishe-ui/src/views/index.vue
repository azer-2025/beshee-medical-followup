<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel tone-patient">
          <div class="card-panel-icon-wrapper">
            <svg-icon icon-class="peoples" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">患者总数</div>
            <div class="card-panel-num">{{ stats.patientCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel tone-doctor">
          <div class="card-panel-icon-wrapper">
            <svg-icon icon-class="user" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">医护人数</div>
            <div class="card-panel-num">{{ stats.doctorCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel tone-warning">
          <div class="card-panel-icon-wrapper">
            <svg-icon icon-class="bug" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">健康预警</div>
            <div class="card-panel-num">{{ stats.warningCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel tone-followup">
          <div class="card-panel-icon-wrapper">
            <svg-icon icon-class="form" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日随访</div>
            <div class="card-panel-num">{{ stats.todayFollowups }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="chart-title">近 7 日患者增长趋势</div>
          <div id="trendChart" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-title">预警等级分布</div>
          <div id="warningChart" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-title">病种分布</div>
          <div id="diseaseChart" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-title">随访任务漏斗</div>
          <div id="funnelChart" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-title">风险等级分布</div>
          <div id="riskChart" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="24">
        <div class="chart-card">
          <div class="chart-title">病种与风险等级热力图</div>
          <div id="heatmapChart" class="chart-box heatmap-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">最新预警通知</div>
          <el-table :data="stats.recentWarnings" style="width: 100%; padding-top: 12px">
            <el-table-column label="预警时间" prop="createTime" width="180" align="center">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="患者姓名" prop="patientName" width="140" />
            <el-table-column label="预警类型" prop="warningType" min-width="120" />
            <el-table-column label="异常值" prop="warningValue" width="120" />
            <el-table-column label="提示信息" prop="warningMsg" min-width="220" :show-overflow-tooltip="true" />
            <el-table-column label="等级" width="100" align="center">
              <template #default="scope">
                <el-tag :type="warningTagType(scope.row.warningLevel)">{{ warningLevelText(scope.row.warningLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isRead === '0' ? 'danger' : 'success'">{{ scope.row.isRead === "0" ? "未读" : "已读" }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-dialog
      title="健康预警提醒"
      v-model="warningOpen"
      width="520px"
      center
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="warning-dialog-title">
        <svg-icon icon-class="bug" class-name="warning-icon" />
        您有新的异常预警，请及时查看并处理
      </div>
      <el-table :data="unreadWarnings" border>
        <el-table-column label="类型" prop="warningType" align="center" />
        <el-table-column label="异常值" prop="warningValue" align="center" />
        <el-table-column label="建议" prop="warningMsg" align="center" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleMarkRead">我已知晓</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { getCurrentInstance, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { getDashboardStats } from "@/api/index";
import { changeWarningReadStatus } from "@/api/health/warning";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const stats = ref({
  patientCount: 0,
  doctorCount: 0,
  warningCount: 0,
  todayFollowups: 0,
  recentWarnings: [],
  pieData: {},
  trendDates: [],
  trendData: [],
  diseaseData: {},
  riskData: {},
  ageStageData: {},
  funnelData: {},
  diseaseNames: [],
  riskLevels: [],
  diseaseRiskHeatmap: [],
});

const warningOpen = ref(false);
const unreadWarnings = ref([]);

const charts = {};

function warningLevelText(level) {
  const map = { 1: "低危", 2: "中危", 3: "高危" };
  return map[level] || "低危";
}

function warningTagType(level) {
  const map = { 1: "info", 2: "warning", 3: "danger" };
  return map[level] || "info";
}

function ensureChart(id) {
  const dom = document.getElementById(id);
  if (!dom) {
    return null;
  }
  if (charts[id]) {
    charts[id].dispose();
  }
  charts[id] = echarts.init(dom);
  return charts[id];
}

function initCharts() {
  const trendChart = ensureChart("trendChart");
  const warningChart = ensureChart("warningChart");
  const diseaseChart = ensureChart("diseaseChart");
  const funnelChart = ensureChart("funnelChart");
  const riskChart = ensureChart("riskChart");
  const heatmapChart = ensureChart("heatmapChart");

  const colors = ["#6366f1", "#10b981", "#f59e0b", "#ef4444", "#3b82f6", "#8b5cf6"];

  if (trendChart) {
    trendChart.setOption({
      tooltip: {
        trigger: "axis",
        backgroundColor: "rgba(255, 255, 255, 0.9)",
        borderRadius: 8,
        borderWidth: 0,
        textStyle: { color: "#1e293b" },
        axisPointer: { lineStyle: { color: "#e2e8f0" } }
      },
      grid: { left: "4%", right: "4%", bottom: "5%", top: "10%", containLabel: true },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: stats.value.trendDates,
        axisLine: { lineStyle: { color: "#f1f5f9" } },
        axisLabel: { color: "#94a3b8" }
      },
      yAxis: {
        type: "value",
        splitLine: { lineStyle: { color: "#f1f5f9", type: "dashed" } },
        axisLabel: { color: "#94a3b8" }
      },
      series: [{
        name: "新增患者",
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        data: stats.value.trendData,
        lineStyle: { width: 4, color: "#6366f1" },
        itemStyle: { color: "#6366f1", borderColor: "#fff", borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(99, 102, 241, 0.2)" },
            { offset: 1, color: "rgba(99, 102, 241, 0)" }
          ])
        }
      }]
    });
  }

  if (warningChart) {
    warningChart.setOption({
      tooltip: { trigger: "item" },
      legend: { bottom: 0, icon: "circle", textStyle: { color: "#64748b" } },
      series: [{
        name: "预警等级",
        type: "pie",
        radius: ["50%", "75%"],
        center: ["50%", "45%"],
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 4 },
        label: { show: false },
        data: [
          { value: stats.value.pieData["1"] || 0, name: "低危", itemStyle: { color: "#10b981" } },
          { value: stats.value.pieData["2"] || 0, name: "中危", itemStyle: { color: "#f59e0b" } },
          { value: stats.value.pieData["3"] || 0, name: "高危", itemStyle: { color: "#ef4444" } }
        ]
      }]
    });
  }

  if (diseaseChart) {
    const diseaseData = Object.entries(stats.value.diseaseData || {}).map(([name, value]) => ({ name, value }));
    diseaseChart.setOption({
      tooltip: { trigger: "item" },
      series: [{
        name: "病种分布",
        type: "pie",
        radius: ["20%", "80%"],
        center: ["50%", "50%"],
        roseType: "radius",
        itemStyle: { borderRadius: 8 },
        label: {
          show: true,
          formatter: "{b}\n{c}人",
          color: "#475569",
          fontWeight: "600"
        },
        data: diseaseData.length > 0 ? diseaseData : [{ value: 0, name: "暂无数据" }],
        color: colors
      }]
    });
  }

  if (funnelChart) {
    const funnelSource = Object.entries(stats.value.funnelData || {})
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value);
    
    funnelChart.setOption({
      tooltip: { trigger: "item" },
      series: [{
        type: "funnel",
        left: "15%", top: "10%", bottom: "10%", width: "70%",
        minSize: "20%",
        gap: 12,
        label: {
          position: "inside",
          formatter: "{b}: {c}",
          fontSize: 13,
          color: "#fff",
          fontWeight: "bold"
        },
        itemStyle: {
          borderRadius: 15,
          borderWidth: 0,
          shadowBlur: 15,
          shadowColor: "rgba(0,0,0,0.1)",
          opacity: 0.85
        },
        data: funnelSource,
        color: ["#8b5cf6", "#6366f1", "#0ea5e9"]
      }]
    });
  }

  if (riskChart) {
    const riskNames = Object.keys(stats.value.riskData || {});
    const riskValues = riskNames.map((name) => stats.value.riskData[name]);

    riskChart.setOption({
      tooltip: { trigger: "axis" },
      polar: { radius: ["30%", "80%"], center: ["50%", "50%"] },
      angleAxis: { max: Math.max(...riskValues, 10) * 1.2, startAngle: 90, show: false },
      radiusAxis: { type: "category", data: riskNames, axisLabel: { show: true, color: "#475569", fontWeight: "600", interval: 0 } },
      series: [{
        type: "bar",
        data: riskValues,
        coordinateSystem: "polar",
        itemStyle: {
          borderRadius: 10,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
            { offset: 0, color: "#f87171" },
            { offset: 1, color: "#ef4444" }
          ])
        },
        label: { show: true, position: "end", formatter: "{c}" }
      }]
    });
  }

  if (heatmapChart) {
    // 改为气泡图以更好地展示少量数据
    heatmapChart.setOption({
      tooltip: {
        formatter: (params) => {
          const disease = stats.value.diseaseNames[params.value[0]];
          const risk = stats.value.riskLevels[params.value[1]];
          return `<div style="padding:4px 8px;"><b>${disease}</b><br/>${risk}: <span style="color:#6366f1;font-weight:bold">${params.value[2]}</span></div>`;
        }
      },
      grid: { left: "12%", right: "8%", top: "10%", bottom: "15%" },
      xAxis: {
        type: "category",
        data: stats.value.diseaseNames,
        axisLine: { lineStyle: { color: "#e2e8f0" } },
        splitLine: { show: true, lineStyle: { color: "#f8fafc" } }
      },
      yAxis: {
        type: "category",
        data: stats.value.riskLevels,
        axisLine: { lineStyle: { color: "#e2e8f0" } },
        splitLine: { show: true, lineStyle: { color: "#f8fafc" } }
      },
      series: [{
        type: "scatter",
        symbolSize: (val) => Math.sqrt(val[2]) * 40 + 20,
        data: stats.value.diseaseRiskHeatmap,
        itemStyle: {
          color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, [
            { offset: 0, color: "rgba(99, 102, 241, 0.7)" },
            { offset: 1, color: "rgba(99, 102, 241, 1)" }
          ]),
          shadowBlur: 20,
          shadowColor: "rgba(99, 102, 241, 0.4)"
        },
        label: {
          show: true,
          formatter: (param) => param.value[2],
          color: "#fff",
          fontWeight: "bold"
        }
      }]
    });
  }
}

function resizeCharts() {
  Object.values(charts).forEach((chart) => chart?.resize());
}

function fetchData() {
  getDashboardStats().then((response) => {
    stats.value = response.data;
    if (!userStore.roles.includes("admin")) {
      unreadWarnings.value = (stats.value.recentWarnings || []).filter((item) => item.isRead === "0");
      warningOpen.value = unreadWarnings.value.length > 0;
    }
    nextTick(() => {
      initCharts();
    });
  });
}

function handleMarkRead() {
  Promise.all(unreadWarnings.value.map((item) => changeWarningReadStatus(item.warningId, "1"))).then(() => {
    proxy.$modal.msgSuccess("已标记为已读");
    warningOpen.value = false;
    fetchData();
  });
}

onMounted(() => {
  fetchData();
  window.addEventListener("resize", resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeCharts);
  Object.values(charts).forEach((chart) => chart?.dispose());
});
</script>

<style lang="scss" scoped>
.dashboard-container {
  min-height: 100vh;
  padding: 32px;
  background: #f8fafc;
  background-image: 
    radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.05) 0px, transparent 50%),
    radial-gradient(at 100% 0%, rgba(16, 185, 129, 0.05) 0px, transparent 50%);
}

.panel-group {
  margin-bottom: 24px;
}

.card-panel-col {
  margin-bottom: 24px;
}

.card-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 120px;
  padding: 24px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  }

  &::after {
    content: "";
    position: absolute;
    top: -50%;
    right: -20%;
    width: 150px;
    height: 150px;
    background: currentColor;
    opacity: 0.05;
    border-radius: 50%;
    z-index: 0;
  }
}

.tone-patient { color: #6366f1; }
.tone-doctor { color: #0ea5e9; }
.tone-warning { color: #ef4444; }
.tone-followup { color: #10b981; }

.card-panel-icon-wrapper {
  position: relative;
  z-index: 1;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: currentColor;
  
  .card-panel-icon {
    font-size: 32px;
    color: #fff;
  }
}

.card-panel-description {
  position: relative;
  z-index: 1;
  text-align: right;
}

.card-panel-text {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-panel-num {
  margin-top: 4px;
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
}

.chart-card {
  margin-bottom: 24px;
  padding: 24px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

.chart-title {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  
  &::before {
    content: "";
    width: 4px;
    height: 18px;
    margin-right: 12px;
    background: #6366f1;
    border-radius: 4px;
  }
}

.chart-box {
  width: 100%;
  height: 360px;
}

.heatmap-box {
  height: 450px;
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  
  th {
    font-weight: 600;
    color: #475569;
  }
}

.warning-dialog-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
  color: #ef4444;
  font-size: 18px;
  font-weight: 700;
}
</style>
