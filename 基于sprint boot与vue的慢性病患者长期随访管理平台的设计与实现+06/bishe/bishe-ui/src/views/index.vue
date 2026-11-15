<template>
  <div class="dashboard-container">
    <!-- Top Statistics Cards -->
    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-people">
            <svg-icon icon-class="peoples" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">患者总数</div>
            <div class="card-panel-num">{{ stats.patientCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-message">
            <svg-icon icon-class="user" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">在职医生</div>
            <div class="card-panel-num">{{ stats.doctorCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-money">
            <svg-icon icon-class="bug" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">健康预警</div>
            <div class="card-panel-num">{{ stats.warningCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper icon-shopping">
            <svg-icon icon-class="form" class-name="card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">今日随访</div>
            <div class="card-panel-num">{{ stats.todayFollowups }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Main Charts -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :lg="16">
        <div class="chart-wrapper">
          <div class="chart-title">近7日患者增长趋势</div>
          <div id="lineChart" style="width: 100%; height: 350px;"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="8">
        <div class="chart-wrapper">
          <div class="chart-title">预警等级分布</div>
          <div id="pieChart" style="width: 100%; height: 350px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Recent Warnings Table -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <div class="chart-title">最新健康预警</div>
          <el-table :data="stats.recentWarnings" style="width: 100%; padding-top: 15px;">
            <el-table-column label="预警时间" width="180" prop="createTime" align="center">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="患者姓名" width="180" prop="patientName" align="center" />
            <el-table-column label="预警类型" prop="warningType" align="center" />
            <el-table-column label="预警等级" align="center">
              <template #default="scope">
                <el-tag type="danger" v-if="scope.row.warningLevel == '3'">高危</el-tag>
                <el-tag type="warning" v-else-if="scope.row.warningLevel == '2'">中危</el-tag>
                <el-tag type="info" v-else>低危</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="异常值" prop="warningValue" align="center" />
            <el-table-column label="提示信息" prop="warningMsg" align="center" />
            <el-table-column label="状态" align="center">
              <template #default="scope">
                <el-tag type="danger" v-if="scope.row.isRead == '0'">未读</el-tag>
                <el-tag type="success" v-else>已读</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- Warning Dialog for Patients -->
    <el-dialog title="健康预警提醒" v-model="warningOpen" width="500px" center :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <div style="text-align: center; color: red; font-size: 16px; margin-bottom: 20px;">
        <svg-icon icon-class="bug" style="font-size: 40px; margin-bottom: 10px; display: block; margin: 0 auto 10px;" />
        您有新的健康预警消息，请及时查看！
      </div>
      <el-table :data="unreadWarnings" border>
        <el-table-column label="类型" prop="warningType" align="center" />
        <el-table-column label="异常值" prop="warningValue" align="center" />
        <el-table-column label="建议" prop="warningMsg" align="center" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleMarkRead">我已知晓 (标记为已读)</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import * as echarts from 'echarts';
import { getDashboardStats } from '@/api/index';
import { changeWarningReadStatus } from '@/api/health/warning';
import useUserStore from "@/store/modules/user";

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const stats = ref({
  patientCount: 0,
  doctorCount: 0,
  warningCount: 0,
  todayFollowups: 0,
  recentWarnings: [],
  pieData: {},
  trendDates: [],
  trendData: []
});

const warningOpen = ref(false);
const unreadWarnings = ref([]);

function initCharts() {
  // Line Chart
  const lineChart = echarts.init(document.getElementById('lineChart'));
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: stats.value.trendDates
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增患者',
        type: 'line',
        stack: 'Total',
        data: stats.value.trendData,
        smooth: true,
        itemStyle: { color: '#40c9c6' },
        areaStyle: { color: '#f0f9eb' }
      }
    ]
  });

  // Pie Chart
  const pieChart = echarts.init(document.getElementById('pieChart'));
  
  const pieDataArray = [
    { value: stats.value.pieData['1'] || 0, name: '低风险' },
    { value: stats.value.pieData['2'] || 0, name: '中风险' },
    { value: stats.value.pieData['3'] || 0, name: '高风险' }
  ];

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [
      {
        name: '预警分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: '20', fontWeight: 'bold' }
        },
        data: pieDataArray
      }
    ]
  });
  
  window.addEventListener('resize', () => {
    lineChart.resize();
    pieChart.resize();
  });
}

function fetchData() {
  getDashboardStats().then(response => {
    stats.value = response.data;
    
    // Check for unread warnings if current user is a patient or doctor
    // Note: The backend already filters 'recentWarnings' by userId for patients or doctorId for doctors.
    // We check if any of them are unread.
    const canShowWarning = !userStore.roles.includes("admin");
    if (canShowWarning) {
        // Find ALL unread warnings (backend returns 5 recent, which is good enough for alert, or we can iterate)
        const unread = stats.value.recentWarnings.filter(w => w.isRead == '0');
        if (unread.length > 0) {
            unreadWarnings.value = unread;
            warningOpen.value = true;
        }
    }

    nextTick(() => {
      initCharts();
    });
  });
}

function handleMarkRead() {
    const promises = unreadWarnings.value.map(w => changeWarningReadStatus(w.warningId, '1'));
    Promise.all(promises).then(() => {
        proxy.$modal.msgSuccess("已标记为已读");
        warningOpen.value = false;
        // Refresh data to update the table
        fetchData();
    });
}

onMounted(() => {
  fetchData();
});
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 32px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  }

  .chart-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 4px solid #40c9c6;
  }
}

.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);
    border-radius: 8px;

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}
</style>