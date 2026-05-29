<template>
  <div class="report-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalReports || 0 }}</div>
            <div class="stat-label">报告总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value" style="color: #67c23a">{{ stats.avgPassRate || 0 }}%</div>
            <div class="stat-label">平均通过率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCases || 0 }}</div>
            <div class="stat-label">总执行用例</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value" style="color: #f56c6c">{{ stats.totalFailures || 0 }}</div>
            <div class="stat-label">失败用例</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势分析图表 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <div class="flex-between">
          <span>质量趋势分析</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrendData">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="14">近14天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendChartRef" class="chart"></div>
    </el-card>

    <!-- 环境分析和缺陷分析 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>环境稳定性分析</template>
          <div ref="envChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>失败原因分析</template>
          <div ref="failureChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 报告列表 -->
    <el-card style="margin-top: 16px">
      <template #header>执行报告列表</template>
      <el-table :data="reports" v-loading="loading" style="width: 100%">
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="suiteName" label="测试套件" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.passRate >= 90 ? 'success' : row.passRate >= 70 ? 'warning' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCases" label="总用例" width="80" />
        <el-table-column prop="passedCases" label="通过" width="80" />
        <el-table-column prop="failedCases" label="失败" width="80" />
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.passRate" :color="row.passRate >= 90 ? '#67C23A' : row.passRate >= 70 ? '#E6A23C' : '#F56C6C'" />
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100" />
        <el-table-column prop="startTime" label="执行时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { getReportList, getTrendData, getEnvironmentAnalysis, getFailureAnalysis } from '@/api/modules/report'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const reports = ref<any[]>([])
const trendDays = ref(14)
const stats = ref({ totalReports: 156, avgPassRate: 91.5, totalCases: 12480, totalFailures: 1056 })

const trendChartRef = ref<HTMLDivElement>()
const envChartRef = ref<HTMLDivElement>()
const failureChartRef = ref<HTMLDivElement>()
let trendChart: ECharts | null = null
let envChart: ECharts | null = null
let failureChart: ECharts | null = null

async function loadReports() {
  loading.value = true
  try {
    const res = await getReportList({ page: 1, pageSize: 10 })
    reports.value = res.data?.records || []
  } catch {
    reports.value = [
      {
        id: 1,
        projectName: '用户服务',
        suiteName: '回归测试',
        status: 'SUCCESS',
        totalCases: 120,
        passedCases: 115,
        failedCases: 5,
        passRate: 95,
        duration: '15分30秒',
        startTime: '2026-05-29 10:30'
      },
      { id: 2, projectName: '订单系统', suiteName: '冒烟测试', status: 'FAILED', totalCases: 25, passedCases: 20, failedCases: 5, passRate: 80, duration: '5分20秒', startTime: '2026-05-29 09:15' }
    ]
  } finally {
    loading.value = false
  }
}

async function loadTrendData() {
  try {
    const res = await getTrendData(trendDays.value)
    const data = res.data || generateMockTrendData()

    trendChart?.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['通过率', '执行数'] },
      xAxis: { type: 'category', data: data.map((d: any) => d.date) },
      yAxis: [
        { type: 'value', name: '通过率(%)', min: 0, max: 100 },
        { type: 'value', name: '执行数' }
      ],
      series: [
        { name: '通过率', type: 'line', yAxisIndex: 0, data: data.map((d: any) => d.passRate), smooth: true, itemStyle: { color: '#67C23A' } },
        { name: '执行数', type: 'bar', yAxisIndex: 1, data: data.map((d: any) => d.total), itemStyle: { color: '#409EFF' } }
      ]
    })
  } catch {
    // 使用模拟数据
  }
}

function generateMockTrendData() {
  const data = []
  for (let i = trendDays.value - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    data.push({
      date: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }),
      total: Math.floor(Math.random() * 100) + 50,
      passRate: Math.floor(Math.random() * 20) + 80
    })
  }
  return data
}

function loadEnvChart() {
  envChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['DEV', 'TEST', 'UAT', 'PROD'] },
    yAxis: { type: 'value', name: '稳定性(%)' },
    series: [
      {
        type: 'bar',
        data: [
          { value: 85, itemStyle: { color: '#E6A23C' } },
          { value: 95, itemStyle: { color: '#67C23A' } },
          { value: 92, itemStyle: { color: '#67C23A' } },
          { value: 98, itemStyle: { color: '#67C23A' } }
        ]
      }
    ]
  })
}

function loadFailureChart() {
  failureChart?.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 45, name: '元素定位失败', itemStyle: { color: '#F56C6C' } },
          { value: 30, name: '网络超时', itemStyle: { color: '#E6A23C' } },
          { value: 15, name: '数据异常', itemStyle: { color: '#409EFF' } },
          { value: 10, name: '环境问题', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  })
}

function viewDetail(row: any) {
  router.push(`/report/${row.id}`)
}

onMounted(() => {
  trendChart = echarts.init(trendChartRef.value)
  envChart = echarts.init(envChartRef.value)
  failureChart = echarts.init(failureChartRef.value)

  loadReports()
  loadTrendData()
  loadEnvChart()
  loadFailureChart()

  window.addEventListener('resize', () => {
    trendChart?.resize()
    envChart?.resize()
    failureChart?.resize()
  })
})

onBeforeUnmount(() => {
  trendChart?.dispose()
  envChart?.dispose()
  failureChart?.dispose()
})
</script>

<style scoped lang="scss">
.report-container {
  .stat-card {
    .stat-content {
      text-align: center;

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #303133;
      }

      .stat-label {
        color: #909399;
        margin-top: 8px;
      }
    }
  }

  .chart {
    width: 100%;
    height: 300px;
  }
}
</style>
