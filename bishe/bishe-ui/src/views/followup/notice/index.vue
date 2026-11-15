<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="通知标题" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          placeholder="请输入通知标题"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="通知状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 180px">
          <el-option
            v-for="dict in sys_notice_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isAdmin" label="发布人" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          placeholder="请输入发布人"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8" v-if="isAdmin">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增通知</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain icon="Refresh" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column v-if="isAdmin" type="selection" width="55" align="center" />
      <el-table-column label="编号" prop="noticeId" width="90" align="center" />
      <el-table-column label="通知标题" prop="noticeTitle" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <dict-tag :options="sys_notice_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="发布人" prop="createBy" width="120" align="center" />
      <el-table-column label="发布时间" prop="createTime" width="180" align="center">
        <template #default="scope">
          <span>{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="通知内容" min-width="380">
        <template #default="scope">
          <div class="notice-preview">{{ formatContent(scope.row.noticeContent) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">查看</el-button>
          <el-button v-if="isAdmin" link type="primary" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-if="isAdmin" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="detailTitle" v-model="detailOpen" width="760px" append-to-body>
      <div v-if="detail" class="notice-detail">
        <div class="detail-title">{{ detail.noticeTitle }}</div>
        <div class="detail-meta">
          <span>发布时间：{{ formatTime(detail.createTime) }}</span>
          <span>发布人：{{ detail.createBy || "-" }}</span>
          <span>状态：{{ formatStatus(detail.status) }}</span>
        </div>
        <div class="detail-body">{{ normalizeNoticeContent(detail.noticeContent) || "-" }}</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="detailOpen = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="通知标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入通知标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="通知状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_notice_status" :key="dict.value" :value="dict.value">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知内容" prop="noticeContent">
          <el-input
            v-model="form.noticeContent"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="请输入通知内容，支持填写随访提醒、复诊建议、注意事项等"
          />
        </el-form-item>
        <el-form-item label="系统关联">
          <div class="relation-panel">
            <div class="relation-header">
              <span class="relation-title">{{ relationSummary.source }}</span>
              <el-tag size="small" type="primary" effect="light">{{ relationSummary.mode }}</el-tag>
            </div>
            <div class="relation-tags">
              <el-tag v-if="relationSummary.planId" size="small" effect="plain">计划 {{ relationSummary.planId }}</el-tag>
              <el-tag v-if="relationSummary.doctorId" size="small" effect="plain">医护 {{ relationSummary.doctorId }}</el-tag>
              <el-tag v-if="relationSummary.patientId" size="small" effect="plain">患者 {{ relationSummary.patientId }}</el-tag>
              <el-tag v-if="relationSummary.round" size="small" type="warning" effect="light">第 {{ relationSummary.round }} 轮</el-tag>
            </div>
            <div class="relation-grid">
              <div class="relation-item">
                <span class="relation-label">来源方式</span>
                <span class="relation-value">{{ relationSummary.source }}</span>
              </div>
              <div class="relation-item">
                <span class="relation-label">派发类型</span>
                <span class="relation-value">{{ relationSummary.mode }}</span>
              </div>
              <div class="relation-item" v-if="relationSummary.planId">
                <span class="relation-label">关联计划</span>
                <span class="relation-value">{{ relationSummary.planId }}</span>
              </div>
              <div class="relation-item" v-if="relationSummary.round">
                <span class="relation-label">提醒轮次</span>
                <span class="relation-value">第{{ relationSummary.round }}轮</span>
              </div>
              <div class="relation-item" v-if="relationSummary.doctorId">
                <span class="relation-label">责任医护</span>
                <span class="relation-value">{{ relationSummary.doctorId }}</span>
              </div>
              <div class="relation-item" v-if="relationSummary.patientId">
                <span class="relation-label">关联患者</span>
                <span class="relation-value">{{ relationSummary.patientId }}</span>
              </div>
            </div>
          </div>
        </el-form-item>
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

<script setup name="FollowupNotice">
import { computed, getCurrentInstance, reactive, ref, toRefs } from "vue";
import useUserStore from "@/store/modules/user";
import { addNotice, delNotice, getNotice, listNotice, updateNotice } from "@/api/followup/notice";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const { sys_notice_status } = proxy.useDict("sys_notice_status");

const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const noticeList = ref([]);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const open = ref(false);
const detailOpen = ref(false);
const title = ref("");
const detailTitle = ref("随访通知详情");
const detail = ref(null);
const isAdmin = computed(() => (userStore.roles || []).includes("admin"));
const relationSummary = computed(() => buildRemarkSummary(form.value.remark));

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    noticeTitle: "",
    createBy: "",
    status: "",
  },
  rules: {
    noticeTitle: [{ required: true, message: "通知标题不能为空", trigger: "blur" }],
    noticeContent: [{ required: true, message: "通知内容不能为空", trigger: "blur" }],
    status: [{ required: true, message: "通知状态不能为空", trigger: "change" }],
  },
});

const { form, queryParams, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listNotice(queryParams.value)
    .then((response) => {
      noticeList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
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
  ids.value = selection.map((item) => item.noticeId);
  single.value = selection.length !== 1;
  multiple.value = selection.length === 0;
}

function reset() {
  form.value = {
    noticeId: undefined,
    noticeTitle: "",
    noticeContent: "",
    status: "0",
    remark: "",
  };
  proxy.resetForm("noticeRef");
}

function cancel() {
  open.value = false;
  reset();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增随访通知";
}

function handleUpdate(row) {
  reset();
  const noticeId = row?.noticeId || ids.value[0];
  if (!noticeId) {
    return;
  }
  getNotice(noticeId).then((response) => {
    form.value = {
      noticeId: response.data.noticeId,
      noticeTitle: response.data.noticeTitle,
      noticeContent: normalizeNoticeContent(response.data.noticeContent),
      status: response.data.status || "0",
      remark: response.data.remark || "",
    };
    open.value = true;
    title.value = "修改随访通知";
  });
}

function handleDetail(row) {
  getNotice(row.noticeId).then((response) => {
    detail.value = response.data;
    detailOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs.noticeRef.validate((valid) => {
    if (!valid) {
      return;
    }
    const submitData = {
      ...form.value,
      noticeContent: normalizeNoticeContent(form.value.noticeContent),
    };
    const request = form.value.noticeId ? updateNotice(submitData) : addNotice(submitData);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.noticeId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const noticeIds = row?.noticeId || ids.value;
  if (!noticeIds || (Array.isArray(noticeIds) && noticeIds.length === 0)) {
    return;
  }
  proxy.$modal
    .confirm(`是否确认删除随访通知 ${Array.isArray(noticeIds) ? noticeIds.join(",") : noticeIds}？`)
    .then(() => delNotice(noticeIds))
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

function formatStatus(value) {
  return sys_notice_status.value.find((item) => item.value === value)?.label || value || "-";
}

function formatTime(value) {
  return proxy.parseTime(value, "{y}-{m}-{d} {h}:{i}:{s}");
}

function formatContent(content) {
  const plainText = normalizeNoticeContent(content);
  if (!plainText) {
    return "-";
  }
  return plainText.length > 80 ? `${plainText.slice(0, 80)}...` : plainText;
}

function normalizeNoticeContent(content) {
  if (!content) {
    return "";
  }
  return String(content)
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/p>/gi, "\n")
    .replace(/<p[^>]*>/gi, "")
    .replace(/&nbsp;/gi, " ")
    .replace(/<[^>]+>/g, "")
    .replace(/\r\n/g, "\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

function buildRemarkSummary(remark) {
  if (!remark) {
    return {
      source: "管理员手工发布",
      mode: "独立通知",
      planId: "",
      doctorId: "",
      patientId: "",
      round: "",
    };
  }
  if (remark.includes("manual")) {
    return {
      source: "管理员手工发布",
      mode: "独立通知",
      planId: "",
      doctorId: extractRemarkValue(remark, "adminId"),
      patientId: "",
      round: "",
    };
  }
  return {
    source: "随访任务自动派发",
    mode: "计划通知",
    planId: extractRemarkValue(remark, "planId"),
    doctorId: extractRemarkValue(remark, "doctorId"),
    patientId: extractRemarkValue(remark, "patientId"),
    round: extractRemarkValue(remark, "round"),
  };
}

function extractRemarkValue(remark, key) {
  const match = String(remark).match(new RegExp(`${key}:([^|]+)`));
  return match?.[1] || "";
}

getList();
</script>

<style scoped>
.notice-preview {
  line-height: 1.7;
  color: #4b5563;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: #6b7280;
  font-size: 13px;
}

.detail-body {
  padding: 16px 18px;
  background: #f8fafc;
  border-radius: 10px;
  line-height: 1.8;
  color: #374151;
  border: 1px solid #e5e7eb;
  word-break: break-word;
  white-space: pre-wrap;
}

.relation-panel {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #dbe7ff;
  background: linear-gradient(180deg, #f8fbff 0%, #f3f7ff 100%);
  border-radius: 10px;
}

.relation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.relation-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.relation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.relation-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}

.relation-item {
  padding: 10px 12px;
  background: #ffffff;
  border: 1px solid #e6eefc;
  border-radius: 8px;
}

.relation-label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #7a8699;
}

.relation-value {
  display: block;
  font-size: 14px;
  color: #253858;
  font-weight: 500;
}
</style>
