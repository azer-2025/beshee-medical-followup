<template>
  <div class="portal-shell">
    <header class="portal-header">
      <div class="brand">
        <div class="brand-mark">EL</div>
        <div>
          <div class="brand-title">EL Library</div>
          <div class="brand-subtitle">NEXT-GEN READING</div>
        </div>
      </div>
      <nav class="portal-nav">
        <button v-for="item in navItems" :key="item.key" class="nav-item" :class="{ active: activeTab === item.key }" @click="activeTab = item.key">
          {{ item.label }}
        </button>
      </nav>
      <div class="portal-user">
        <el-button v-if="canEnterAdmin" link type="primary" @click="router.push('/index')">后台管理</el-button>
        <el-avatar :src="userStore.avatar" :size="38" />
        <span class="user-name">{{ userStore.nickName }}</span>
      </div>
    </header>

    <div class="portal-grid">
      <main class="portal-main">
        <section v-if="activeTab === 'books'" class="hero-card">
          <div class="hero-copy">
            <p class="eyebrow">SMART BORROWING</p>
            <h1>在线借阅，把图书馆搬到你的学习流程里。</h1>
            <p class="hero-desc">
              实时查看可借库存、快速完成借书与归还，并同步跟踪续借、逾期和罚金情况。
            </p>
          </div>
          <div class="hero-metrics">
            <div class="metric-block">
              <span>可借图书</span>
              <strong>{{ bookStats.available }}</strong>
            </div>
            <div class="metric-block">
              <span>当前借阅</span>
              <strong>{{ summary.borrowingCount || 0 }}</strong>
            </div>
            <div class="metric-block">
              <span>待缴罚金</span>
              <strong>{{ formatMoney(summary.pendingFineAmount) }}</strong>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'books'" class="content-card">
          <div class="section-head">
            <div>
              <p class="eyebrow">BOOK COLLECTION</p>
              <h2>借阅图书</h2>
            </div>
            <div class="filter-row">
              <el-input v-model="bookQuery.bookName" placeholder="搜索图书名称" clearable class="filter-input" @keyup.enter="loadBooks" />
              <el-input v-model="bookQuery.author" placeholder="搜索作者" clearable class="filter-input" @keyup.enter="loadBooks" />
              <el-button type="primary" @click="loadBooks">搜索</el-button>
            </div>
          </div>

          <div class="book-grid">
            <article v-for="book in books" :key="book.bookId" class="book-card">
              <div class="book-cover">
                <img v-if="resolveFile(book.cover)" :src="resolveFile(book.cover)" :alt="book.bookName" />
                <div v-else class="book-cover-fallback">{{ (book.bookName || '书').slice(0, 1) }}</div>
              </div>
              <div class="book-meta">
                <p class="book-category">{{ book.category || '综合分类' }}</p>
                <h3>{{ book.bookName }}</h3>
                <p class="book-author">{{ book.author }}</p>
                <p class="book-desc">{{ book.description || '这本书暂未填写简介。' }}</p>
                <div class="book-footer">
                  <div>
                    <span class="stock-label">可借库存</span>
                    <strong>{{ book.availableStock }}</strong>
                  </div>
                  <el-button type="success" :disabled="book.availableStock <= 0 || borrowingAction" @click="handleBorrow(book)">在线借书</el-button>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section v-if="activeTab === 'records'" class="content-card">
          <div class="section-head">
            <div>
              <p class="eyebrow">BORROW LOG</p>
              <h2>我的借阅记录</h2>
            </div>
            <el-button plain @click="loadRecords">刷新记录</el-button>
          </div>
          <el-table :data="records" v-loading="recordLoading">
            <el-table-column label="图书名称" prop="bookName" min-width="180" show-overflow-tooltip />
            <el-table-column label="借阅时间" prop="borrowDate" min-width="160" />
            <el-table-column label="应还时间" prop="dueDate" min-width="160" />
            <el-table-column label="续借次数" prop="renewCount" width="100" />
            <el-table-column label="逾期天数" prop="overdueDays" width="100" />
            <el-table-column label="罚金" prop="fineAmount" width="100">
              <template #default="scope">
                {{ formatMoney(scope.row.fineAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="120">
              <template #default="scope">
                <dict-tag :options="library_borrow_status" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.status === '0'" link type="primary" @click="handleRenew(scope.row)">在线续借</el-button>
                <el-button v-if="scope.row.status !== '1'" link type="primary" @click="handleReturn(scope.row)">在线还书</el-button>
                <span v-if="scope.row.status === '1'" class="muted">已完成</span>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section v-if="activeTab === 'profile'" class="content-card">
          <div class="section-head">
            <div>
              <p class="eyebrow">PROFILE CENTER</p>
              <h2>个人资料</h2>
            </div>
          </div>
          <div class="profile-grid">
            <div class="profile-card">
              <h3>基础资料</h3>
              <el-form :model="profileForm" label-width="88px">
                <el-form-item label="头像">
                  <div class="avatar-row">
                    <el-avatar :src="userStore.avatar" :size="76" />
                    <el-upload :show-file-list="false" :http-request="uploadUserAvatar" accept="image/*">
                      <el-button type="primary" plain>更换头像</el-button>
                    </el-upload>
                  </div>
                </el-form-item>
                <el-form-item label="姓名">
                  <el-input v-model="profileForm.nickName" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phonenumber" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-select v-model="profileForm.sex" style="width: 100%">
                    <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存资料</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div class="profile-card">
              <h3>账户安全</h3>
              <el-form :model="pwdForm" label-width="88px">
                <el-form-item label="旧密码">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="success" @click="savePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </section>
      </main>

      <aside class="portal-side">
        <section class="side-card">
          <p class="eyebrow">READER STATUS</p>
          <h3>{{ readerInfo?.nickName || userStore.nickName }}</h3>
          <div class="side-list">
            <div>
              <span>读者证号</span>
              <strong>{{ readerInfo?.readerNo || '未开通' }}</strong>
            </div>
            <div>
              <span>借书证号</span>
              <strong>{{ readerInfo?.cardNo || '未设置' }}</strong>
            </div>
            <div>
              <span>借阅上限</span>
              <strong>{{ readerInfo?.borrowLimit || 0 }}</strong>
            </div>
            <div>
              <span>当前借阅</span>
              <strong>{{ readerInfo?.currentBorrowCount || 0 }}</strong>
            </div>
          </div>
        </section>

        <section class="side-card">
          <p class="eyebrow">REALTIME STATUS</p>
          <h3>借阅摘要</h3>
          <div class="summary-stack">
            <div class="summary-item">
              <span>借阅中</span>
              <strong>{{ summary.borrowingCount || 0 }}</strong>
            </div>
            <div class="summary-item">
              <span>已归还</span>
              <strong>{{ summary.returnedCount || 0 }}</strong>
            </div>
            <div class="summary-item danger">
              <span>已逾期</span>
              <strong>{{ summary.overdueCount || 0 }}</strong>
            </div>
            <div class="summary-item">
              <span>待缴罚金</span>
              <strong>{{ formatMoney(summary.pendingFineAmount) }}</strong>
            </div>
          </div>
        </section>

        <section class="side-card">
          <p class="eyebrow">LATEST ACTIVITY</p>
          <h3>最近借阅</h3>
          <div v-if="records.length" class="activity-list">
            <div v-for="item in records.slice(0, 3)" :key="item.recordId" class="activity-item">
              <strong>{{ item.bookName }}</strong>
              <span>{{ item.borrowDate }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无借阅记录" :image-size="72" />
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup name="LibraryPortal">
import { ElMessage } from "element-plus";
import { listPortalBook } from "@/api/library/book";
import { borrowBook, listMyBorrow, renewMyBorrow, returnMyBorrow } from "@/api/library/borrow";
import { getPortalSummary } from "@/api/library/portal";
import { getUserProfile, updateUserProfile, updateUserPwd, uploadAvatar } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

const router = useRouter();
const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const { library_borrow_status, sys_user_sex } = proxy.useDict("library_borrow_status", "sys_user_sex");

const navItems = [
  { key: "books", label: "图书借阅" },
  { key: "records", label: "我的借阅" },
  { key: "profile", label: "个人资料" }
];

const activeTab = ref("books");
const books = ref([]);
const records = ref([]);
const readerInfo = ref(null);
const summary = ref({});
const borrowingAction = ref(false);
const recordLoading = ref(false);

const bookQuery = reactive({
  pageNum: 1,
  pageSize: 12,
  bookName: "",
  author: ""
});

const profileForm = reactive({
  nickName: "",
  phonenumber: "",
  email: "",
  sex: "0"
});

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const canEnterAdmin = computed(() => userStore.roles.includes("admin") || userStore.roles.includes("librarian"));
const bookStats = computed(() => ({
  available: books.value.reduce((sum, item) => sum + (item.availableStock || 0), 0)
}));

function formatMoney(value) {
  return Number(value || 0).toFixed(2);
}

function resolveFile(url) {
  if (!url) return "";
  return /^https?:/.test(url) ? url : import.meta.env.VITE_APP_BASE_API + url;
}

function syncProfile(user) {
  profileForm.nickName = user?.nickName || userStore.nickName;
  profileForm.phonenumber = user?.phonenumber || "";
  profileForm.email = user?.email || "";
  profileForm.sex = user?.sex || "0";
}

function loadBooks() {
  return listPortalBook(bookQuery).then(response => {
    books.value = response.rows || [];
  });
}

function loadRecords() {
  recordLoading.value = true;
  return listMyBorrow({ pageNum: 1, pageSize: 50 }).then(response => {
    records.value = response.rows || [];
  }).finally(() => {
    recordLoading.value = false;
  });
}

function loadSummary() {
  return getPortalSummary().then(response => {
    summary.value = response.data.summary || {};
    readerInfo.value = response.data.reader;
  });
}

function loadUserProfile() {
  return getUserProfile().then(response => {
    syncProfile(response.data);
  });
}

function refreshAll() {
  Promise.all([loadBooks(), loadRecords(), loadSummary(), loadUserProfile()]);
}

function handleBorrow(book) {
  borrowingAction.value = true;
  borrowBook(book.bookId).then(() => {
    proxy.$modal.msgSuccess(`已成功借阅《${book.bookName}》`);
    refreshAll();
  }).finally(() => {
    borrowingAction.value = false;
  });
}

function handleReturn(row) {
  returnMyBorrow(row.recordId).then(() => {
    proxy.$modal.msgSuccess("归还成功");
    refreshAll();
  });
}

function handleRenew(row) {
  renewMyBorrow(row.recordId).then(() => {
    proxy.$modal.msgSuccess("续借成功");
    refreshAll();
  });
}

function saveProfile() {
  updateUserProfile(profileForm).then(() => {
    proxy.$modal.msgSuccess("个人资料已更新");
    return userStore.getInfo();
  }).then(() => {
    return loadUserProfile();
  });
}

function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning("请完整填写密码信息");
    return;
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning("两次输入的新密码不一致");
    return;
  }
  updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword).then(() => {
    proxy.$modal.msgSuccess("密码修改成功");
    pwdForm.oldPassword = "";
    pwdForm.newPassword = "";
    pwdForm.confirmPassword = "";
  });
}

function uploadUserAvatar(option) {
  const formData = new FormData();
  formData.append("avatarfile", option.file);
  uploadAvatar(formData).then(() => {
    proxy.$modal.msgSuccess("头像更新成功");
    return userStore.getInfo();
  }).then(() => {
    option.onSuccess();
  }).catch(error => {
    option.onError(error);
  });
}

refreshAll();
</script>

<style scoped lang="scss">
.portal-shell {
  --portal-primary: #0891b2;
  --portal-secondary: #22d3ee;
  --portal-accent: #22c55e;
  --portal-bg: #f6feff;
  --portal-text: #123b4b;
  --portal-muted: #6d8a96;
  min-height: calc(100vh - 84px);
  padding: 18px 20px 32px;
  background:
    radial-gradient(circle at top right, rgba(34, 211, 238, 0.18), transparent 28%),
    linear-gradient(180deg, #ffffff 0%, var(--portal-bg) 100%);
  color: var(--portal-text);
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 18px 24px;
  margin-bottom: 22px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(8, 145, 178, 0.12);
  border-radius: 24px;
  backdrop-filter: blur(12px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--portal-primary), #2563eb);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.brand-title,
.hero-card h1,
.section-head h2,
.side-card h3 {
  font-family: "Baloo 2", "Microsoft YaHei", sans-serif;
}

.brand-title {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.brand-subtitle,
.eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #83a6b1;
}

.portal-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-item {
  height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--portal-muted);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-item.active,
.nav-item:hover {
  background: rgba(8, 145, 178, 0.08);
  color: var(--portal-primary);
}

.portal-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 700;
}

.portal-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.portal-main,
.portal-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.content-card,
.side-card,
.profile-card {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(8, 145, 178, 0.12);
  border-radius: 24px;
  box-shadow: 0 16px 40px rgba(18, 59, 75, 0.06);
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 24px;
  padding: 30px;
}

.hero-card h1 {
  margin: 10px 0 14px;
  font-size: 42px;
  line-height: 1.1;
}

.hero-desc {
  margin: 0;
  color: var(--portal-muted);
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  gap: 16px;
}

.metric-block,
.summary-item,
.side-list div,
.activity-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: #f8fdff;
  border: 1px solid rgba(8, 145, 178, 0.1);
}

.metric-block span,
.summary-item span,
.side-list span,
.activity-item span {
  display: block;
  color: var(--portal-muted);
  font-size: 13px;
}

.metric-block strong,
.summary-item strong,
.side-list strong,
.activity-item strong {
  display: block;
  margin-top: 6px;
  font-size: 24px;
  font-weight: 800;
}

.content-card,
.side-card,
.profile-card {
  padding: 24px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 8px 0 0;
  font-size: 30px;
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-input {
  width: 220px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 18px;
}

.book-card {
  overflow: hidden;
  border: 1px solid rgba(8, 145, 178, 0.12);
  border-radius: 20px;
  background: #fff;
}

.book-cover {
  height: 220px;
  background: linear-gradient(135deg, rgba(8, 145, 178, 0.1), rgba(34, 197, 94, 0.12));
  display: grid;
  place-items: center;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-cover-fallback {
  width: 88px;
  height: 88px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--portal-primary), var(--portal-secondary));
  color: #fff;
  font-size: 34px;
  font-weight: 800;
}

.book-meta {
  padding: 20px;
}

.book-meta h3 {
  margin: 8px 0;
  font-size: 22px;
}

.book-category,
.book-author,
.book-desc,
.stock-label,
.muted {
  color: var(--portal-muted);
}

.book-desc {
  min-height: 44px;
  line-height: 1.7;
}

.book-footer {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.profile-card h3 {
  margin-top: 0;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.summary-stack,
.side-list,
.activity-list {
  display: grid;
  gap: 12px;
}

.summary-item.danger strong {
  color: #dc2626;
}

@media (max-width: 1100px) {
  .portal-grid,
  .hero-card,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .portal-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .portal-shell {
    padding: 12px;
  }

  .portal-nav {
    width: 100%;
    overflow-x: auto;
  }

  .filter-row,
  .portal-user {
    width: 100%;
  }

  .filter-input {
    width: 100%;
  }
}
</style>
