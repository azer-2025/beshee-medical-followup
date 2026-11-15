<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="读者证号" prop="readerNo">
        <el-input v-model="queryParams.readerNo" placeholder="请输入读者证号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="登录账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入登录账号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="读者姓名" prop="nickName">
        <el-input v-model="queryParams.nickName" placeholder="请输入读者姓名" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="读者状态" clearable style="width: 180px">
          <el-option v-for="dict in library_reader_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:reader:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:reader:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:reader:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:reader:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="readerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="头像" align="center" width="88">
        <template #default="scope">
          <image-preview :src="scope.row.avatar" :width="44" :height="44" />
        </template>
      </el-table-column>
      <el-table-column label="读者证号" align="center" prop="readerNo" min-width="120" />
      <el-table-column label="登录账号" align="center" prop="userName" min-width="120" />
      <el-table-column label="读者姓名" align="center" prop="nickName" min-width="120" />
      <el-table-column label="手机号" align="center" prop="phonenumber" min-width="120" />
      <el-table-column label="借阅上限" align="center" prop="borrowLimit" width="100" />
      <el-table-column label="当前借阅" align="center" prop="currentBorrowCount" width="100" />
      <el-table-column label="累计借阅" align="center" prop="totalBorrowCount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="library_reader_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-hasPermi="['library:reader:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['library:reader:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="820px" append-to-body>
      <el-form ref="readerRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="登录账号" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入登录账号" :disabled="!!form.readerId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="读者姓名" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入读者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="form.readerId ? '重置密码' : '登录密码'" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="新增必填，修改可留空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-select v-model="form.sex" placeholder="请选择性别">
                <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="读者证号" prop="readerNo">
              <el-input v-model="form.readerNo" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借书证号" prop="cardNo">
              <el-input v-model="form.cardNo" placeholder="请输入借书证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借阅上限" prop="borrowLimit">
              <el-input-number v-model="form.borrowLimit" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in library_reader_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="读者头像">
              <image-upload v-model="form.avatar" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  </div>
</template>

<script setup name="LibraryReader">
import { listReader, getReader, addReader, updateReader, delReader } from "@/api/library/reader";

const { proxy } = getCurrentInstance();
const { library_reader_status, sys_user_sex } = proxy.useDict("library_reader_status", "sys_user_sex");

const readerList = ref([]);
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
    readerNo: null,
    userName: null,
    nickName: null,
    phonenumber: null,
    status: null
  },
  rules: {
    userName: [{ required: true, message: "登录账号不能为空", trigger: "blur" }],
    nickName: [{ required: true, message: "读者姓名不能为空", trigger: "blur" }],
    password: [{
      validator: (rule, value, callback) => {
        if (!form.value.readerId && !value) {
          callback(new Error("新增读者时密码不能为空"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listReader(queryParams.value).then(response => {
    readerList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function reset() {
  form.value = {
    readerId: null,
    userName: null,
    nickName: null,
    password: null,
    phonenumber: null,
    email: null,
    sex: "0",
    avatar: null,
    readerNo: null,
    cardNo: null,
    idCard: null,
    borrowLimit: 5,
    status: "0",
    remark: null
  };
  proxy.resetForm("readerRef");
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
  ids.value = selection.map(item => item.readerId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增读者";
}

function handleUpdate(row) {
  reset();
  const readerId = row?.readerId || ids.value[0];
  getReader(readerId).then(response => {
    form.value = { ...response.data, password: null };
    open.value = true;
    title.value = "修改读者";
  });
}

function submitForm() {
  proxy.$refs["readerRef"].validate(valid => {
    if (!valid) return;
    const request = form.value.readerId ? updateReader(form.value) : addReader(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.readerId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const readerIds = row?.readerId || ids.value;
  proxy.$modal.confirm(`是否确认删除读者编号为"${readerIds}"的数据项？`).then(() => {
    return delReader(readerIds);
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功");
    getList();
  }).catch(() => {});
}

function handleExport() {
  proxy.download("/library/reader/export", queryParams.value, `reader_${new Date().getTime()}.xlsx`);
}

getList();
</script>
