<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="计划标题" prop="planTitle">
            <el-input
               v-model="queryParams.planTitle"
               placeholder="请输入计划标题"
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
            <el-select v-model="queryParams.status" placeholder="计划状态" clearable style="width: 200px">
               <el-option
                  v-for="dict in followup_plan_status"
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
               v-if="isDoctor || $auth.hasPermi('followup:plan:add')"
            >制定随访计划</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-if="isDoctor || $auth.hasPermi('followup:plan:edit')"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-if="isDoctor || $auth.hasPermi('followup:plan:remove')"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['followup:plan:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="planList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="计划ID" align="center" prop="planId" />
         <el-table-column label="医生姓名" align="center" prop="doctorName" />
         <el-table-column label="患者姓名" align="center" prop="patientName" />
         <el-table-column label="计划标题" align="center" prop="planTitle" />
         <el-table-column label="开始日期" align="center" prop="startDate" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="结束日期" align="center" prop="endDate" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="频率" align="center" prop="frequency">
            <template #default="scope">
               <span>{{ formatFrequency(scope.row.frequency) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="followup_plan_status" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="isDoctor || $auth.hasPermi('followup:plan:edit')">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="isDoctor || $auth.hasPermi('followup:plan:remove')">删除</el-button>
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

      <!-- 添加或修改随访计划对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="planRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="医生ID" prop="doctorId" v-if="!isDoctor">
               <el-input v-model="form.doctorId" placeholder="请输入医生ID" />
            </el-form-item>
            <el-form-item label="患者ID" prop="patientId">
               <el-input v-model="form.patientId" placeholder="请输入患者ID" />
            </el-form-item>
            <el-form-item label="计划标题" prop="planTitle">
               <el-input v-model="form.planTitle" placeholder="请输入计划标题" />
            </el-form-item>
            <el-form-item label="随访内容" prop="planContent">
               <el-input v-model="form.planContent" type="textarea" placeholder="请输入随访内容" />
            </el-form-item>
            <el-form-item label="开始日期" prop="startDate">
               <el-date-picker clearable
                  v-model="form.startDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择开始日期">
               </el-date-picker>
            </el-form-item>
            <el-form-item label="结束日期" prop="endDate">
               <el-date-picker clearable
                  v-model="form.endDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择结束日期">
               </el-date-picker>
            </el-form-item>
            <el-form-item label="频率" prop="frequency">
               <el-select v-model="form.frequency" placeholder="请选择频率">
                  <el-option label="每天" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
               </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio
                     v-for="dict in followup_plan_status"
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

<script setup name="Plan">
import { listPlan, getPlan, delPlan, addPlan, updatePlan } from "@/api/followup/plan";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const { followup_plan_status } = proxy.useDict("followup_plan_status");
const userStore = useUserStore();

const isPatient = computed(() => {
  return !userStore.roles.includes("admin") && !userStore.roles.includes("doctor");
});

const isDoctor = computed(() => {
  return userStore.roles.includes("doctor");
});

const planList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planTitle: null,
    doctorName: null,
    patientName: null,
    status: null,
  },
  rules: {
    doctorId: [
      { required: true, message: "医生ID不能为空", trigger: "blur" }
    ],
    patientId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    planTitle: [
      { required: true, message: "计划标题不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

function formatFrequency(frequency) {
  const map = {
    'daily': '每天',
    'weekly': '每周',
    'monthly': '每月'
  };
  return map[frequency] || frequency;
}

/** 查询随访计划列表 */
function getList() {
  loading.value = true;
  listPlan(queryParams.value).then(response => {
    planList.value = response.rows;
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
    planId: null,
    doctorId: null,
    patientId: null,
    planTitle: null,
    planContent: null,
    startDate: null,
    endDate: null,
    frequency: 'weekly',
    status: '0',
    createBy: null,
    createTime: null
  };
  proxy.resetForm("planRef");
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
  ids.value = selection.map(item => item.planId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加随访计划";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const planId = row.planId || ids.value
  getPlan(planId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改随访计划";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["planRef"].validate(valid => {
    if (valid) {
      if (form.value.planId != null) {
        updatePlan(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPlan(form.value).then(response => {
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
  const planIds = row.planId || ids.value;
  proxy.$modal.confirm('是否确认删除随访计划编号为"' + planIds + '"的数据项？').then(function() {
    return delPlan(planIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('followup/plan/export', {
    ...queryParams.value
  }, `plan_${new Date().getTime()}.xlsx`)
}

getList();
</script>
