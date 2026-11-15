<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="患者姓名" prop="patientName" v-if="!isPatient">
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
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-if="!isPatient && (isDoctor || $auth.hasPermi('health:warning:edit'))"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5" v-if="!isPatient">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-if="isDoctor || $auth.hasPermi('health:warning:remove')"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['health:warning:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="warningList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="预警ID" align="center" prop="warningId" />
         <el-table-column label="患者姓名" align="center" prop="patientName" v-if="!isPatient" />
         <el-table-column label="预警类型" align="center" prop="warningType" />
         <el-table-column label="异常值" align="center" prop="warningValue" />
         <el-table-column label="预警等级" align="center" prop="warningLevel">
            <template #default="scope">
               <el-tag v-if="scope.row.warningLevel == 1" type="info">低</el-tag>
               <el-tag v-else-if="scope.row.warningLevel == 2" type="warning">中</el-tag>
               <el-tag v-else-if="scope.row.warningLevel == 3" type="danger">高</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="预警消息" align="center" prop="warningMsg" :show-overflow-tooltip="true" />
         <el-table-column label="是否已读" align="center" prop="isRead">
            <template #default="scope">
               <el-tag v-if="scope.row.isRead == 0" type="danger">未读</el-tag>
               <el-tag v-else type="success">已读</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Check" @click="handleRead(scope.row)" v-if="isPatient && scope.row.isRead == 0">已读</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="!isPatient && (isDoctor || $auth.hasPermi('health:warning:edit'))">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="!isPatient && (isDoctor || $auth.hasPermi('health:warning:remove'))">删除</el-button>
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

      <!-- 添加或修改健康预警对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="warningRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="患者ID" prop="userId">
               <el-input v-model="form.userId" placeholder="请输入患者ID" />
            </el-form-item>
            <el-form-item label="预警类型" prop="warningType">
               <el-input v-model="form.warningType" placeholder="请输入预警类型" />
            </el-form-item>
            <el-form-item label="异常值" prop="warningValue">
               <el-input v-model="form.warningValue" placeholder="请输入异常值" />
            </el-form-item>
            <el-form-item label="预警等级" prop="warningLevel">
               <el-select v-model="form.warningLevel" placeholder="请选择预警等级">
                  <el-option label="低" value="1" />
                  <el-option label="中" value="2" />
                  <el-option label="高" value="3" />
               </el-select>
            </el-form-item>
            <el-form-item label="预警消息" prop="warningMsg">
               <el-input v-model="form.warningMsg" type="textarea" placeholder="请输入预警消息" />
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
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Warning">
import { listWarning, getWarning, delWarning, addWarning, updateWarning, changeWarningReadStatus } from "@/api/health/warning";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const isPatient = computed(() => {
  return !userStore.roles.includes("admin") && !userStore.roles.includes("doctor");
});

const isDoctor = computed(() => {
  return userStore.roles.includes("doctor");
});

const warningList = ref([]);
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
    patientName: null,
    warningType: null,
    warningLevel: null,
    isRead: null,
  },
  rules: {
    userId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    warningType: [
      { required: true, message: "预警类型不能为空", trigger: "blur" }
    ],
    warningValue: [
      { required: true, message: "异常值不能为空", trigger: "blur" }
    ],
    warningLevel: [
        { required: true, message: "预警等级不能为空", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询健康预警列表 */
function getList() {
  loading.value = true;
  listWarning(queryParams.value).then(response => {
    warningList.value = response.rows;
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
    warningId: null,
    userId: null,
    warningType: null,
    warningValue: null,
    warningLevel: null,
    warningMsg: null,
    isRead: '0',
    createTime: null
  };
  proxy.resetForm("warningRef");
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
  ids.value = selection.map(item => item.warningId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 标记为已读 */
function handleRead(row) {
  proxy.$modal.confirm('是否将此预警标记为已读？').then(function() {
    return changeWarningReadStatus(row.warningId, '1');
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("已标记为已读");
  }).catch(() => {});
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加健康预警";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const warningId = row.warningId || ids.value
  getWarning(warningId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改健康预警";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["warningRef"].validate(valid => {
    if (valid) {
      if (form.value.warningId != null) {
        updateWarning(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addWarning(form.value).then(response => {
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
  const warningIds = row.warningId || ids.value;
  proxy.$modal.confirm('是否确认删除健康预警编号为"' + warningIds + '"的数据项？').then(function() {
    return delWarning(warningIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('health/warning/export', {
    ...queryParams.value
  }, `warning_${new Date().getTime()}.xlsx`)
}

getList();
</script>