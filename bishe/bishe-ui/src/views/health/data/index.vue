<template>
  <div class="app-container health-data-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="患者姓名" prop="patientName" v-if="!isPatient">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="采集日期" prop="measureTime">
        <el-date-picker
          v-model="queryParams.measureTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择日期"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb16">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-if="isPatient || $auth.hasPermi('health:data:add')">
          {{ isPatient ? "上传居家数据" : "新增" }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-if="isPatient || $auth.hasPermi('health:data:edit')">
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-if="isPatient || $auth.hasPermi('health:data:remove')">
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-if="isPatient || $auth.hasPermi('health:data:export')">
          导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <section class="trend-shell">
      <aside class="metric-sidebar">
        <div class="sidebar-head">
          <span class="eyebrow">Trend Sidebar</span>
          <h3>指标分类</h3>
          <p>按体征、心血管、代谢和康复四类切换图表。</p>
        </div>

        <div v-for="group in metricGroups" :key="group.key" class="metric-group">
          <div class="group-label">{{ group.label }}</div>
          <button
            v-for="metric in group.metrics"
            :key="metric.key"
            type="button"
            class="metric-button"
            :class="{ active: metric.key === selectedMetricKey }"
            @click="selectMetric(group.key, metric.key)"
          >
            <span>{{ metric.label }}</span>
            <small>{{ metric.description }}</small>
          </button>
        </div>
      </aside>

      <main class="trend-main">
        <section class="hero-panel">
          <div>
            <span class="eyebrow">{{ selectedGroupLabel }}</span>
            <h2>{{ selectedMetricConfig.label }} 趋势监测</h2>
            <p>{{ selectedMetricConfig.description }}</p>
          </div>
          <div class="hero-value">
            <strong>{{ selectedMetricStats.latestValue }}</strong>
            <span>{{ selectedMetricConfig.unit }}</span>
          </div>
        </section>

        <el-row :gutter="16" class="stats-row">
          <el-col :xs="12" :lg="6" v-for="item in summaryCards" :key="item.label">
            <div class="summary-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.note }}</small>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :xl="15">
            <div class="chart-card">
              <div class="card-title">主趋势折线</div>
              <div ref="trendChartRef" class="chart-box large"></div>
            </div>
          </el-col>
          <el-col :xs="24" :xl="9">
            <div class="chart-card">
              <div class="card-title">近次柱状对比</div>
              <div ref="barChartRef" class="chart-box"></div>
            </div>
          </el-col>
        </el-row>

        <div class="chart-card">
          <div class="card-title">同组指标联动</div>
          <div ref="groupChartRef" class="chart-box"></div>
        </div>
      </main>
    </section>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="数据ID" prop="dataId" width="88" align="center" />
      <el-table-column label="患者姓名" prop="patientName" min-width="120" v-if="!isPatient" />
      <el-table-column label="体征概览" min-width="220">
        <template #default="scope">
          <div class="metric-summary">
            <div>体温：{{ formatMetric(scope.row.temperature, "℃") }}</div>
            <div>身高：{{ formatMetric(scope.row.height, "cm") }}</div>
            <div>体重：{{ formatMetric(scope.row.weight, "kg") }}</div>
            <div>康复上传：{{ scope.row.rehabPhoto ? "已完成" : "未上传" }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="血压" min-width="120" align="center">
        <template #default="scope">
          {{ scope.row.bloodPressureSys || "-" }}/{{ scope.row.bloodPressureDia || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="心率" prop="heartRate" width="100" align="center" />
      <el-table-column label="血糖" prop="bloodSugar" width="100" align="center" />
      <el-table-column label="体检报告" prop="reportImage" width="100" align="center">
        <template #default="scope">
          <image-preview :src="scope.row.reportImage" :width="48" :height="48" />
        </template>
      </el-table-column>
      <el-table-column label="康复照片" prop="rehabPhoto" width="100" align="center">
        <template #default="scope">
          <image-preview :src="scope.row.rehabPhoto" :width="48" :height="48" />
        </template>
      </el-table-column>
      <el-table-column label="采集时间" prop="measureTime" width="168" align="center">
        <template #default="scope">
          <span>{{ parseTime(scope.row.measureTime, "{y}-{m}-{d} {h}:{i}:{s}") }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="dataRef" :model="form" :rules="rules" label-width="110px">
        <el-alert
          v-if="currentTrainingPlan"
          type="success"
          :closable="false"
          show-icon
          class="mb16"
          :title="`当前存在康复训练任务：${currentTrainingPlan}`"
        />
        <el-alert
          v-else
          type="info"
          :closable="false"
          show-icon
          class="mb16"
          title="当前无康复训练任务，也可补充上传康复图片作为随访留档"
        />

        <el-form-item label="患者姓名" prop="userId" v-if="!isPatient">
          <el-select
            v-model="form.userId"
            filterable
            remote
            reserve-keyword
            clearable
            style="width: 100%"
            placeholder="请输入患者姓名"
            :remote-method="loadPatientOptions"
            @change="handlePatientChange"
          >
            <el-option
              v-for="item in patientOptions"
              :key="item.userId"
              :label="`${item.nickName} (${item.userName})`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="体温(℃)" prop="temperature">
              <el-input-number v-model="form.temperature" :precision="1" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number v-model="form.height" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="form.weight" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收缩压" prop="bloodPressureSys">
              <el-input-number v-model="form.bloodPressureSys" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压" prop="bloodPressureDia">
              <el-input-number v-model="form.bloodPressureDia" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="心率(bpm)" prop="heartRate">
              <el-input-number v-model="form.heartRate" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血糖" prop="bloodSugar">
              <el-input-number v-model="form.bloodSugar" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="体检报告" prop="reportImage">
          <image-upload v-model="form.reportImage" />
        </el-form-item>
        <el-form-item label="康复训练拍照" prop="rehabPhoto">
          <image-upload v-model="form.rehabPhoto" />
        </el-form-item>
        <el-form-item label="采集时间" prop="measureTime">
          <el-date-picker
            v-model="form.measureTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择采集时间"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

<script setup name="Data">
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRefs, watch } from "vue";
import * as echarts from "echarts";
import { listArchive } from "@/api/health/archive";
import { addData, delData, getData, listData, updateData } from "@/api/health/data";
import { listMyPatients } from "@/api/system/doctorPatient";
import { listUser } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const metricCatalog = {
  temperature: { label: "体温", unit: "℃", description: "观察炎症或感染波动", color: "#0f766e" },
  height: { label: "身高", unit: "cm", description: "用于 BMI 与成长评估", color: "#0369a1" },
  weight: { label: "体重", unit: "kg", description: "监测营养与体重变化", color: "#2563eb" },
  bloodPressure: { label: "血压", unit: "mmHg", description: "联合观察收缩压与舒张压", color: "#b91c1c" },
  heartRate: { label: "心率", unit: "bpm", description: "反映心血管负荷变化", color: "#dc2626" },
  bloodSugar: { label: "血糖", unit: "mmol/L", description: "评估糖代谢与控制情况", color: "#7c3aed" },
  rehabCompletion: { label: "康复完成率", unit: "%", description: "根据康复照片回传情况换算", color: "#059669" },
};

const metricGroups = [
  {
    key: "vitals",
    label: "基础体征",
    metrics: [
      { key: "temperature", label: "体温", description: "发热与感染趋势" },
      { key: "height", label: "身高", description: "基础资料对比" },
      { key: "weight", label: "体重", description: "营养恢复监测" },
    ],
  },
  {
    key: "cardio",
    label: "心血管",
    metrics: [
      { key: "bloodPressure", label: "血压", description: "收缩压/舒张压" },
      { key: "heartRate", label: "心率", description: "心率波动监测" },
    ],
  },
  {
    key: "metabolism",
    label: "代谢监测",
    metrics: [{ key: "bloodSugar", label: "血糖", description: "空腹与波动情况" }],
  },
  {
    key: "rehab",
    label: "康复执行",
    metrics: [{ key: "rehabCompletion", label: "康复完成率", description: "按照片回传换算百分比" }],
  },
];

const charts = {
  trend: null,
  bar: null,
  group: null,
};

const trendChartRef = ref(null);
const barChartRef = ref(null);
const groupChartRef = ref(null);

const isPatient = computed(() => !userStore.roles.includes("admin") && !userStore.roles.includes("doctor"));
const dataList = ref([]);
const patientOptions = ref([]);
const currentArchive = ref(null);
const selectedGroupKey = ref(metricGroups[0].key);
const selectedMetricKey = ref(metricGroups[0].metrics[0].key);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const currentTrainingPlan = computed(() => currentArchive.value?.trainingPlan || "");
const selectedMetricConfig = computed(() => metricCatalog[selectedMetricKey.value]);
const selectedGroupLabel = computed(() => metricGroups.find((item) => item.key === selectedGroupKey.value)?.label || "趋势分类");

const sortedData = computed(() =>
  dataList.value.slice().sort((a, b) => new Date(a.measureTime).getTime() - new Date(b.measureTime).getTime())
);

const createForm = () => ({
  dataId: null,
  userId: isPatient.value ? userStore.id : null,
  temperature: null,
  height: null,
  weight: null,
  bloodPressureSys: null,
  bloodPressureDia: null,
  heartRate: null,
  bloodSugar: null,
  reportImage: "",
  rehabPhoto: "",
  measureTime: "",
  remark: "",
});

const data = reactive({
  form: createForm(),
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    patientName: "",
    measureTime: "",
  },
  rules: {
    userId: [{ required: true, message: "请选择患者", trigger: "change" }],
    measureTime: [{ required: true, message: "请选择采集时间", trigger: "change" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

const selectedMetricStats = computed(() => buildMetricStats(selectedMetricKey.value, sortedData.value));

const summaryCards = computed(() => [
  {
    label: "最近一次",
    value: selectedMetricStats.value.latestValue,
    note: `单位 ${selectedMetricConfig.value.unit}`,
  },
  {
    label: "平均值",
    value: selectedMetricStats.value.averageValue,
    note: "基于当前筛选范围",
  },
  {
    label: "异常次数",
    value: `${selectedMetricStats.value.abnormalCount} 次`,
    note: "用于重点随访判断",
  },
  {
    label: "样本数量",
    value: `${sortedData.value.length} 条`,
    note: "含历史采集记录",
  },
]);

function formatMetric(value, unit = "") {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  return `${value}${unit ? ` ${unit}` : ""}`;
}

function createMetricValue(metricKey, row) {
  if (metricKey === "bloodPressure") {
    if (row.bloodPressureSys === null || row.bloodPressureSys === undefined) {
      return null;
    }
    return row.bloodPressureSys;
  }
  if (metricKey === "rehabCompletion") {
    return row.rehabPhoto ? 100 : 0;
  }
  return row?.[metricKey] ?? null;
}

function abnormalForMetric(metricKey, row) {
  if (metricKey === "temperature") {
    return row.temperature < 36 || row.temperature > 37.5;
  }
  if (metricKey === "bloodPressure") {
    return row.bloodPressureSys > 140 || row.bloodPressureDia > 90;
  }
  if (metricKey === "heartRate") {
    return row.heartRate < 55 || row.heartRate > 110;
  }
  if (metricKey === "bloodSugar") {
    return row.bloodSugar > 7.8;
  }
  return false;
}

function buildMetricStats(metricKey, rows) {
  const values = rows.map((item) => createMetricValue(metricKey, item)).filter((item) => item !== null && item !== undefined);
  const latest = values.length ? values[values.length - 1] : null;
  const average = values.length ? values.reduce((sum, item) => sum + Number(item), 0) / values.length : null;
  const abnormalCount = rows.filter((item) => abnormalForMetric(metricKey, item)).length;
  return {
    latestValue: latest === null ? "--" : metricKey === "bloodPressure" && rows.length
      ? `${rows[rows.length - 1].bloodPressureSys || "-"} / ${rows[rows.length - 1].bloodPressureDia || "-"}`
      : metricKey === "rehabCompletion"
        ? `${round(latest)}%`
        : Number(latest).toFixed(metricKey === "temperature" || metricKey === "bloodSugar" ? 1 : 0),
    averageValue: average === null ? "--" : metricKey === "rehabCompletion" ? `${round(average)}%` : Number(average).toFixed(1),
    abnormalCount,
  };
}

function round(value) {
  return Math.round(Number(value || 0));
}

function mergePatientOption(userId, patientName) {
  if (!userId || !patientName) {
    return;
  }
  if (!patientOptions.value.some((item) => item.userId === userId)) {
    patientOptions.value.push({
      userId,
      userName: patientName,
      nickName: patientName,
    });
  }
}

function loadPatientOptions(query = "") {
  if (userStore.roles.includes("doctor")) {
    return listMyPatients({ pageNum: 1, pageSize: 100, nickName: query || undefined }).then((response) => {
      patientOptions.value = response.rows || [];
    });
  }
  return listUser({ pageNum: 1, pageSize: 100, nickName: query || undefined, params: { roleKey: "patient" } }).then((response) => {
    patientOptions.value = response.rows || [];
  });
}

function loadArchiveForUser(userId) {
  if (!userId) {
    currentArchive.value = null;
    return Promise.resolve();
  }
  return listArchive({ pageNum: 1, pageSize: 10, userId }).then((response) => {
    currentArchive.value = (response.rows || []).find((item) => item.userId === userId) || (response.rows || [])[0] || null;
  });
}

function getList() {
  loading.value = true;
  listData(queryParams.value)
    .then((response) => {
      dataList.value = response.rows || [];
      total.value = response.total || 0;
      nextTick(renderCharts);
    })
    .finally(() => {
      loading.value = false;
    });
}

function reset() {
  form.value = createForm();
  patientOptions.value = [];
  currentArchive.value = null;
  proxy.resetForm("dataRef");
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
  ids.value = selection.map((item) => item.dataId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handlePatientChange(userId) {
  loadArchiveForUser(userId);
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增健康数据";
  if (isPatient.value) {
    loadArchiveForUser(userStore.id);
  } else {
    loadPatientOptions();
  }
}

function handleUpdate(row) {
  reset();
  const dataId = row?.dataId || ids.value[0];
  if (!dataId) {
    proxy.$modal.msgWarning("请选择一条数据");
    return;
  }
  getData(dataId).then((response) => {
    form.value = { ...createForm(), ...response.data };
    mergePatientOption(response.data.userId, response.data.patientName);
    loadArchiveForUser(response.data.userId);
    open.value = true;
    title.value = "编辑健康数据";
  });
}

function submitForm() {
  proxy.$refs.dataRef.validate((valid) => {
    if (!valid) {
      return;
    }
    const request = form.value.dataId ? updateData(form.value) : addData(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.dataId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const dataIds = row?.dataId ? [row.dataId] : ids.value;
  if (!dataIds.length) {
    proxy.$modal.msgWarning("请选择要删除的数据");
    return;
  }
  proxy.$modal
    .confirm(`是否确认删除健康数据编号为“${dataIds.join(",")}”的数据项？`)
    .then(() => delData(dataIds.join(",")))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

function handleExport() {
  proxy.download("health/data/export", { ...queryParams.value }, `health_data_${new Date().getTime()}.xlsx`);
}

function selectMetric(groupKey, metricKey) {
  selectedGroupKey.value = groupKey;
  selectedMetricKey.value = metricKey;
}

function buildAxisDates(rows) {
  return rows.map((item) => proxy.parseTime(item.measureTime, "{m}-{d} {h}:{i}"));
}

function renderTrendChart() {
  if (!trendChartRef.value) {
    return;
  }
  charts.trend?.dispose();
  charts.trend = echarts.init(trendChartRef.value);

  const dates = buildAxisDates(sortedData.value);
  const color = selectedMetricConfig.value.color;
  let option;

  if (selectedMetricKey.value === "bloodPressure") {
    option = {
      tooltip: { trigger: "axis" },
      legend: { data: ["收缩压", "舒张压"] },
      xAxis: { type: "category", data: dates },
      yAxis: { type: "value", name: "mmHg" },
      series: [
        {
          name: "收缩压",
          type: "line",
          smooth: true,
          data: sortedData.value.map((item) => item.bloodPressureSys),
          lineStyle: { width: 3, color: "#dc2626" },
          itemStyle: { color: "#dc2626" },
          areaStyle: { color: "rgba(220, 38, 38, 0.08)" },
        },
        {
          name: "舒张压",
          type: "line",
          smooth: true,
          data: sortedData.value.map((item) => item.bloodPressureDia),
          lineStyle: { width: 3, color: "#f97316" },
          itemStyle: { color: "#f97316" },
        },
      ],
    };
  } else {
    option = {
      tooltip: { trigger: "axis" },
      xAxis: { type: "category", data: dates },
      yAxis: { type: "value", name: selectedMetricConfig.value.unit },
      series: [
        {
          name: selectedMetricConfig.value.label,
          type: selectedMetricKey.value === "rehabCompletion" ? "bar" : "line",
          smooth: selectedMetricKey.value !== "rehabCompletion",
          data: sortedData.value.map((item) => createMetricValue(selectedMetricKey.value, item)),
          itemStyle: { color },
          lineStyle: { color, width: 3 },
          areaStyle: selectedMetricKey.value === "rehabCompletion" ? null : { color: `${color}22` },
          barWidth: selectedMetricKey.value === "rehabCompletion" ? 28 : null,
        },
      ],
    };
  }

  charts.trend.setOption(option);
}

function renderBarChart() {
  if (!barChartRef.value) {
    return;
  }
  charts.bar?.dispose();
  charts.bar = echarts.init(barChartRef.value);

  const recentRows = sortedData.value.slice(-7);
  charts.bar.setOption({
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: buildAxisDates(recentRows) },
    yAxis: { type: "value" },
    series: [
      {
        name: selectedMetricConfig.value.label,
        type: "bar",
        barWidth: 26,
        itemStyle: { color: selectedMetricConfig.value.color, borderRadius: [8, 8, 0, 0] },
        data:
          selectedMetricKey.value === "bloodPressure"
            ? recentRows.map((item) => item.bloodPressureSys)
            : recentRows.map((item) => createMetricValue(selectedMetricKey.value, item)),
      },
    ],
  });
}

function renderGroupChart() {
  if (!groupChartRef.value) {
    return;
  }
  charts.group?.dispose();
  charts.group = echarts.init(groupChartRef.value);

  const currentGroup = metricGroups.find((item) => item.key === selectedGroupKey.value) || metricGroups[0];
  const dates = buildAxisDates(sortedData.value);
  const series = currentGroup.metrics.flatMap((metric) => {
    if (metric.key === "bloodPressure") {
      return [
        {
          name: "收缩压",
          type: "line",
          smooth: true,
          data: sortedData.value.map((item) => item.bloodPressureSys),
        },
        {
          name: "舒张压",
          type: "line",
          smooth: true,
          data: sortedData.value.map((item) => item.bloodPressureDia),
        },
      ];
    }
    return [
      {
        name: metricCatalog[metric.key].label,
        type: metric.key === "rehabCompletion" ? "bar" : "line",
        smooth: metric.key !== "rehabCompletion",
        data: sortedData.value.map((item) => createMetricValue(metric.key, item)),
      },
    ];
  });

  charts.group.setOption({
    tooltip: { trigger: "axis" },
    legend: { top: 0 },
    grid: { left: "4%", right: "4%", bottom: "4%", top: "18%", containLabel: true },
    xAxis: { type: "category", data: dates },
    yAxis: { type: "value" },
    series,
  });
}

function renderCharts() {
  if (!sortedData.value.length) {
    charts.trend?.clear();
    charts.bar?.clear();
    charts.group?.clear();
    return;
  }
  renderTrendChart();
  renderBarChart();
  renderGroupChart();
}

function resizeCharts() {
  Object.values(charts).forEach((item) => item?.resize());
}

watch([selectedMetricKey, sortedData], () => {
  nextTick(renderCharts);
}, { deep: true });

onMounted(() => {
  getList();
  window.addEventListener("resize", resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeCharts);
  Object.values(charts).forEach((item) => item?.dispose());
});
</script>

<style scoped lang="scss">
.health-data-page {
  --page-bg: #ecfeff;
  --card-bg: rgba(255, 255, 255, 0.96);
  --text-main: #164e63;
  --text-sub: #4b6470;
  --border: rgba(8, 145, 178, 0.12);
  --shadow: 0 18px 50px rgba(8, 145, 178, 0.12);

  background:
    radial-gradient(circle at top left, rgba(34, 211, 238, 0.18), transparent 28%),
    radial-gradient(circle at top right, rgba(5, 150, 105, 0.1), transparent 24%),
    var(--page-bg);
}

.mb16 {
  margin-bottom: 16px;
}

.trend-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
  margin-bottom: 20px;
}

.metric-sidebar,
.hero-panel,
.summary-card,
.chart-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.metric-sidebar {
  padding: 22px 18px;
  border-radius: 24px;
}

.sidebar-head h3,
.hero-panel h2 {
  margin: 6px 0 10px;
  color: var(--text-main);
}

.sidebar-head p,
.hero-panel p,
.summary-card small {
  color: var(--text-sub);
}

.eyebrow {
  color: #0891b2;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-group + .metric-group {
  margin-top: 18px;
}

.group-label {
  margin-bottom: 10px;
  color: var(--text-main);
  font-size: 13px;
  font-weight: 700;
}

.metric-button {
  width: 100%;
  display: block;
  margin-bottom: 10px;
  padding: 14px;
  border: 1px solid transparent;
  border-radius: 16px;
  background: #f4feff;
  color: var(--text-main);
  text-align: left;
  cursor: pointer;
  transition: 0.22s ease;
}

.metric-button small {
  display: block;
  margin-top: 4px;
  color: var(--text-sub);
}

.metric-button:hover,
.metric-button.active {
  border-color: rgba(5, 150, 105, 0.24);
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.14), rgba(5, 150, 105, 0.12));
  transform: translateY(-1px);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
  padding: 24px 28px;
  border-radius: 28px;
}

.hero-value {
  min-width: 140px;
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #0891b2, #059669);
  color: #fff;
  text-align: center;
}

.hero-value strong {
  display: block;
  font-size: 30px;
  line-height: 1;
}

.hero-value span {
  display: block;
  margin-top: 6px;
  opacity: 0.85;
}

.stats-row {
  margin-bottom: 4px;
}

.summary-card {
  min-height: 126px;
  margin-bottom: 16px;
  padding: 18px 20px;
  border-radius: 22px;
}

.summary-card span {
  color: var(--text-sub);
  font-size: 13px;
}

.summary-card strong {
  display: block;
  margin: 12px 0 8px;
  color: var(--text-main);
  font-size: 28px;
}

.chart-card {
  margin-bottom: 16px;
  padding: 18px 20px;
  border-radius: 24px;
}

.card-title {
  margin-bottom: 14px;
  color: var(--text-main);
  font-size: 16px;
  font-weight: 700;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box.large {
  height: 360px;
}

.metric-summary {
  line-height: 1.7;
}

@media (max-width: 1200px) {
  .trend-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-value {
    width: 100%;
  }
}
</style>
