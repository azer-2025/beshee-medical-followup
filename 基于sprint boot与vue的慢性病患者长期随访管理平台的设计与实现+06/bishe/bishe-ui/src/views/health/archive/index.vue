<template>
   <div class="app-container">
      <!-- 管理员/医生视图 -->
      <div v-if="!isPatient">
         <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="患者姓名" prop="patientName">
               <el-input
                  v-model="queryParams.patientName"
                  placeholder="请输入患者姓名"
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
               <el-button
                  type="primary"
                  plain
                  icon="Plus"
                  @click="handleAdd"
                  v-if="isPatient || $auth.hasPermi('health:archive:add')"
               >新增</el-button>
            </el-col>
            <el-col :span="1.5">
               <el-button
                  type="success"
                  plain
                  icon="Edit"
                  :disabled="single"
                  @click="handleUpdate"
                  v-if="isPatient || $auth.hasPermi('health:archive:edit')"
               >修改</el-button>
            </el-col>
            <el-col :span="1.5">
               <el-button
                  type="danger"
                  plain
                  icon="Delete"
                  :disabled="multiple"
                  @click="handleDelete"
                  v-if="isPatient || $auth.hasPermi('health:archive:remove')"
               >删除</el-button>
            </el-col>
            <el-col :span="1.5">
               <el-button
                  type="warning"
                  plain
                  icon="Download"
                  @click="handleExport"
                  v-if="isPatient || $auth.hasPermi('health:archive:export')"
               >导出</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
         </el-row>

         <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="档案ID" align="center" prop="archiveId" />
            <el-table-column label="患者姓名" align="center" prop="patientName" />
            <el-table-column label="基本信息" align="center" width="300">
               <template #default="scope">
                  <div v-if="scope.row.basicInfo">
                     <el-tag v-for="(value, key) in parseBasicInfo(scope.row.basicInfo)" :key="key" style="margin-right: 5px; margin-bottom: 5px;">
                        {{ formatKey(key) }}: {{ value }}
                     </el-tag>
                  </div>
               </template>
            </el-table-column>
            <el-table-column label="既往病史" align="center" prop="medicalHistory" :show-overflow-tooltip="true" />
            <el-table-column label="过敏史" align="center" prop="allergyHistory" :show-overflow-tooltip="true" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
               <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
               </template>
            </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="View" @click="handleUpdate(scope.row)" v-if="isDoctor">查看</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="isPatient || $auth.hasPermi('health:archive:edit')">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="isPatient || $auth.hasPermi('health:archive:remove')">删除</el-button>
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

      <!-- 患者个人视图 -->
      <div v-else class="patient-view">
         <div v-if="archiveList && archiveList.length > 0">
            <el-card class="box-card" shadow="hover">
               <template #header>
                  <div class="card-header">
                     <span><el-icon><User /></el-icon> 我的健康档案</span>
                     <el-button class="button" type="primary" text icon="Edit" @click="handleUpdate(archiveList[0])">编辑档案</el-button>
                  </div>
               </template>
               <div class="archive-content">
                  <el-descriptions title="基本信息" :column="2" border>
                     <el-descriptions-item label="患者姓名">{{ archiveList[0].patientName }}</el-descriptions-item>
                     <el-descriptions-item label="创建时间">{{ parseTime(archiveList[0].createTime) }}</el-descriptions-item>
                     
                     <template v-if="archiveList[0].basicInfo">
                        <el-descriptions-item v-for="(value, key) in parseBasicInfo(archiveList[0].basicInfo)" :key="key" :label="formatKey(key)">
                           {{ value }}
                        </el-descriptions-item>
                     </template>
                  </el-descriptions>
                  
                  <el-divider />
                  
                  <el-descriptions title="医疗历史" :column="1" border direction="vertical">
                     <el-descriptions-item label="既往病史">
                        <div class="text-content">{{ archiveList[0].medicalHistory || '无' }}</div>
                     </el-descriptions-item>
                     <el-descriptions-item label="过敏史">
                         <div class="text-content">{{ archiveList[0].allergyHistory || '无' }}</div>
                     </el-descriptions-item>
                     <el-descriptions-item label="备注">
                         <div class="text-content">{{ archiveList[0].remark || '无' }}</div>
                     </el-descriptions-item>
                  </el-descriptions>
               </div>
            </el-card>
         </div>
         <div v-else class="empty-archive">
            <el-empty description="您还没有建立健康档案">
               <el-button type="primary" size="large" icon="Plus" @click="handleAdd">立即建立档案</el-button>
            </el-empty>
         </div>
      </div>

      <!-- 添加或修改电子健康档案对话框 -->
      <el-dialog :title="title" v-model="open" width="700px" append-to-body>
         <el-form ref="archiveRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="患者ID" prop="userId" v-if="!isPatient">
               <el-input v-model="form.userId" placeholder="请输入患者ID (管理员/医生填写)" />
            </el-form-item>
            
            <el-divider content-position="left">基本信息</el-divider>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="身高">
                     <el-input v-model="basicInfoForm.height" placeholder="例如: 175cm" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="体重">
                     <el-input v-model="basicInfoForm.weight" placeholder="例如: 70kg" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="血型">
                     <el-select v-model="basicInfoForm.bloodType" placeholder="请选择血型">
                        <el-option label="A型" value="A型"></el-option>
                        <el-option label="B型" value="B型"></el-option>
                        <el-option label="O型" value="O型"></el-option>
                        <el-option label="AB型" value="AB型"></el-option>
                        <el-option label="其他" value="其他"></el-option>
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="紧急联系人">
                     <el-input v-model="basicInfoForm.emergencyContact" placeholder="请输入联系人及电话" />
                  </el-form-item>
               </el-col>
            </el-row>

            <el-divider content-position="left">病史信息</el-divider>
            
            <el-form-item label="既往病史" prop="medicalHistory">
               <el-input v-model="form.medicalHistory" type="textarea" :rows="3" placeholder="请输入既往病史，如高血压、糖尿病等" />
            </el-form-item>
            <el-form-item label="过敏史" prop="allergyHistory">
               <el-input v-model="form.allergyHistory" type="textarea" :rows="3" placeholder="请输入过敏史，如药物过敏、食物过敏等" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入备注内容" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm" v-if="!isDoctor">确 定</el-button>
               <el-button @click="cancel">{{ isDoctor ? '关 闭' : '取 消' }}</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Archive">
import { listArchive, getArchive, delArchive, addArchive, updateArchive } from "@/api/health/archive";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const isPatient = computed(() => {
  return !userStore.roles.includes("admin") && !userStore.roles.includes("doctor");
});

const isDoctor = computed(() => {
  return userStore.roles.includes("doctor");
});

const archiveList = ref([]);
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
  basicInfoForm: {
     height: '',
     weight: '',
     bloodType: '',
     emergencyContact: ''
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    patientName: null,
  },
  rules: {
    userId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, basicInfoForm, rules } = toRefs(data);

function parseBasicInfo(jsonStr) {
  if (!jsonStr) return {};
  try {
    const obj = JSON.parse(jsonStr);
    return obj || {};
  } catch (e) {
    return {};
  }
}

function formatKey(key) {
  const map = {
    height: '身高',
    weight: '体重',
    bloodType: '血型',
    emergencyContact: '紧急联系'
  };
  return map[key] || key;
}

/** 查询电子健康档案列表 */
function getList() {
  loading.value = true;
  listArchive(queryParams.value).then(response => {
    if (isPatient.value) {
       // Only show own record for patients
       archiveList.value = response.rows.filter(item => item.userId == userStore.id);
    } else {
       archiveList.value = response.rows;
    }
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
    archiveId: null,
    userId: null,
    basicInfo: null,
    medicalHistory: null,
    allergyHistory: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  basicInfoForm.value = {
     height: '',
     weight: '',
     bloodType: '',
     emergencyContact: ''
  };
  proxy.resetForm("archiveRef");
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
  ids.value = selection.map(item => item.archiveId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加电子健康档案";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const archiveId = row.archiveId || ids.value
  getArchive(archiveId).then(response => {
    form.value = response.data;
    // Parse basic info back to form
    const parsed = parseBasicInfo(form.value.basicInfo);
    basicInfoForm.value = { ...basicInfoForm.value, ...parsed };
    open.value = true;
    title.value = isDoctor.value ? "查看电子健康档案" : "修改电子健康档案";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["archiveRef"].validate(valid => {
    if (valid) {
      // Serialize basic info form to JSON string
      form.value.basicInfo = JSON.stringify(basicInfoForm.value);
      
      if (form.value.archiveId != null) {
        updateArchive(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addArchive(form.value).then(response => {
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
  const archiveIds = row.archiveId || ids.value;
  proxy.$modal.confirm('是否确认删除电子健康档案编号为"' + archiveIds + '"的数据项？').then(function() {
    return delArchive(archiveIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('health/archive/export', {
    ...queryParams.value
  }, `archive_${new Date().getTime()}.xlsx`)
}

getList();
</script>

<style scoped>
.patient-view {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.archive-content {
  padding: 10px;
}

.text-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.empty-archive {
  margin-top: 50px;
  text-align: center;
}
</style>