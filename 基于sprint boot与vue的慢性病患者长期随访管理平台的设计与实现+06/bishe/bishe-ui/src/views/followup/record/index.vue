<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="计划ID" prop="planId">
            <el-input
               v-model="queryParams.planId"
               placeholder="请输入计划ID"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="医生姓名" prop="doctorName" v-if="!isDoctor">
            <el-input
               v-model="queryParams.doctorName"
               placeholder="请输入医生姓名"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="患者姓名" prop="patientName" v-if="!isPatient">
            <el-input
               v-model="queryParams.patientName"
               placeholder="请输入患者姓名"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="记录状态" clearable style="width: 200px">
               <el-option
                  v-for="dict in followup_record_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8" v-if="!isPatient">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-if="isDoctor || $auth.hasPermi('followup:record:add')"
            >新增随访记录</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['followup:record:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['followup:record:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['followup:record:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="记录ID" align="center" prop="recordId" />
         <el-table-column label="计划ID" align="center" prop="planId" />
         <el-table-column label="医生姓名" align="center" prop="doctorName" />
         <el-table-column label="患者姓名" align="center" prop="patientName" />
         <el-table-column label="患者反馈" align="center" prop="feedback" :show-overflow-tooltip="true" />
         <el-table-column label="医生建议" align="center" prop="doctorAdvice" :show-overflow-tooltip="true" />
         <el-table-column label="随访日期" align="center" prop="recordDate" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.recordDate, '{y}-{m}-{d}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="followup_record_status" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="isPatient || isDoctor || $auth.hasPermi('followup:record:edit')">
                  {{ isPatient ? '填写反馈' : (isDoctor ? '填写小结' : '修改') }}
               </el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="!isPatient && !isDoctor && $auth.hasPermi('followup:record:remove')">删除</el-button>
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

      <!-- 添加或修改随访记录对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="recordRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="随访计划" prop="planId" v-if="!isPatient">
               <el-select v-model="form.planId" placeholder="请选择随访计划" @change="handlePlanChange" style="width: 100%">
                  <el-option
                     v-for="item in planOptions"
                     :key="item.planId"
                     :label="item.planTitle"
                     :value="item.planId"
                  />
               </el-select>
            </el-form-item>
            <el-form-item label="医生ID" prop="doctorId" v-if="!isPatient && !isDoctor">
               <el-input v-model="form.doctorId" placeholder="请输入医生ID" />
            </el-form-item>
            <el-form-item label="患者ID" prop="patientId" v-if="!isPatient && !isDoctor">
               <el-input v-model="form.patientId" placeholder="请输入患者ID" />
            </el-form-item>
            <el-form-item label="患者反馈" prop="feedback">
               <el-input v-model="form.feedback" type="textarea" placeholder="请输入患者反馈" :disabled="isDoctor" />
            </el-form-item>
            <el-form-item label="医生建议" prop="doctorAdvice">
               <el-input v-model="form.doctorAdvice" type="textarea" placeholder="请输入医生建议" :disabled="isPatient" />
            </el-form-item>
            <el-form-item label="随访日期" prop="recordDate" v-if="!isPatient">
               <el-date-picker clearable
                  v-model="form.recordDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择随访日期">
               </el-date-picker>
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-radio-group v-model="form.status" :disabled="isPatient">
                  <el-radio
                     v-for="dict in followup_record_status"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</el-radio>
               </el-radio-group>
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Record">
import { listRecord, getRecord, delRecord, addRecord, updateRecord } from "@/api/followup/record";
import { listPlan } from "@/api/followup/plan";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const { followup_record_status } = proxy.useDict("followup_record_status");
const userStore = useUserStore();

const isPatient = computed(() => {
  return !userStore.roles.includes("admin") && !userStore.roles.includes("doctor");
});

const isDoctor = computed(() => {
  return userStore.roles.includes("doctor");
});

const recordList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const planOptions = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planId: null,
    doctorName: null,
    patientName: null,
    status: null,
  },
  rules: {
    planId: [
      { required: true, message: "计划ID不能为空", trigger: "blur" }
    ],
    doctorId: [
      { required: true, message: "医生ID不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    recordDate: [
      { required: true, message: "随访日期不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询随访计划列表 */
function getPlanList() {
  listPlan().then(response => {
    planOptions.value = response.rows;
  });
}

/** 随访计划选择事件 */
function handlePlanChange(planId) {
  const plan = planOptions.value.find(item => item.planId === planId);
  if (plan) {
    form.value.patientId = plan.patientId;
    form.value.doctorId = plan.doctorId;
  }
}

/** 查询随访记录列表 */
function getList() {
  loading.value = true;
  listRecord(queryParams.value).then(response => {
    recordList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    recordId: null,
    planId: null,
    patientId: null,
    doctorId: null,
    feedback: null,
    doctorAdvice: null,
    recordDate: null,
    status: '0',
    createTime: null,
    updateTime: null
  };
  proxy.resetForm("recordRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recordId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  getPlanList();
  open.value = true;
  title.value = "添加随访记录";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const recordId = row.recordId || ids.value
  getRecord(recordId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改随访记录";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["recordRef"].validate(valid => {
    if (valid) {
      if (form.value.recordId != null) {
        updateRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const recordIds = row.recordId || ids.value;
  proxy.$modal.confirm('是否确认删除随访记录编号为"' + recordIds + '"的数据项？').then(function() {
    return delRecord(recordIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('followup/record/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

getList();
</script>
