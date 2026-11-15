<template>
  <div class="app-container">
    <!-- 患者视角: 我的主治医生 -->
    <div v-if="isPatient">
      <el-row :gutter="20">
        <!-- 当前主治医生 -->
        <el-col :span="24" class="mb20">
          <el-card class="box-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Avatar /></el-icon> 我的主治医生</span>
              </div>
            </template>
            <div v-if="myDoctor" class="doctor-profile">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="医生姓名">{{ myDoctor.nickName }}</el-descriptions-item>
                <el-descriptions-item label="医生ID">{{ myDoctor.userId }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ myDoctor.phonenumber || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="关联时间">
                  <el-tag type="success">{{ (myDoctor.params && myDoctor.params.associationTime) ? myDoctor.params.associationTime : parseTime(new Date()) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="性别">{{ selectDictLabel(sys_user_sex, myDoctor.sex) }}</el-descriptions-item>
                <el-descriptions-item label="简介" :span="2">{{ myDoctor.remark || '这位医生很懒，什么都没写。' }}</el-descriptions-item>
              </el-descriptions>
              <div class="mt20 text-right">
                <el-button type="warning" plain icon="Refresh" @click="handleSelect">更换医生</el-button>
              </div>
            </div>
            <div v-else class="empty-doctor">
              <el-empty description="您还没有关联主治医生">
                <el-button type="primary" icon="Search" @click="handleSelect">去选择医生</el-button>
              </el-empty>
            </div>
          </el-card>
        </el-col>

        <!-- 医生选择列表 -->
        <el-col :span="24" v-if="showList">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><Search /></el-icon> 选择主治医生</span>
                <el-button type="info" text @click="showList = false">取消</el-button>
              </div>
            </template>

            <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
              <el-form-item label="医生ID" prop="userId">
                <el-input
                  v-model="queryParams.userId"
                  placeholder="请输入医生唯一ID"
                  clearable
                  style="width: 200px"
                  @keyup.enter="getDoctorList"
                />
              </el-form-item>
              <el-form-item label="医生姓名" prop="nickName">
                <el-input
                  v-model="queryParams.nickName"
                  placeholder="请输入医生姓名"
                  clearable
                  style="width: 200px"
                  @keyup.enter="getDoctorList"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="getDoctorList">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table v-loading="loading" :data="doctorList">
              <el-table-column label="医生ID" align="center" prop="userId" />
              <el-table-column label="医生姓名" align="center" prop="nickName" />
              <el-table-column label="性别" align="center" prop="sex">
                <template #default="scope">
                  <dict-tag :options="sys_user_sex" :value="scope.row.sex" />
                </template>
              </el-table-column>
              <el-table-column label="手机号码" align="center" prop="phonenumber" />
              <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-button
                    link
                    type="primary"
                    icon="Check"
                    @click="confirmSelect(scope.row)"
                    :disabled="myDoctor && myDoctor.userId === scope.row.userId"
                  >{{ (myDoctor && myDoctor.userId === scope.row.userId) ? '当前选择' : '选择该医生' }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <pagination
              v-show="total > 0"
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              @pagination="getDoctorList"
            />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 医生/管理员视角: 关联管理 -->
    <div v-else>
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="医生姓名" prop="params.doctorName" v-if="!isDoctor">
           <el-input v-model="queryParams.params.doctorName" placeholder="请输入医生姓名" clearable @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="患者姓名" prop="nickName">
           <el-input v-model="queryParams.nickName" placeholder="请输入患者姓名" clearable @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
           <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter="getList" />
        </el-form-item>
        <el-form-item>
           <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
           <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Link"
               @click="handleBind"
            >{{ isDoctor ? '关联现有患者' : '新增医患关联' }}</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="patientList">
         <el-table-column label="患者ID" align="center" prop="userId" />
         <el-table-column label="头像" align="center" prop="avatar" width="100">
            <template #default="scope">
               <image-preview :src="scope.row.avatar" :width="50" :height="50"/>
            </template>
         </el-table-column>
         <el-table-column label="关联医生" align="center" prop="params.doctorName" v-if="!isDoctor" />
         <el-table-column label="患者账号" align="center" prop="userName" />
         <el-table-column label="患者姓名" align="center" prop="nickName" />
         <el-table-column label="手机号码" align="center" prop="phonenumber" />
         <el-table-column label="性别" align="center" prop="sex">
            <template #default="scope">
               <dict-tag :options="sys_user_sex" :value="scope.row.sex" />
            </template>
         </el-table-column>
         <el-table-column label="关联时间" align="center" width="180">
            <template #default="scope">
               <el-tag type="info">{{ (scope.row.params && scope.row.params.associationTime) ? scope.row.params.associationTime : parseTime(scope.row.createTime) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="注册时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
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

      <!-- 关联现有患者对话框 -->
      <el-dialog :title="isDoctor ? '关联现有患者' : '医患关联管理'" v-model="bindOpen" width="700px" append-to-body>
         <!-- Admin Only: Select Doctor first -->
         <div v-if="!isDoctor" style="margin-bottom: 20px;">
            <span style="font-weight: bold; margin-right: 10px;">选择医生:</span>
            <el-select 
              v-model="selectedDoctorId" 
              placeholder="请先选择医生" 
              filterable 
              remote
              :remote-method="searchDoctors"
              @change="loadUnboundForDoctor"
              style="width: 300px;">
               <el-option
                  v-for="item in doctorOptions"
                  :key="item.userId"
                  :label="item.nickName + ' (' + item.userName + ')'"
                  :value="item.userId"
               ></el-option>
            </el-select>
         </div>

         <el-table :data="unboundList" @selection-change="handleSelectionChange" height="400px">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="患者姓名" align="center" prop="nickName" />
            <el-table-column label="患者账号" align="center" prop="userName" />
            <el-table-column label="手机号码" align="center" prop="phonenumber" />
         </el-table>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitBind">确 定</el-button>
               <el-button @click="cancelBind">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 新增/修改患者对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="用户昵称" prop="nickName">
                     <el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="用户账号" prop="userName">
                     <el-input v-model="form.userName" placeholder="请输入用户账号" maxlength="30" :disabled="form.userId != undefined"/>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="手机号码" prop="phonenumber">
                     <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="用户邮箱" prop="email">
                     <el-input v-model="form.email" placeholder="请输入用户邮箱" maxlength="50" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
                     <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="用户性别">
                     <el-select v-model="form.sex" placeholder="请选择">
                        <el-option
                           v-for="dict in sys_user_sex"
                           :key="dict.value"
                           :label="dict.label"
                           :value="dict.value"
                        ></el-option>
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="24">
                  <el-form-item label="用户头像">
                     <image-upload v-model="form.avatar"/>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="24">
                  <el-form-item label="备注">
                     <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
                  </el-form-item>
               </el-col>
            </el-row>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup name="MyDoctor">
import { listDoctors, getMyDoctor, selectDoctor, listMyPatients, listUnboundPatients, bindPatient, unbindPatient, addPatient, updatePatient } from "@/api/system/doctorPatient";
import { getUser } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const { sys_user_sex } = proxy.useDict("sys_user_sex");
const userStore = useUserStore();

const isPatient = computed(() => !userStore.roles.includes("doctor") && !userStore.roles.includes("admin"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));

// Patient View State
const myDoctor = ref(null);
const doctorList = ref([]);
const showList = ref(false);

// Doctor View State
const patientList = ref([]);
const unboundList = ref([]);
const open = ref(false);
const bindOpen = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const total = ref(0);
const title = ref("");
const selectedDoctorId = ref(null);
const doctorOptions = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    nickName: undefined,
    phonenumber: undefined,
    userId: undefined,
    params: {
        doctorName: undefined
    }
  },
  rules: {
    userName: [{ required: true, message: "用户账号不能为空", trigger: "blur" }],
    nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
    password: [{ required: true, message: "用户密码不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** Main Init */
function init() {
  if (isPatient.value) {
    fetchMyDoctor();
  } else {
    getList();
  }
}

/** [Patient] 获取当前医生 */
function fetchMyDoctor() {
  getMyDoctor().then(response => {
    myDoctor.value = response.data;
    if (!myDoctor.value) {
      showList.value = true;
      getDoctorList();
    }
  });
}

/** [Patient] 获取医生列表 */
function getDoctorList() {
  loading.value = true;
  listDoctors(queryParams.value).then(response => {
    doctorList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** [Patient] 更换/选择医生 */
function handleSelect() {
  showList.value = true;
  getDoctorList();
}

/** [Patient] 确认选择 */
function confirmSelect(row) {
  proxy.$modal.confirm('是否确认关联 [' + row.nickName + '] 医生为您的主治医生？').then(function() {
    return selectDoctor({ doctorId: row.userId });
  }).then(() => {
    proxy.$modal.msgSuccess("关联成功");
    showList.value = false;
    fetchMyDoctor();
  }).catch(() => {});
}

/** [Doctor] 查询医生名下的患者列表 */
function getList() {
  loading.value = true;
  listMyPatients(queryParams.value).then(response => {
    patientList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** [Doctor] 新增患者 */
function handleCreate() {
  reset();
  open.value = true;
  title.value = "新增患者";
}

/** [Doctor] 关联现有患者 */
function handleBind() {
  unboundList.value = [];
  selectedDoctorId.value = null;
  
  if (isDoctor.value) {
     listUnboundPatients().then(response => {
       unboundList.value = response.data;
     });
  } else {
     // Admin: Load doctors for selection
     searchDoctors('');
  }
  bindOpen.value = true;
}

/** Search doctors for Admin Bind Dialog */
function searchDoctors(query) {
   listDoctors({ nickName: query, pageSize: 50 }).then(res => {
      doctorOptions.value = res.rows;
   });
}

/** Load unbound patients when Admin selects a doctor */
function loadUnboundForDoctor() {
   if (!selectedDoctorId.value) return;
   listUnboundPatients({ doctorId: selectedDoctorId.value }).then(response => {
      unboundList.value = response.data;
   });
}

/** [Doctor] 修改患者 */
function handleUpdate(row) {
  reset();
  getUser(row.userId).then(response => {
    form.value = response.data;
    form.value.password = ""; 
    open.value = true;
    title.value = "修改患者";
  });
}

/** [Doctor] 提交绑定 */
function submitBind() {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning("请选择要绑定的患者");
    return;
  }
  if (!isDoctor.value && !selectedDoctorId.value) {
    proxy.$modal.msgWarning("请选择要绑定的医生");
    return;
  }

  const docId = isDoctor.value ? null : selectedDoctorId.value;
  const promises = ids.value.map(id => bindPatient({ patientId: id, doctorId: docId }));
  
  Promise.all(promises).then(() => {
    proxy.$modal.msgSuccess("绑定成功");
    bindOpen.value = false;
    getList();
  });
}

/** [Doctor] 解绑 */
function handleUnbind(row) {
  proxy.$modal.confirm('是否确认解绑患者"' + row.nickName + '"？').then(function() {
    // If admin, pass the doctorId from the row
    const docId = (!isDoctor.value && row.params && row.params.doctorId) ? row.params.doctorId : undefined;
    return unbindPatient(row.userId, docId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("解绑成功");
  }).catch(() => {});
}

/** Helper Functions */
function reset() {
  form.value = {
    userId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    avatar: undefined,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("userRef");
}

function cancel() {
  open.value = false;
  reset();
}

function cancelBind() {
  bindOpen.value = false;
}

function resetQuery() {
  proxy.resetForm("queryRef");
  if (isPatient.value) getDoctorList(); else getList();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
}

function submitForm() {
  proxy.$refs["userRef"].validate(valid => {
    if (valid) {
      if (form.value.userId != undefined) {
        updatePatient(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPatient(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

init();
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
.mt20 {
  margin-top: 20px;
}
.text-right {
  text-align: right;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.doctor-profile {
  padding: 10px;
}
</style>
