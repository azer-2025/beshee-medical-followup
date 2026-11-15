<template>
  <div class="app-container">
    <div v-if="!isPatient">
      <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="82px">
        <el-form-item label="患者姓名" prop="patientName">
          <el-input
            v-model="queryParams.patientName"
            placeholder="请输入患者姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="病种分组" prop="diseaseType">
          <el-select v-model="queryParams.diseaseType" placeholder="全部病种" clearable style="width: 180px">
            <el-option v-for="item in diseaseOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄阶段" prop="ageStage">
          <el-select v-model="queryParams.ageStage" placeholder="全部阶段" clearable style="width: 180px">
            <el-option v-for="item in ageStageOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="queryParams.riskLevel" placeholder="全部等级" clearable style="width: 180px">
            <el-option v-for="item in riskOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签筛选" prop="archiveTags">
          <el-input
            v-model="queryParams.archiveTags"
            placeholder="请输入标签关键词"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-if="$auth.hasPermi('health:archive:add')">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-if="$auth.hasPermi('health:archive:edit')">
            修改
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-if="$auth.hasPermi('health:archive:remove')">
            删除
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-if="$auth.hasPermi('health:archive:export')">
            导出
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="档案ID" prop="archiveId" width="88" align="center" />
        <el-table-column label="患者姓名" prop="patientName" min-width="120" />
        <el-table-column label="病种分组" prop="diseaseType" min-width="140">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.diseaseType || "未分组" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="年龄阶段" prop="ageStage" min-width="120" />
        <el-table-column label="风险等级" prop="riskLevel" min-width="120">
          <template #default="scope">
            <el-tag :type="riskTagType(scope.row.riskLevel)">{{ scope.row.riskLevel || "未评估" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="诊断记录" prop="diagnosisRecord" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="训练计划" prop="trainingPlan" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="用药建议" prop="medicationAdvice" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" prop="createTime" width="168" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleUpdate(scope.row)">
              {{ isDoctor ? "查看" : "编辑" }}
            </el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="$auth.hasPermi('health:archive:remove') && !isDoctor">
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
    </div>

    <div v-else class="patient-view">
      <el-card v-if="currentArchive" shadow="hover" class="archive-card">
        <div class="patient-hero">
          <div>
            <div class="hero-title">我的健康档案</div>
            <div class="hero-subtitle">支持查看检查报告、诊断记录、训练计划、用药建议，并按时间轴追溯病情演变</div>
          </div>
          <el-button type="primary" plain icon="Edit" @click="handleUpdate(currentArchive)">编辑档案</el-button>
        </div>

        <div class="hero-tags">
          <el-tag size="large" type="success">{{ currentArchive.diseaseType || "未分组" }}</el-tag>
          <el-tag size="large" :type="riskTagType(currentArchive.riskLevel)">{{ currentArchive.riskLevel || "未评估" }}</el-tag>
          <el-tag size="large" type="info">{{ currentArchive.ageStage || "未分层" }}</el-tag>
        </div>

        <el-descriptions :column="2" border class="mt16">
          <el-descriptions-item label="患者姓名">{{ currentArchive.patientName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="建档时间">{{ parseTime(currentArchive.createTime) || "-" }}</el-descriptions-item>
          <el-descriptions-item label="病种分组">{{ currentArchive.diseaseType || "-" }}</el-descriptions-item>
          <el-descriptions-item label="年龄阶段">{{ currentArchive.ageStage || "-" }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ currentArchive.riskLevel || "-" }}</el-descriptions-item>
          <el-descriptions-item label="档案标签">
            <div class="tag-list">
              <el-tag v-for="tag in splitTags(currentArchive.archiveTags)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              <span v-if="!splitTags(currentArchive.archiveTags).length">无</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="summary-grid">
          <div class="summary-item" v-for="item in summaryItems(currentArchive)" :key="item.label">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value">{{ item.value || "未填写" }}</div>
          </div>
        </div>

        <el-row :gutter="16" class="record-grid">
          <el-col :xs="24" :sm="12">
            <el-card shadow="never" class="record-card">
              <template #header>检查报告</template>
              <div class="record-text">{{ currentArchive.examReport || "暂无检查报告" }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card shadow="never" class="record-card">
              <template #header>诊断记录</template>
              <div class="record-text">{{ currentArchive.diagnosisRecord || "暂无诊断记录" }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card shadow="never" class="record-card">
              <template #header>训练计划</template>
              <div class="record-text">{{ currentArchive.trainingPlan || "暂无训练计划" }}</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card shadow="never" class="record-card">
              <template #header>用药建议</template>
              <div class="record-text">{{ currentArchive.medicationAdvice || "暂无用药建议" }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="timeline-card">
          <template #header>病情演变时间轴</template>
          <el-timeline class="record-timeline">
            <el-timeline-item
              v-for="item in timelineItems(currentArchive)"
              :key="item.label"
              :timestamp="item.timestamp"
              placement="top"
              :type="item.type"
            >
              <div class="timeline-title">{{ item.label }}</div>
              <div class="timeline-content">{{ item.content }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-card>

      <el-empty v-else description="暂无健康档案">
        <el-button type="primary" icon="Plus" @click="handleAdd">立即建档</el-button>
      </el-empty>
    </div>

    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="archiveRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12" v-if="!isPatient">
            <el-form-item label="患者姓名" prop="userId">
              <el-select
                v-model="form.userId"
                filterable
                remote
                reserve-keyword
                clearable
                style="width: 100%"
                placeholder="请输入患者姓名"
                :remote-method="loadPatientOptions"
                :disabled="isDoctor"
              >
                <el-option
                  v-for="item in patientOptions"
                  :key="item.userId"
                  :label="`${item.nickName} (${item.userName})`"
                  :value="item.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="病种分组" prop="diseaseType">
              <el-select v-model="form.diseaseType" placeholder="请选择病种" style="width: 100%" :disabled="isDoctor">
                <el-option v-for="item in diseaseOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄阶段" prop="ageStage">
              <el-select v-model="form.ageStage" placeholder="请选择年龄阶段" style="width: 100%" :disabled="isDoctor">
                <el-option v-for="item in ageStageOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择风险等级" style="width: 100%" :disabled="isDoctor">
                <el-option v-for="item in riskOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="档案标签" prop="archiveTags">
              <el-select
                v-model="archiveTagSelections"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择档案标签"
                style="width: 100%"
                :disabled="isDoctor"
              >
                <el-option v-for="item in archiveTagOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身高">
              <el-input-number
                v-model="basicInfoForm.height"
                :min="50"
                :max="260"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                :disabled="isDoctor"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重">
              <el-input-number
                v-model="basicInfoForm.weight"
                :min="10"
                :max="300"
                :precision="1"
                controls-position="right"
                style="width: 100%"
                :disabled="isDoctor"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血型">
              <el-select v-model="basicInfoForm.bloodType" placeholder="请选择血型" style="width: 100%" :disabled="isDoctor">
                <el-option label="A型" value="A型" />
                <el-option label="B型" value="B型" />
                <el-option label="O型" value="O型" />
                <el-option label="AB型" value="AB型" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number
                v-model="basicInfoForm.age"
                :min="0"
                :max="120"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                :disabled="isDoctor"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="basicInfoForm.birthDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择出生日期"
                style="width: 100%"
                :disabled="isDoctor"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="basicInfoForm.emergencyContact" placeholder="联系人及电话" :disabled="isDoctor" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">病历资料</el-divider>
        <el-form-item label="既往病史" prop="medicalHistory">
          <el-input v-model="form.medicalHistory" type="textarea" :rows="3" placeholder="请输入既往病史" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="过敏史" prop="allergyHistory">
          <el-input v-model="form.allergyHistory" type="textarea" :rows="3" placeholder="请输入过敏史" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="诊断记录" prop="diagnosisRecord">
          <el-input v-model="form.diagnosisRecord" type="textarea" :rows="3" placeholder="请输入诊断记录" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="检查报告" prop="examReport">
          <el-input v-model="form.examReport" type="textarea" :rows="3" placeholder="请输入检查报告摘要" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="训练计划" prop="trainingPlan">
          <el-input v-model="form.trainingPlan" type="textarea" :rows="3" placeholder="请输入康复训练计划" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="用药建议" prop="medicationAdvice">
          <el-input v-model="form.medicationAdvice" type="textarea" :rows="3" placeholder="请输入用药建议" :disabled="isDoctor" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" :disabled="isDoctor" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" v-if="!isDoctor">确定</el-button>
          <el-button @click="cancel">{{ isDoctor ? "关闭" : "取消" }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Archive">
import { computed, getCurrentInstance, reactive, ref, toRefs } from "vue";
import { addArchive, delArchive, getArchive, listArchive, updateArchive } from "@/api/health/archive";
import { listMyPatients } from "@/api/system/doctorPatient";
import { listUser } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const diseaseOptions = ["高血压", "糖尿病", "冠心病", "脑卒中", "慢阻肺", "骨科康复", "慢病管理"];
const ageStageOptions = ["青年", "中年", "老年", "高龄", "成人"];
const riskOptions = ["低风险", "中风险", "高风险", "重点随访"];
const archiveTagOptions = [
  ...diseaseOptions,
  ...ageStageOptions,
  ...riskOptions,
  "饮食干预",
  "运动干预",
  "睡眠管理",
  "康复训练",
  "复诊提醒",
  "重点观察",
];

const isPatient = computed(() => !userStore.roles.includes("admin") && !userStore.roles.includes("doctor"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));

const archiveList = ref([]);
const patientOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const currentArchive = computed(() => archiveList.value[0] || null);

const createForm = () => ({
  archiveId: null,
  userId: isPatient.value ? userStore.id : null,
  basicInfo: "",
  medicalHistory: "",
  allergyHistory: "",
  diseaseType: "",
  ageStage: "",
  riskLevel: "",
  archiveTags: "",
  diagnosisRecord: "",
  examReport: "",
  trainingPlan: "",
  medicationAdvice: "",
  remark: "",
});

const data = reactive({
  form: createForm(),
  basicInfoForm: {
    height: undefined,
    weight: undefined,
    bloodType: "",
    age: undefined,
    birthDate: "",
    emergencyContact: "",
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    patientName: "",
    diseaseType: "",
    ageStage: "",
    riskLevel: "",
    archiveTags: "",
  },
  rules: {
    userId: [{ required: true, message: "请选择患者", trigger: "change" }],
    diseaseType: [{ required: true, message: "请选择病种分组", trigger: "change" }],
    riskLevel: [{ required: true, message: "请选择风险等级", trigger: "change" }],
  },
});

const { queryParams, form, basicInfoForm, rules } = toRefs(data);
const archiveTagSelections = computed({
  get: () => splitTags(form.value.archiveTags),
  set: (values) => {
    form.value.archiveTags = joinTags(values);
  },
});

function parseBasicInfo(jsonStr) {
  if (!jsonStr) {
    return {};
  }
  try {
    return JSON.parse(jsonStr) || {};
  } catch (error) {
    return {};
  }
}

function splitTags(tags) {
  return (tags || "")
    .split(/[，,]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function joinTags(tags) {
  return [...new Set((tags || []).map((item) => String(item || "").trim()).filter(Boolean))].join("，");
}

function riskTagType(level) {
  const map = {
    低风险: "success",
    中风险: "warning",
    高风险: "danger",
    重点随访: "danger",
  };
  return map[level] || "info";
}

function summaryItems(archive) {
  const info = parseBasicInfo(archive.basicInfo);
  return [
    { label: "身高", value: formatMetricValue(info.height, "cm") },
    { label: "体重", value: formatMetricValue(info.weight, "kg") },
    { label: "血型", value: info.bloodType },
    { label: "年龄", value: formatMetricValue(info.age, "岁") },
    { label: "出生日期", value: info.birthDate },
    { label: "紧急联系人", value: info.emergencyContact },
  ];
}

function formatMetricValue(value, unit) {
  if (value === undefined || value === null || value === "") {
    return "";
  }
  const text = String(value).trim();
  return /[a-zA-Z\u4e00-\u9fa5]/.test(text) ? text : `${text}${unit}`;
}

function toNumberOrUndefined(value) {
  if (value === undefined || value === null || value === "") {
    return undefined;
  }
  const matched = String(value).match(/-?\d+(\.\d+)?/);
  return matched ? Number(matched[0]) : undefined;
}

function buildBasicInfoPayload() {
  return {
    ...basicInfoForm.value,
    height: basicInfoForm.value.height === undefined || basicInfoForm.value.height === null ? "" : String(basicInfoForm.value.height),
    weight: basicInfoForm.value.weight === undefined || basicInfoForm.value.weight === null ? "" : String(basicInfoForm.value.weight),
    age: basicInfoForm.value.age === undefined || basicInfoForm.value.age === null ? "" : String(basicInfoForm.value.age),
  };
}

function timelineItems(archive) {
  const createdAt = proxy.parseTime(archive.createTime, "{y}-{m}-{d} {h}:{i}") || "建档时间";
  const updatedAt = proxy.parseTime(archive.updateTime, "{y}-{m}-{d} {h}:{i}") || createdAt;
  const baseItems = [
    { label: "建档完成", content: `完成患者健康档案建档，已记录病种分组、风险等级、基础资料。`, timestamp: createdAt, type: "primary" },
    { label: "诊断记录", content: archive.diagnosisRecord || "暂无诊断记录", timestamp: updatedAt, type: "success" },
    { label: "检查报告", content: archive.examReport || "暂无检查报告", timestamp: updatedAt, type: "warning" },
    { label: "训练计划", content: archive.trainingPlan || "暂无训练计划", timestamp: updatedAt, type: "info" },
    { label: "用药建议", content: archive.medicationAdvice || "暂无用药建议", timestamp: updatedAt, type: "danger" },
    {
      label: "病史与备注",
      content: `既往病史：${archive.medicalHistory || "无"}\n过敏史：${archive.allergyHistory || "无"}\n备注：${archive.remark || "无"}`,
      timestamp: updatedAt,
      type: "primary",
    },
  ];
  return baseItems;
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
  if (isDoctor.value) {
    return listMyPatients({ pageNum: 1, pageSize: 100, nickName: query || undefined }).then((response) => {
      patientOptions.value = response.rows || [];
    });
  }
  return listUser({ pageNum: 1, pageSize: 100, nickName: query || undefined, params: { roleKey: "patient" } }).then((response) => {
    patientOptions.value = response.rows || [];
  });
}

function getList() {
  loading.value = true;
  listArchive(queryParams.value)
    .then((response) => {
      archiveList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

function reset() {
  form.value = createForm();
  basicInfoForm.value = {
    height: undefined,
    weight: undefined,
    bloodType: "",
    age: undefined,
    birthDate: "",
    emergencyContact: "",
  };
  patientOptions.value = [];
  proxy.resetForm("archiveRef");
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
  ids.value = selection.map((item) => item.archiveId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增健康档案";
  if (!isPatient.value) {
    loadPatientOptions();
  }
}

function handleUpdate(row) {
  reset();
  const archiveId = row?.archiveId || ids.value[0];
  if (!archiveId) {
    proxy.$modal.msgWarning("请选择一条档案");
    return;
  }
  getArchive(archiveId).then((response) => {
    form.value = { ...createForm(), ...response.data };
    const parsedBasicInfo = parseBasicInfo(form.value.basicInfo);
    basicInfoForm.value = {
      ...basicInfoForm.value,
      ...parsedBasicInfo,
      height: toNumberOrUndefined(parsedBasicInfo.height),
      weight: toNumberOrUndefined(parsedBasicInfo.weight),
      age: toNumberOrUndefined(parsedBasicInfo.age),
    };
    mergePatientOption(response.data.userId, response.data.patientName);
    open.value = true;
    title.value = isDoctor.value ? "查看健康档案" : "编辑健康档案";
  });
}

function submitForm() {
  proxy.$refs.archiveRef.validate((valid) => {
    if (!valid) {
      return;
    }
    form.value.archiveTags = joinTags(archiveTagSelections.value);
    form.value.basicInfo = JSON.stringify(buildBasicInfoPayload());
    const request = form.value.archiveId ? updateArchive(form.value) : addArchive(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.archiveId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const archiveIds = row?.archiveId ? [row.archiveId] : ids.value;
  if (!archiveIds.length) {
    proxy.$modal.msgWarning("请选择要删除的档案");
    return;
  }
  proxy.$modal
    .confirm(`是否确认删除健康档案编号为“${archiveIds.join(",")}”的数据项？`)
    .then(() => delArchive(archiveIds.join(",")))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

function handleExport() {
  proxy.download("health/archive/export", { ...queryParams.value }, `archive_${new Date().getTime()}.xlsx`);
}

getList();
</script>

<style scoped>
.patient-view {
  max-width: 1180px;
  margin: 0 auto;
}

.archive-card {
  border-radius: 18px;
}

.patient-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 6px 0 4px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.mt16 {
  margin-top: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin: 24px 0;
}

.summary-item {
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fafc, #eef6ff);
  border: 1px solid #e2e8f0;
}

.summary-label {
  color: #64748b;
  margin-bottom: 8px;
}

.summary-value {
  color: #0f172a;
  font-size: 18px;
  font-weight: 600;
}

.record-grid {
  margin-bottom: 24px;
}

.record-card {
  height: 100%;
  border-radius: 14px;
}

.record-text {
  min-height: 88px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #334155;
}

.timeline-card {
  border-radius: 14px;
}

.record-timeline {
  margin-top: 8px;
}

.timeline-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.timeline-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #475569;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.mb8 {
  margin-bottom: 8px;
}
</style>
