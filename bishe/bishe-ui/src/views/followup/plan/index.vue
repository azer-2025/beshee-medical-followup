<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="计划标题" prop="planTitle">
        <el-input v-model="queryParams.planTitle" placeholder="请输入计划标题" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="提醒模块" prop="moduleType">
        <el-select v-model="queryParams.moduleType" placeholder="全部模块" clearable style="width: 180px">
          <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="医生姓名" prop="doctorName" v-if="!isDoctor">
        <el-input v-model="queryParams.doctorName" placeholder="请输入医生姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="患者姓名" prop="patientName" v-if="!isPatient">
        <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 180px">
          <el-option v-for="dict in followup_plan_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8" v-if="!isPatient">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-if="isDoctor || $auth.hasPermi('followup:plan:add')">新增提醒计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-if="isDoctor || $auth.hasPermi('followup:plan:edit')">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-if="isDoctor || $auth.hasPermi('followup:plan:remove')">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-if="$auth.hasPermi('followup:plan:export')">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="planList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="!isPatient" />
      <el-table-column label="计划标题" prop="planTitle" min-width="180" />
      <el-table-column label="提醒模块" prop="moduleType" width="120">
        <template #default="scope">
          <el-tag>{{ formatModuleType(scope.row.moduleType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标分组" prop="targetGroup" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="Cron表达式" prop="cronExpression" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="通知渠道" prop="notifyChannel" width="120">
        <template #default="scope">
          {{ formatNotifyChannel(scope.row.notifyChannel) }}
        </template>
      </el-table-column>
      <el-table-column label="医生" prop="doctorName" width="110" />
      <el-table-column label="患者" prop="patientName" width="110" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <dict-tag :options="followup_plan_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width" v-if="!isPatient">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">
            {{ isDoctor ? "编辑" : "查看" }}
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="!isDoctor">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12" v-if="!isDoctor">
            <el-form-item label="医生姓名" prop="doctorId">
              <el-select
                v-model="form.doctorId"
                filterable
                remote
                reserve-keyword
                clearable
                style="width: 100%"
                placeholder="请输入医生姓名"
                :remote-method="loadDoctorOptions"
              >
                <el-option v-for="item in doctorOptions" :key="item.userId" :label="item.nickName" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者姓名" prop="patientId">
              <el-select
                v-model="form.patientId"
                filterable
                remote
                reserve-keyword
                clearable
                style="width: 100%"
                placeholder="请输入患者姓名"
                :remote-method="loadPatientOptions"
              >
                <el-option v-for="item in patientOptions" :key="item.userId" :label="item.nickName" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划标题" prop="planTitle">
              <el-input v-model="form.planTitle" placeholder="例如：高血压一级晨间测量提醒" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提醒模块" prop="moduleType">
              <el-select v-model="form.moduleType" placeholder="请选择提醒模块" style="width: 100%">
                <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标分组" prop="targetGroup">
              <el-input v-model="form.targetGroup" placeholder="如：高血压一级/老年/高风险" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通知渠道" prop="notifyChannel">
              <el-select v-model="form.notifyChannel" placeholder="请选择通知渠道" style="width: 100%">
                <el-option v-for="item in notifyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Cron表达式" prop="cronExpression">
              <el-input v-model="form.cronExpression" placeholder="请选择或输入 Cron 表达式">
                <template #append>
                  <el-button type="primary" @click="handleShowCron">生成表达式</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="提醒说明" prop="reminderDescription">
              <el-input v-model="form.reminderDescription" type="textarea" :rows="3" placeholder="请输入提醒规则说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="随访内容" prop="planContent">
              <el-input v-model="form.planContent" type="textarea" :rows="3" placeholder="请输入随访执行内容" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提醒频率" prop="frequency">
              <el-select v-model="form.frequency" placeholder="请选择频率" style="width: 100%">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in followup_plan_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="要求反馈" prop="requireFeedback">
              <el-switch v-model="form.requireFeedback" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练拍照" prop="requirePhoto">
              <el-switch v-model="form.requirePhoto" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="Cron 表达式生成器" v-model="openCron" append-to-body destroy-on-close>
      <crontab ref="crontabRef" :expression="expression" @hide="openCron = false" @fill="crontabFill" />
    </el-dialog>
  </div>
</template>

<script setup name="Plan">
import { computed, getCurrentInstance, reactive, ref, toRefs } from "vue";
import Crontab from "@/components/Crontab";
import { addPlan, delPlan, getPlan, listPlan, updatePlan } from "@/api/followup/plan";
import { listMyPatients } from "@/api/system/doctorPatient";
import { listUser } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const { followup_plan_status } = proxy.useDict("followup_plan_status");
const userStore = useUserStore();

const moduleOptions = [
  { label: "用药提醒", value: "medication" },
  { label: "指标测量", value: "measurement" },
  { label: "康复训练", value: "rehab" },
  { label: "复诊通知", value: "revisit" },
];
const notifyOptions = [
  { label: "站内通知", value: "websocket" },
  { label: "短信模拟", value: "sms" },
  { label: "站内+短信模拟", value: "all" },
];

const isPatient = computed(() => !userStore.roles.includes("admin") && !userStore.roles.includes("doctor"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));

const planList = ref([]);
const doctorOptions = ref([]);
const patientOptions = ref([]);
const open = ref(false);
const openCron = ref(false);
const expression = ref("");
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const createForm = () => ({
  planId: null,
  doctorId: isDoctor.value ? userStore.id : null,
  patientId: null,
  planTitle: "",
  moduleType: "medication",
  targetGroup: "",
  cronExpression: "",
  reminderDescription: "",
  notifyChannel: "websocket",
  requireFeedback: "Y",
  requirePhoto: "N",
  planContent: "",
  startDate: "",
  endDate: "",
  frequency: "weekly",
  status: "0",
});

const data = reactive({
  form: createForm(),
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planTitle: "",
    moduleType: "",
    doctorName: "",
    patientName: "",
    status: "",
  },
  rules: {
    doctorId: [{ required: true, message: "请选择医生", trigger: "change" }],
    patientId: [{ required: true, message: "请选择患者", trigger: "change" }],
    planTitle: [{ required: true, message: "请输入计划标题", trigger: "blur" }],
    moduleType: [{ required: true, message: "请选择提醒模块", trigger: "change" }],
    cronExpression: [{ required: true, message: "请生成 Cron 表达式", trigger: "blur" }],
  },
});

const { queryParams, form, rules } = toRefs(data);

function formatModuleType(value) {
  return moduleOptions.find((item) => item.value === value)?.label || value || "未设置";
}

function formatNotifyChannel(value) {
  return notifyOptions.find((item) => item.value === value)?.label || value || "未设置";
}

function mergeUserOption(target, user) {
  if (!user?.userId) {
    return;
  }
  if (!target.value.some((item) => item.userId === user.userId)) {
    target.value.push(user);
  }
}

function loadDoctorOptions(query = "") {
  return listUser({ pageNum: 1, pageSize: 100, nickName: query || undefined, params: { roleKey: "doctor" } }).then((response) => {
    doctorOptions.value = response.rows || [];
  });
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
  listPlan(queryParams.value)
    .then((response) => {
      planList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

function reset() {
  form.value = createForm();
  doctorOptions.value = [];
  patientOptions.value = [];
  proxy.resetForm("planRef");
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
  ids.value = selection.map((item) => item.planId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleShowCron() {
  expression.value = form.value.cronExpression;
  openCron.value = true;
}

function crontabFill(value) {
  form.value.cronExpression = value;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增提醒计划";
  loadPatientOptions();
  if (!isDoctor.value) {
    loadDoctorOptions();
  }
}

function handleUpdate(row) {
  reset();
  const planId = row?.planId || ids.value[0];
  if (!planId) {
    proxy.$modal.msgWarning("请选择一条计划");
    return;
  }
  getPlan(planId).then((response) => {
    form.value = { ...createForm(), ...response.data };
    mergeUserOption(patientOptions, {
      userId: response.data.patientId,
      userName: response.data.patientName || "",
      nickName: response.data.patientName || "",
    });
    if (!isDoctor.value) {
      mergeUserOption(doctorOptions, {
        userId: response.data.doctorId,
        userName: response.data.doctorName || "",
        nickName: response.data.doctorName || "",
      });
    }
    open.value = true;
    title.value = "编辑提醒计划";
  });
}

function submitForm() {
  proxy.$refs.planRef.validate((valid) => {
    if (!valid) {
      return;
    }
    if (form.value.moduleType !== "rehab") {
      form.value.requirePhoto = "N";
    }
    const request = form.value.planId ? updatePlan(form.value) : addPlan(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.planId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const planIds = row?.planId ? [row.planId] : ids.value;
  if (!planIds.length) {
    proxy.$modal.msgWarning("请选择要删除的计划");
    return;
  }
  proxy.$modal
    .confirm(`是否确认删除提醒计划编号为“${planIds.join(",")}”的数据项？`)
    .then(() => delPlan(planIds.join(",")))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

function handleExport() {
  proxy.download("followup/plan/export", { ...queryParams.value }, `followup_plan_${new Date().getTime()}.xlsx`);
}

getList();
</script>
