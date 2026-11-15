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
         <el-form-item label="测量时间" prop="measureTime">
            <el-date-picker clearable
               v-model="queryParams.measureTime"
               type="date"
               value-format="YYYY-MM-DD"
               placeholder="请选择测量时间">
            </el-date-picker>
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
               :plain="!isPatient"
               icon="Plus"
               @click="handleAdd"
               v-if="isPatient || $auth.hasPermi('health:data:add')"
            >{{ isPatient ? '上传健康数据' : '新增' }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-if="isPatient || $auth.hasPermi('health:data:edit')"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-if="isPatient || $auth.hasPermi('health:data:remove')"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-if="isPatient || $auth.hasPermi('health:data:export')"
            >导出</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="info"
               plain
               icon="TrendCharts"
               @click="handleChart"
               v-if="isPatient || $auth.hasPermi('health:data:list')"
            >趋势图</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="数据ID" align="center" prop="dataId" />
         <el-table-column label="患者姓名" align="center" prop="patientName" v-if="!isPatient" />
         <el-table-column label="身高(cm)" align="center" prop="height" />
         <el-table-column label="体重(kg)" align="center" prop="weight" />
         <el-table-column label="收缩压" align="center" prop="bloodPressureSys" />
         <el-table-column label="舒张压" align="center" prop="bloodPressureDia" />
         <el-table-column label="心率" align="center" prop="heartRate" />
         <el-table-column label="血糖" align="center" prop="bloodSugar" />
         <el-table-column label="体检报告" align="center" prop="reportImage" width="100">
            <template #default="scope">
               <image-preview :src="scope.row.reportImage" :width="50" :height="50"/>
            </template>
         </el-table-column>
         <el-table-column label="测量时间" align="center" prop="measureTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.measureTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="isPatient || $auth.hasPermi('health:data:edit')">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="isPatient || $auth.hasPermi('health:data:remove')">删除</el-button>
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

      <!-- 添加或修改健康数据采集对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="dataRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="患者ID" prop="userId" v-if="!isPatient">
               <el-input v-model="form.userId" placeholder="请输入患者ID" />
            </el-form-item>
            <el-form-item label="身高(cm)" prop="height">
               <el-input-number v-model="form.height" :precision="2" :step="0.1" placeholder="请输入身高" />
            </el-form-item>
            <el-form-item label="体重(kg)" prop="weight">
               <el-input-number v-model="form.weight" :precision="2" :step="0.1" placeholder="请输入体重" />
            </el-form-item>
            <el-form-item label="收缩压" prop="bloodPressureSys">
               <el-input-number v-model="form.bloodPressureSys" placeholder="请输入收缩压" />
            </el-form-item>
            <el-form-item label="舒张压" prop="bloodPressureDia">
               <el-input-number v-model="form.bloodPressureDia" placeholder="请输入舒张压" />
            </el-form-item>
            <el-form-item label="心率(bpm)" prop="heartRate">
               <el-input-number v-model="form.heartRate" placeholder="请输入心率" />
            </el-form-item>
            <el-form-item label="血糖" prop="bloodSugar">
               <el-input-number v-model="form.bloodSugar" :precision="2" :step="0.1" placeholder="请输入血糖" />
            </el-form-item>
            <el-form-item label="体检报告" prop="reportImage">
               <image-upload v-model="form.reportImage"/>
            </el-form-item>
            <el-form-item label="测量时间" prop="measureTime">
               <el-date-picker clearable
                  v-model="form.measureTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择测量时间">
               </el-date-picker>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 趋势图对话框 -->
      <el-dialog title="健康趋势图" v-model="chartOpen" width="800px" append-to-body @opened="initChart">
         <div id="healthChart" style="width: 100%; height: 400px;"></div>
      </el-dialog>
   </div>
</template>

<script setup name="Data">
import { listData, getData, delData, addData, updateData } from "@/api/health/data";
import useUserStore from "@/store/modules/user";
import * as echarts from 'echarts';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

const isPatient = computed(() => {
  return !userStore.roles.includes("admin") && !userStore.roles.includes("doctor");
});

const dataList = ref([]);
const open = ref(false);
const chartOpen = ref(false);
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
    measureTime: null,
  },
  rules: {
    userId: [
      { required: true, message: "患者ID不能为空", trigger: "blur" }
    ],
    measureTime: [
        { required: true, message: "测量时间不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询健康数据采集列表 */
function getList() {
  loading.value = true;
  listData(queryParams.value).then(response => {
    if (isPatient.value) {
       // Only show own records for patients
       dataList.value = response.rows.filter(item => item.userId == userStore.id);
    } else {
       dataList.value = response.rows;
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
    dataId: null,
    userId: null,
    height: null,
    weight: null,
    bloodPressureSys: null,
    bloodPressureDia: null,
    heartRate: null,
    bloodSugar: null,
    reportImage: null,
    measureTime: null,
    createTime: null,
    remark: null
  };
  proxy.resetForm("dataRef");
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
  ids.value = selection.map(item => item.dataId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加健康数据采集";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const dataId = row.dataId || ids.value
  getData(dataId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改健康数据采集";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dataRef"].validate(valid => {
    if (valid) {
      if (form.value.dataId != null) {
        updateData(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addData(form.value).then(response => {
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
  const dataIds = row.dataId || ids.value;
  proxy.$modal.confirm('是否确认删除健康数据采集编号为"' + dataIds + '"的数据项？').then(function() {
    return delData(dataIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('health/data/export', {
    ...queryParams.value
  }, `data_${new Date().getTime()}.xlsx`)
}

/** 打开趋势图 */
function handleChart() {
    if (dataList.value.length === 0) {
        proxy.$modal.msgWarning("暂无数据可展示");
        return;
    }
    chartOpen.value = true;
}

/** 初始化图表 */
function initChart() {
    const chartDom = document.getElementById('healthChart');
    const myChart = echarts.init(chartDom);
    
    // 按时间排序
    const sortedData = [...dataList.value].sort((a, b) => new Date(a.measureTime) - new Date(b.measureTime));
    
    const dates = sortedData.map(item => proxy.parseTime(item.measureTime, '{y}-{m}-{d}'));
    const sys = sortedData.map(item => item.bloodPressureSys);
    const dia = sortedData.map(item => item.bloodPressureDia);
    const sugar = sortedData.map(item => item.bloodSugar);

    const option = {
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['收缩压', '舒张压', '血糖']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dates
        },
        yAxis: [
            {
                type: 'value',
                name: '血压(mmHg)',
                position: 'left'
            },
            {
                type: 'value',
                name: '血糖(mmol/L)',
                position: 'right'
            }
        ],
        series: [
            {
                name: '收缩压',
                type: 'line',
                data: sys,
                yAxisIndex: 0
            },
            {
                name: '舒张压',
                type: 'line',
                data: dia,
                yAxisIndex: 0
            },
            {
                name: '血糖',
                type: 'line',
                data: sugar,
                yAxisIndex: 1
            }
        ]
    };

    myChart.setOption(option);
}

getList();
</script>