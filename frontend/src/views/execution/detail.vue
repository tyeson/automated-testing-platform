<template>
  <div class="execution-detail-container">
    <el-card>
      <template #header>
        <div class="flex-between">
          <span>执行详情 - {{ execution?.suiteName }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <!-- 执行概览 -->
      <el-descriptions :column="4" border class="overview">
        <el-descriptions-item label="项目">{{ execution?.projectName }}</el-descriptions-item>
        <el-descriptions-item label="测试套件">{{ execution?.suiteName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(execution?.status || '')">{{ execution?.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="触发方式">{{ execution?.triggerType }}</el-descriptions-item>
        <el-descriptions-item label="总用例数">{{ execution?.totalCases }}</el-descriptions-item>
        <el-descriptions-item label="通过">{{ execution?.passedCases }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ execution?.failedCases }}</el-descriptions-item>
        <el-descriptions-item label="通过率">{{ getPassRate() }}%</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ execution?.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ execution?.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ formatDuration(execution?.duration || 0) }}</el-descriptions-item>
        <el-descriptions-item label="环境">{{ execution?.environment }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 执行日志 -->
    <el-card style="margin-top: 16px">
      <template #header>执行日志</template>
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="caseName" label="用例名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">{{ formatDuration(row.duration || 0) }}</template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.screenshot" link type="primary">截图</el-button>
            <el-button v-if="row.video" link type="primary">视频</el-button>
            <el-button link type="primary">日志</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getExecutionDetail, getExecutionLogs } from '@/api/modules/execution'
import type { ExecutionRecord, ExecutionLog } from '@/types/execution'

const route = useRoute()
const loading = ref(false)
const execution = ref<ExecutionRecord | null>(null)
const logs = ref<ExecutionLog[]>([])

function getStatusType(status: string) {
  const types: Record<string, 'success' | 'danger' | 'warning' | 'info'> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    RUNNING: 'warning',
    TIMEOUT: 'info',
    PENDING: 'info'
  }
  return types[status] || 'info'
}

function getPassRate() {
  if (!execution.value || execution.value.totalCases === 0) return 0
  return Math.round((execution.value.passedCases / execution.value.totalCases) * 100)
}

function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

async function loadData() {
  loading.value = true
  const id = Number(route.params.id)
  try {
    const [detailRes, logsRes] = await Promise.all([getExecutionDetail(id), getExecutionLogs(id)])
    execution.value = detailRes.data
    logs.value = logsRes.data || []
  } catch {
    execution.value = {
      id,
      projectId: 1,
      projectName: '用户服务',
      suiteId: 1,
      suiteName: '冒烟测试',
      triggerType: 'manual',
      status: 'SUCCESS',
      totalCases: 25,
      passedCases: 24,
      failedCases: 1,
      skippedCases: 0,
      startTime: '2026-05-29 10:30:00',
      endTime: '2026-05-29 10:45:00',
      duration: 900,
      environment: 'TEST',
      creator: '张三'
    }
    logs.value = [
      {
        id: 1,
        executionId: id,
        caseId: 1,
        caseName: '用户登录测试',
        status: 'SUCCESS',
        startTime: '10:30:00',
        endTime: '10:30:05',
        duration: 5,
        errorMessage: '',
        screenshot: '',
        video: '',
        trace: '',
        consoleLogs: '',
        networkLogs: ''
      },
      {
        id: 2,
        executionId: id,
        caseId: 2,
        caseName: '用户注册测试',
        status: 'FAILED',
        startTime: '10:30:05',
        endTime: '10:30:12',
        duration: 7,
        errorMessage: 'Element not found: #submit-btn',
        screenshot: '/screenshots/1.png',
        video: '/videos/1.mp4',
        trace: '',
        consoleLogs: '',
        networkLogs: ''
      }
    ]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.execution-detail-container {
  .overview {
    margin-bottom: 16px;
  }
}
</style>
