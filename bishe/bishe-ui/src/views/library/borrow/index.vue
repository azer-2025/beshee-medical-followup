<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="借阅单号" prop="borrowNo">
        <el-input v-model="queryParams.borrowNo" placeholder="请输入借阅单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="图书名称" prop="bookName">
        <el-input v-model="queryParams.bookName" placeholder="请输入图书名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="读者姓名" prop="nickName">
        <el-input v-model="queryParams.nickName" placeholder="请输入读者姓名" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="读者证号" prop="readerNo">
        <el-input v-model="queryParams.readerNo" placeholder="请输入读者证号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="借阅状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="借阅状态" clearable style="width: 180px">
          <el-option v-for="dict in library_borrow_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:borrow:add']" type="primary" plain icon="Plus" @click="handleAdd">新增借阅</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:borrow:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:borrow:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:borrow:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="borrowList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="封面" align="center" width="88">
        <template #default="scope">
          <image-preview :src="scope.row.cover" :width="44" :height="58" />
        </template>
      </el-table-column>
      <el-table-column label="借阅单号" align="center" prop="borrowNo" min-width="160" />
      <el-table-column label="图书名称" align="center" prop="bookName" min-width="180" show-overflow-tooltip />
      <el-table-column label="读者姓名" align="center" prop="nickName" min-width="120" />
      <el-table-column label="读者证号" align="center" prop="readerNo" min-width="120" />
      <el-table-column label="借阅时间" align="center" prop="borrowDate" min-width="160" />
      <el-table-column label="应还时间" align="center" prop="dueDate" min-width="160" />
      <el-table-column label="归还时间" align="center" prop="returnDate" min-width="160" />
      <el-table-column label="续借次数" align="center" prop="renewCount" width="96" />
      <el-table-column label="催还次数" align="center" prop="reminderCount" width="96" />
      <el-table-column label="逾期天数" align="center" prop="overdueDays" width="96" />
      <el-table-column label="罚金" align="center" prop="fineAmount" width="96">
        <template #default="scope">
          <span>{{ formatMoney(scope.row.fineAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="110">
        <template #default="scope">
          <dict-tag :options="library_borrow_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="320" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-hasPermi="['library:borrow:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['library:borrow:renew']" v-if="scope.row.status !== '1'" link type="primary" icon="Clock" @click="handleRenew(scope.row)">续借</el-button>
          <el-button v-hasPermi="['library:borrow:return']" v-if="scope.row.status !== '1'" link type="primary" icon="RefreshRight" @click="handleReturn(scope.row)">归还</el-button>
          <el-button v-hasPermi="['library:borrow:remind']" v-if="scope.row.status === '2'" link type="warning" icon="Bell" @click="handleRemind(scope.row)">催还</el-button>
          <el-button v-hasPermi="['library:borrow:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="borrowRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="18">
          <template v-if="!form.recordId">
            <el-col :span="12">
              <el-form-item label="选择图书" prop="bookId">
                <el-select v-model="form.bookId" filterable remote reserve-keyword clearable placeholder="请输入图书名称" :remote-method="loadBookOptions" style="width: 100%">
                  <el-option v-for="item in bookOptions" :key="item.bookId" :label="`${item.bookName} / ${item.author}`" :value="item.bookId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选择读者" prop="readerId">
                <el-select v-model="form.readerId" filterable remote reserve-keyword clearable placeholder="请输入读者姓名" :remote-method="loadReaderOptions" style="width: 100%">
                  <el-option v-for="item in readerOptions" :key="item.readerId" :label="`${item.nickName} / ${item.readerNo}`" :value="item.readerId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="借阅天数" prop="borrowDays">
                <el-input-number v-model="form.borrowDays" :min="1" :max="90" style="width: 100%" />
              </el-form-item>
            </el-col>
          </template>
          <template v-else>
            <el-col :span="12">
              <el-form-item label="图书名称">
                <el-input v-model="form.bookName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="读者姓名">
                <el-input v-model="form.nickName" disabled />
              </el-form-item>
            </el-col>
          </template>
          <el-col :span="12">
            <el-form-item label="应还时间" prop="dueDate">
              <el-date-picker v-model="form.dueDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择应还时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="续借次数" prop="renewCount">
              <el-input-number v-model="form.renewCount" :min="0" :max="9" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入备注" />
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

<script setup name="LibraryBorrow">
import { addBorrow, delBorrow, getBorrow, listBorrow, remindBorrow, renewBorrow, returnBorrow, updateBorrow } from "@/api/library/borrow";
import { listBook } from "@/api/library/book";
import { listReader } from "@/api/library/reader";

const { proxy } = getCurrentInstance();
const { library_borrow_status } = proxy.useDict("library_borrow_status");

const borrowList = ref([]);
const bookOptions = ref([]);
const readerOptions = ref([]);
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
    borrowNo: null,
    bookName: null,
    nickName: null,
    readerNo: null,
    status: null
  },
  rules: {
    bookId: [{ required: true, message: "图书不能为空", trigger: "change" }],
    readerId: [{ required: true, message: "读者不能为空", trigger: "change" }],
    dueDate: [{ required: true, message: "应还时间不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function formatMoney(value) {
  const amount = Number(value || 0);
  return amount.toFixed(2);
}

function getList() {
  loading.value = true;
  listBorrow(queryParams.value).then(response => {
    borrowList.value = response.rows || [];
    total.value = response.total || 0;
  }).finally(() => {
    loading.value = false;
  });
}

function loadBookOptions(query = "") {
  return listBook({
    pageNum: 1,
    pageSize: 50,
    bookName: query || undefined,
    status: "0"
  }).then(response => {
    bookOptions.value = response.rows || [];
  });
}

function loadReaderOptions(query = "") {
  return listReader({
    pageNum: 1,
    pageSize: 50,
    nickName: query || undefined,
    status: "0"
  }).then(response => {
    readerOptions.value = response.rows || [];
  });
}

function reset() {
  form.value = {
    recordId: null,
    bookId: null,
    readerId: null,
    borrowDays: 30,
    dueDate: null,
    renewCount: 0,
    remark: null
  };
  proxy.resetForm("borrowRef");
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
  ids.value = selection.map(item => item.recordId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  Promise.all([loadBookOptions(), loadReaderOptions()]).then(() => {
    open.value = true;
    title.value = "新增借阅";
  });
}

function handleUpdate(row) {
  reset();
  const recordId = row?.recordId || ids.value[0];
  getBorrow(recordId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改借阅";
  });
}

function submitForm() {
  proxy.$refs.borrowRef.validate(valid => {
    if (!valid) return;
    const request = form.value.recordId ? updateBorrow(form.value) : addBorrow(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.recordId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleReturn(row) {
  proxy.$modal.confirm(`确认归还《${row.bookName}》吗？`).then(() => {
    return returnBorrow(row.recordId);
  }).then(() => {
    proxy.$modal.msgSuccess("归还成功");
    getList();
  }).catch(() => {});
}

function handleRenew(row) {
  proxy.$modal.confirm(`确认将《${row.bookName}》续借 ${15} 天吗？`).then(() => {
    return renewBorrow(row.recordId);
  }).then(() => {
    proxy.$modal.msgSuccess("续借成功");
    getList();
  }).catch(() => {});
}

function handleRemind(row) {
  proxy.$modal.confirm(`确认对《${row.bookName}》执行一次催还提醒吗？`).then(() => {
    return remindBorrow(row.recordId);
  }).then(() => {
    proxy.$modal.msgSuccess("催还记录已更新");
    getList();
  }).catch(() => {});
}

function handleDelete(row) {
  const recordIds = row?.recordId || ids.value;
  proxy.$modal.confirm(`是否确认删除借阅记录编号为“${recordIds}”的数据项？`).then(() => {
    return delBorrow(recordIds);
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功");
    getList();
  }).catch(() => {});
}

function handleExport() {
  proxy.download("/library/borrow/export", queryParams.value, `borrow_${new Date().getTime()}.xlsx`);
}

getList();
</script>
