<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ metrics.todayExecutions || 0 }}</div>
              <div class="stat-label">今日执行</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ metrics.successRate || 0 }}%</div>
              <div class="stat-label">成功率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ metrics.failRate || 0 }}%</div>
              <div class="stat-label">失败率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ metrics.projectCount || 0 }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>执行趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrendData">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="14">近14天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>执行状态分布</template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>项目执行排行</template>
          <div ref="barChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>最近执行记录</template>
          <el-table :data="recentExecutions" style="width: 100%" max-height="300">
            <el-table-column prop="projectName" label="项目" />
            <el-table-column prop="suiteName" label="套件" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="passRate" label="通过率" width="80" />
            <el-table-column prop="startTime" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { getDashboardMetrics, getTrendData } from '@/api/modules/report'
import { getExecutionList } from '@/api/modules/execution'
import { VideoPlay, CircleCheck, CircleClose, Files } from '@element-plus/icons-vue'

const metrics = ref({
  todayExecutions: 0,
  successRate: 0,
  failRate: 0,
  projectCount: 0
})

const trendDays = ref(7)
const trendChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()

let trendChart: ECharts | null = null
let pieChart: ECharts | null = null
let barChart: ECharts | null = null

const recentExecutions = ref<any[]>([])

function getStatusType(status: string): 'success' | 'danger' | 'warning' | 'info' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info'> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    RUNNING: 'warning',
    TIMEOUT: 'info',
    PENDING: 'info'
  }
  return map[status] || 'info'
}

async function loadDashboardMetrics() {
  try {
    const res = await getDashboardMetrics()
    const data = res.data as Record<string, number>
    if (data) {
      metrics.value = {
        todayExecutions: data.todayExecutions || 0,
        successRate: data.successRate || 0,
        failRate: data.failRate || 0,
        projectCount: data.projectCount || 0
      }
    }
  } catch {
    // 使用模拟数据
    metrics.value = {
      todayExecutions: 156,
      successRate: 92.5,
      failRate: 7.5,
      projectCount: 12
    }
  }
}

async function loadTrendData() {
  try {
    const res = await getTrendData(trendDays.value)
    const data = res.data || generateMockTrendData()

    if (trendChart) {
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['总执行', '通过', '失败'] },
        xAxis: { type: 'category', data: data.map((d: any) => d.date) },
        yAxis: { type: 'value' },
        series: [
          { name: '总执行', type: 'line', data: data.map((d: any) => d.total), smooth: true, itemStyle: { color: '#409EFF' } },
          { name: '通过', type: 'line', data: data.map((d: any) => d.passed), smooth: true, itemStyle: { color: '#67C23A' } },
          { name: '失败', type: 'line', data: data.map((d: any) => d.failed), smooth: true, itemStyle: { color: '#F56C6C' } }
        ]
      })
    }
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
      passed: Math.floor(Math.random() * 80) + 40,
      failed: Math.floor(Math.random() * 20) + 5,
      passRate: Math.floor(Math.random() * 20) + 80
    })
  }
  return data
}

function loadPieChart() {
  if (pieChart) {
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 856, name: '成功', itemStyle: { color: '#67C23A' } },
            { value: 124, name: '失败', itemStyle: { color: '#F56C6C' } },
            { value: 28, name: '超时', itemStyle: { color: '#E6A23C' } },
            { value: 12, name: '运行中', itemStyle: { color: '#409EFF' } }
          ]
        }
      ]
    })
  }
}

function loadBarChart() {
  if (barChart) {
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: ['用户服务', '订单系统', '支付模块', '消息中心', '数据平台'] },
      series: [
        {
          type: 'bar',
          data: [186, 142, 98, 76, 54],
          itemStyle: { color: '#409EFF' }
        }
      ]
    })
  }
}

async function loadRecentExecutions() {
  try {
    const res = await getExecutionList({ page: 1, pageSize: 10 })
    recentExecutions.value = res.data?.records || []
  } catch {
    // 使用模拟数据
    recentExecutions.value = [
      { projectName: '用户服务', suiteName: '回归测试', status: 'SUCCESS', passRate: '98%', startTime: '2026-05-29 10:30' },
      { projectName: '订单系统', suiteName: '冒烟测试', status: 'FAILED', passRate: '85%', startTime: '2026-05-29 09:15' }
    ]
  }
}

onMounted(() => {
  trendChart = echarts.init(trendChartRef.value)
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)

  loadDashboardMetrics()
  loadTrendData()
  loadPieChart()
  loadBarChart()
  loadRecentExecutions()

  window.addEventListener('resize', () => {
    trendChart?.resize()
    pieChart?.resize()
    barChart?.resize()
  })
})

onBeforeUnmount(() => {
  trendChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .stat-cards {
    margin-bottom: 16px;
  }

  .chart-row {
    margin-bottom: 16px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }

        .stat-label {
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart {
    width: 100%;
    height: 300px;
  }
}
</style>
