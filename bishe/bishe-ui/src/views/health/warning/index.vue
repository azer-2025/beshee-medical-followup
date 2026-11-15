<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item v-if="!isPatient" label="患者姓名" prop="patientName">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预警类型" prop="warningType">
        <el-input
          v-model="queryParams.warningType"
          placeholder="请输入预警类型"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预警等级" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="预警等级" clearable style="width: 200px">
          <el-option label="低" value="1" />
          <el-option label="中" value="2" />
          <el-option label="高" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" v-if="!isPatient">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-if="isDoctor || $auth.hasPermi('health:warning:add')"
        >
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatient">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-if="isDoctor || $auth.hasPermi('health:warning:edit')"
        >
          {{ isDoctor ? "二次确认" : "修改" }}
        </el-button>
      </el-col>
      <el-col :span="1.5" v-if="!isPatient">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-if="isDoctor || $auth.hasPermi('health:warning:remove')"
        >
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5" v-if="canEditThresholds">
        <el-button type="info" plain icon="Setting" @click="openThresholdDialog">阈值设置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['health:warning:export']"
        >
          导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="warningList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="预警编号" align="center" prop="warningId" width="100" />
      <el-table-column v-if="!isPatient" label="患者姓名" align="center" prop="patientName" width="130" />
      <el-table-column label="预警类型" align="center" prop="warningType" width="130" />
      <el-table-column label="异常值" align="center" prop="warningValue" width="120" />
      <el-table-column label="阈值依据" align="center" min-width="220">
        <template #default="scope">
          <span>{{ getThresholdRange(scope.row.warningType, scope.row.thresholdRange) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预警等级" align="center" prop="warningLevel" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.warningLevel === '1' || scope.row.warningLevel === 1" type="info">低</el-tag>
          <el-tag v-else-if="scope.row.warningLevel === '2' || scope.row.warningLevel === 2" type="warning">中</el-tag>
          <el-tag v-else type="danger">高</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="医生复核" align="center" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.confirmStatus === '1'" type="success">已确认</el-tag>
          <el-tag v-else type="warning">待确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="复核医生" align="center" prop="confirmBy" width="120" />
      <el-table-column label="复核说明" align="center" prop="confirmNote" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="预警消息" align="center" prop="warningMsg" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="是否已读" align="center" prop="isRead" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isRead === '0' || scope.row.isRead === 0" type="danger">未读</el-tag>
          <el-tag v-else type="success">已读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="isPatient && (scope.row.isRead === '0' || scope.row.isRead === 0)"
            link
            type="primary"
            icon="Check"
            @click="handleRead(scope.row)"
          >
            已读
          </el-button>
          <el-button
            v-if="!isPatient && (isDoctor || $auth.hasPermi('health:warning:edit'))"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >
            {{ isDoctor ? "二次确认" : "修改" }}
          </el-button>
          <el-button
            v-if="!isPatient && (isDoctor || $auth.hasPermi('health:warning:remove'))"
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
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

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="warningRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!isPatient" label="患者姓名" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            remote
            reserve-keyword
            clearable
            style="width: 100%"
            placeholder="请输入患者姓名"
            :remote-method="loadPatientOptions"
          >
            <el-option
              v-for="item in patientOptions"
              :key="item.userId"
              :label="item.nickName"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警类型" prop="warningType">
          <el-input v-model="form.warningType" placeholder="请输入预警类型" />
        </el-form-item>
        <el-form-item label="异常值" prop="warningValue">
          <el-input v-model="form.warningValue" placeholder="请输入异常值" />
        </el-form-item>
        <el-form-item label="系统阈值依据">
          <el-input :model-value="getThresholdRange(form.warningType, form.thresholdRange)" disabled />
          <div class="threshold-tip">智能预警会按系统阈值自动判断，医生可在此基础上二次修改并确认。</div>
        </el-form-item>
        <el-form-item label="预警等级" prop="warningLevel">
          <el-select v-model="form.warningLevel" placeholder="请选择预警等级" style="width: 100%">
            <el-option label="低" value="1" />
            <el-option label="中" value="2" />
            <el-option label="高" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警消息" prop="warningMsg">
          <el-input
            v-model="form.warningMsg"
            type="textarea"
            :rows="4"
            :placeholder="isDoctor ? '可调整系统预警结论并补充处理建议' : '请输入预警消息'"
          />
        </el-form-item>
        <el-form-item v-if="isDoctor && form.warningId" label="复核说明" prop="confirmNote">
          <el-input
            v-model="form.confirmNote"
            type="textarea"
            :rows="3"
            placeholder="请输入医生二次确认说明"
          />
        </el-form-item>
        <el-form-item label="是否已读" prop="isRead">
          <el-radio-group v-model="form.isRead">
            <el-radio label="0">未读</el-radio>
            <el-radio label="1">已读</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">
            {{ isDoctor && form.warningId ? "确认" : "确定" }}
          </el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="thresholdOpen" title="智能预警阈值设置" width="760px" append-to-body>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="管理员和医生可维护智能预警判断阈值；某项留空时系统自动回退到默认阈值。"
        style="margin-bottom: 16px"
      />
      <el-form ref="thresholdRef" :model="thresholdForm" label-width="140px">
        <el-divider content-position="left">血压异常</el-divider>
        <div class="threshold-desc">默认：{{ thresholdDefaultTexts["血压异常"] }}</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="收缩压下限">
              <el-input v-model="thresholdForm.bpSysLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收缩压上限">
              <el-input v-model="thresholdForm.bpSysHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收缩压高危值">
              <el-input v-model="thresholdForm.bpSysCritical" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舒张压下限">
              <el-input v-model="thresholdForm.bpDiaLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舒张压上限">
              <el-input v-model="thresholdForm.bpDiaHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舒张压高危值">
              <el-input v-model="thresholdForm.bpDiaCritical" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">血糖异常</el-divider>
        <div class="threshold-desc">默认：{{ thresholdDefaultTexts["血糖异常"] }}</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="正常下限">
              <el-input v-model="thresholdForm.bloodSugarLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="正常上限">
              <el-input v-model="thresholdForm.bloodSugarHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高危低值">
              <el-input v-model="thresholdForm.bloodSugarCriticalLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高危高值">
              <el-input v-model="thresholdForm.bloodSugarCriticalHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">心率异常</el-divider>
        <div class="threshold-desc">默认：{{ thresholdDefaultTexts["心率异常"] }}</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="正常下限">
              <el-input v-model="thresholdForm.heartRateLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="正常上限">
              <el-input v-model="thresholdForm.heartRateHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高危低值">
              <el-input v-model="thresholdForm.heartRateCriticalLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高危高值">
              <el-input v-model="thresholdForm.heartRateCriticalHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">BMI 判断</el-divider>
        <div class="threshold-desc">默认：{{ thresholdDefaultTexts["体重超标"] }}</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="BMI 下限">
              <el-input v-model="thresholdForm.bmiLow" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="BMI 上限">
              <el-input v-model="thresholdForm.bmiHigh" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="肥胖阈值">
              <el-input v-model="thresholdForm.bmiObesity" clearable placeholder="留空用默认值" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="thresholdSaving" @click="submitThresholdSettings">保存</el-button>
          <el-button @click="thresholdOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Warning">
import {
  listWarning,
  getWarning,
  delWarning,
  addWarning,
  updateWarning,
  changeWarningReadStatus,
  getWarningThresholdSettings,
  updateWarningThresholdSettings,
} from "@/api/health/warning";
import { listUser } from "@/api/system/user";
import { listMyPatients } from "@/api/system/doctorPatient";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const isPatient = computed(() => !userStore.roles.includes("admin") && !userStore.roles.includes("doctor"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));
const canEditThresholds = computed(() => userStore.roles.includes("admin") || userStore.roles.includes("doctor"));

const warningList = ref([]);
const patientOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const open = ref(false);

const thresholdOpen = ref(false);
const thresholdSaving = ref(false);
const thresholdDefaultTexts = ref({});
const thresholdTexts = ref({});

const emptyThresholdForm = () => ({
  bpSysLow: "",
  bpSysHigh: "",
  bpSysCritical: "",
  bpDiaLow: "",
  bpDiaHigh: "",
  bpDiaCritical: "",
  bloodSugarLow: "",
  bloodSugarHigh: "",
  bloodSugarCriticalLow: "",
  bloodSugarCriticalHigh: "",
  heartRateLow: "",
  heartRateHigh: "",
  heartRateCriticalLow: "",
  heartRateCriticalHigh: "",
  bmiLow: "",
  bmiHigh: "",
  bmiObesity: "",
});

const makeEmptyForm = () => ({
  warningId: null,
  userId: null,
  warningType: null,
  warningValue: null,
  warningLevel: null,
  warningMsg: null,
  thresholdRange: null,
  isRead: "0",
  confirmStatus: "0",
  confirmNote: null,
  confirmBy: null,
  confirmTime: null,
  createTime: null,
});

const data = reactive({
  form: makeEmptyForm(),
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    patientName: null,
    warningType: null,
    warningLevel: null,
    isRead: null,
  },
  rules: {
    userId: [{ required: true, message: "患者姓名不能为空", trigger: "change" }],
    warningType: [{ required: true, message: "预警类型不能为空", trigger: "blur" }],
    warningValue: [{ required: true, message: "异常值不能为空", trigger: "blur" }],
    warningLevel: [{ required: true, message: "预警等级不能为空", trigger: "change" }],
  },
  thresholdForm: emptyThresholdForm(),
});

const { queryParams, form, rules, thresholdForm } = toRefs(data);

function normalizeThresholdValue(value) {
  if (value === null || value === undefined) {
    return "";
  }
  return String(value);
}

function applyThresholdSettings(settings = {}) {
  Object.assign(
    thresholdForm.value,
    emptyThresholdForm(),
    Object.fromEntries(Object.entries(settings).map(([key, value]) => [key, normalizeThresholdValue(value)]))
  );
}

function getThresholdRange(type, explicitRange) {
  return explicitRange || thresholdTexts.value[type] || thresholdDefaultTexts.value[type] || "系统按内置阈值自动判断";
}

function loadThresholdSettings() {
  return getWarningThresholdSettings().then((response) => {
    const payload = response.data || {};
    thresholdDefaultTexts.value = payload.defaultTexts || {};
    thresholdTexts.value = payload.effectiveTexts || {};
    applyThresholdSettings(payload.settings || {});
  });
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
    return listMyPatients({
      pageNum: 1,
      pageSize: 50,
      nickName: query || undefined,
    }).then((response) => {
      patientOptions.value = response.rows || [];
    });
  }
  return listUser({
    pageNum: 1,
    pageSize: 50,
    nickName: query || undefined,
    params: { roleKey: "patient" },
  }).then((response) => {
    patientOptions.value = response.rows || [];
  });
}

function getList() {
  loading.value = true;
  return listWarning(queryParams.value)
    .then((response) => {
      warningList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = makeEmptyForm();
  patientOptions.value = [];
  proxy.resetForm("warningRef");
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
  ids.value = selection.map((item) => item.warningId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleRead(row) {
  proxy.$modal
    .confirm("是否将此预警标记为已读？")
    .then(() => changeWarningReadStatus(row.warningId, "1"))
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("已标记为已读");
    })
    .catch(() => {});
}

function handleAdd() {
  reset();
  form.value.thresholdRange = getThresholdRange(form.value.warningType);
  loadPatientOptions();
  open.value = true;
  title.value = "新增健康预警";
}

function handleUpdate(row) {
  reset();
  const warningId = row?.warningId || ids.value;
  getWarning(warningId).then((response) => {
    form.value = {
      ...makeEmptyForm(),
      ...response.data,
      isRead: normalizeThresholdValue(response.data?.isRead || "0"),
      warningLevel: normalizeThresholdValue(response.data?.warningLevel),
      confirmStatus: normalizeThresholdValue(response.data?.confirmStatus || "0"),
    };
    mergePatientOption(response.data.userId, response.data.patientName);
    open.value = true;
    title.value = isDoctor.value ? "医生二次确认预警" : "修改健康预警";
  });
}

function submitForm() {
  proxy.$refs.warningRef.validate((valid) => {
    if (!valid) {
      return;
    }
    if (!form.value.thresholdRange) {
      form.value.thresholdRange = getThresholdRange(form.value.warningType);
    }
    if (isDoctor.value && form.value.warningId) {
      form.value.confirmStatus = "1";
      if (!form.value.confirmNote) {
        form.value.confirmNote = "医生已完成二次复核确认";
      }
    }
    const request = form.value.warningId != null ? updateWarning(form.value) : addWarning(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(
        isDoctor.value && form.value.warningId != null
          ? "确认成功"
          : form.value.warningId != null
            ? "修改成功"
            : "新增成功"
      );
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const warningIds = row?.warningId || ids.value;
  proxy.$modal
    .confirm(`是否确认删除健康预警编号为“${warningIds}”的数据项？`)
    .then(() => delWarning(warningIds))
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

function handleExport() {
  proxy.download(
    "health/warning/export",
    {
      ...queryParams.value,
    },
    `warning_${new Date().getTime()}.xlsx`
  );
}

function openThresholdDialog() {
  loadThresholdSettings().then(() => {
    thresholdOpen.value = true;
  });
}

function submitThresholdSettings() {
  const payload = Object.fromEntries(
    Object.entries(thresholdForm.value).map(([key, value]) => [key, value === "" ? null : value])
  );
  thresholdSaving.value = true;
  updateWarningThresholdSettings(payload)
    .then(() => {
      proxy.$modal.msgSuccess("阈值设置已保存");
      thresholdOpen.value = false;
      return loadThresholdSettings();
    })
    .catch(() => {})
    .finally(() => {
      thresholdSaving.value = false;
    });
}

loadThresholdSettings();
getList();
</script>

<style scoped>
.threshold-tip {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.threshold-desc {
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
