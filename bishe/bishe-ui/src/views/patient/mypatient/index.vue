<template>
   <div class="app-container">
      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleCreate"
            >新增患者</el-button>
         </el-col>
         <el-col :span="1.5" v-if="isDoctor">
            <el-button
               type="success"
               plain
               icon="Link"
               @click="handleBind"
            >关联现有患者</el-button>
         </el-col>
         <right-toolbar @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="patientList">
         <el-table-column label="头像" align="center" prop="avatar" width="100">
            <template #default="scope">
               <image-preview :src="scope.row.avatar" :width="50" :height="50"/>
            </template>
         </el-table-column>
         <el-table-column label="关联医生" align="center" prop="params.doctorName" v-if="isAdmin" />
         <el-table-column label="患者账号" align="center" prop="userName" />
         <el-table-column label="患者姓名" align="center" prop="nickName" />
         <el-table-column label="手机号码" align="center" prop="phonenumber" />
         <el-table-column label="性别" align="center" prop="sex">
            <template #default="scope">
               <dict-tag :options="sys_user_sex" :value="scope.row.sex" />
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
               <el-button v-if="isDoctor" link type="primary" icon="Connection" @click="handleUnbind(scope.row)">解绑</el-button>
               <el-button v-if="isAdmin" link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
      <el-dialog title="关联现有患者" v-model="bindOpen" width="700px" append-to-body>
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
</template>

<script setup name="MyPatient">
import { listMyPatients, listUnboundPatients, bindPatient, unbindPatient, addPatient, updatePatient } from "@/api/system/doctorPatient";
import { getUser, delUser } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const { sys_user_sex } = proxy.useDict("sys_user_sex");
const userStore = useUserStore();

const patientList = ref([]);
const unboundList = ref([]);
const open = ref(false);
const bindOpen = ref(false);
const loading = ref(true);
const ids = ref([]);
const total = ref(0);
const title = ref("");

const isAdmin = computed(() => userStore.roles.includes("admin"));
const isDoctor = computed(() => userStore.roles.includes("doctor"));

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  rules: {
    userName: [
      { required: true, message: "用户账号不能为空", trigger: "blur" }
    ],
    nickName: [
      { required: true, message: "用户昵称不能为空", trigger: "blur" }
    ],
    password: [
      { required: true, message: "用户密码不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询医生名下的患者列表 */
function getList() {
  loading.value = true;
  listMyPatients(queryParams).then(response => {
    patientList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

function cancelBind() {
  bindOpen.value = false;
}

/** 表单重置 */
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

/** 新增按钮操作 */
function handleCreate() {
  reset();
  open.value = true;
  title.value = "新增患者";
}

/** 关联按钮操作 */
function handleBind() {
  listUnboundPatients().then(response => {
    unboundList.value = response.data;
    bindOpen.value = true;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getUser(row.userId).then(response => {
    form.value = response.data;
    form.value.password = ""; // Reset password field
    open.value = true;
    title.value = "修改患者";
  });
}

/** 提交按钮 */
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

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
}

/** 提交绑定 */
function submitBind() {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning("请选择要绑定的患者");
    return;
  }
  const promises = ids.value.map(id => bindPatient({ patientId: id }));
  Promise.all(promises).then(() => {
    proxy.$modal.msgSuccess("绑定成功");
    bindOpen.value = false;
    getList();
  });
}

/** 解绑按钮操作 */
function handleUnbind(row) {
  proxy.$modal.confirm('是否确认解绑患者"' + row.nickName + '"？').then(function() {
    return unbindPatient(row.userId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("解绑成功");
  }).catch(() => {});
}

/** 删除按钮操作 (管理员) */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除患者"' + row.nickName + '"？').then(function() {
    return delUser(row.userId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
