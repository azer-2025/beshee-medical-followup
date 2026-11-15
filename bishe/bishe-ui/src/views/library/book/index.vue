<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="图书名称" prop="bookName">
        <el-input v-model="queryParams.bookName" placeholder="请输入图书名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="queryParams.author" placeholder="请输入作者" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input v-model="queryParams.category" placeholder="请输入分类" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="图书状态" clearable style="width: 180px">
          <el-option v-for="dict in library_book_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:book:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:book:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:book:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['library:book:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="bookList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="封面" align="center" width="90">
        <template #default="scope">
          <image-preview :src="scope.row.cover" :width="46" :height="60" />
        </template>
      </el-table-column>
      <el-table-column label="ISBN" align="center" prop="isbn" min-width="130" />
      <el-table-column label="图书名称" align="center" prop="bookName" min-width="180" show-overflow-tooltip />
      <el-table-column label="作者" align="center" prop="author" min-width="120" />
      <el-table-column label="分类" align="center" prop="category" min-width="100" />
      <el-table-column label="馆藏/可借" align="center" min-width="120">
        <template #default="scope">
          <span>{{ scope.row.totalStock }}/{{ scope.row.availableStock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="library_book_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="馆藏位置" align="center" prop="location" min-width="120" />
      <el-table-column label="累计借阅" align="center" prop="borrowCount" width="100" />
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-hasPermi="['library:book:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['library:book:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
      <el-form ref="bookRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="图书名称" prop="bookName">
              <el-input v-model="form.bookName" placeholder="请输入图书名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入 ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="请输入分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="馆藏位置" prop="location">
              <el-input v-model="form.location" placeholder="如 A-01-02" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择出版日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="馆藏总量" prop="totalStock">
              <el-input-number v-model="form.totalStock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in library_book_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图书封面">
              <image-upload v-model="form.cover" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图书简介" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入图书简介" />
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

<script setup name="LibraryBook">
import { listBook, getBook, addBook, updateBook, delBook } from "@/api/library/book";

const { proxy } = getCurrentInstance();
const { library_book_status } = proxy.useDict("library_book_status");

const bookList = ref([]);
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
    bookName: null,
    author: null,
    category: null,
    status: null
  },
  rules: {
    bookName: [{ required: true, message: "图书名称不能为空", trigger: "blur" }],
    isbn: [{ required: true, message: "ISBN 不能为空", trigger: "blur" }],
    author: [{ required: true, message: "作者不能为空", trigger: "blur" }],
    totalStock: [{ required: true, message: "馆藏总量不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listBook(queryParams.value).then(response => {
    bookList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function reset() {
  form.value = {
    bookId: null,
    isbn: null,
    bookName: null,
    author: null,
    publisher: null,
    category: null,
    cover: null,
    publishDate: null,
    price: 0,
    totalStock: 1,
    status: "0",
    location: null,
    description: null
  };
  proxy.resetForm("bookRef");
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
  ids.value = selection.map(item => item.bookId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增图书";
}

function handleUpdate(row) {
  reset();
  const bookId = row?.bookId || ids.value[0];
  getBook(bookId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改图书";
  });
}

function submitForm() {
  proxy.$refs["bookRef"].validate(valid => {
    if (!valid) return;
    const request = form.value.bookId ? updateBook(form.value) : addBook(form.value);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.bookId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const bookIds = row?.bookId || ids.value;
  proxy.$modal.confirm(`是否确认删除图书编号为"${bookIds}"的数据项？`).then(() => {
    return delBook(bookIds);
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功");
    getList();
  }).catch(() => {});
}

function handleExport() {
  proxy.download("/library/book/export", queryParams.value, `book_${new Date().getTime()}.xlsx`);
}

getList();
</script>
