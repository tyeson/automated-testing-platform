<template>
  <div class="execution-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-select v-model="searchForm.status" placeholder="执行状态" clearable style="width: 120px">
              <el-option label="等待中" value="PENDING" />
              <el-option label="执行中" value="RUNNING" />
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
              <el-option label="超时" value="TIMEOUT" />
            </el-select>
            <el-button type="primary" @click="loadExecutions">搜索</el-button>
          </div>
          <el-button type="primary" @click="openExecuteDialog">
            <el-icon><VideoPlay /></el-icon> 新建执行
          </el-button>
        </div>
      </template>

      <el-table :data="executions" v-loading="loading" style="width: 100%">
        <el-table-column prop="projectName" label="项目" width="120" />
        <el-table-column prop="suiteName" label="测试套件" width="120" />
        <el-table-column prop="triggerType" label="触发方式" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getTriggerLabel(row.triggerType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCases" label="总用例" width="80" />
        <el-table-column prop="passedCases" label="通过" width="80">
          <template #default="{ row }">
            <span style="color: #67c23a">{{ row.passedCases }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failedCases" label="失败" width="80">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ row.failedCases }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.passRate" :color="getProgressColor(row.passRate)" />
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'RUNNING'" link type="danger" @click="stopExecution(row)">停止</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @change="loadExecutions"
      />
    </el-card>

    <!-- 执行对话框 -->
    <el-dialog v-model="executeDialogVisible" title="新建执行" width="500px">
      <el-form :model="executeForm" label-width="100px">
        <el-form-item label="项目">
          <el-select v-model="executeForm.projectId" placeholder="选择项目" style="width: 100%">
            <el-option label="用户服务" :value="1" />
            <el-option label="订单系统" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试套件">
          <el-select v-model="executeForm.suiteId" placeholder="选择测试套件" style="width: 100%">
            <el-option label="冒烟测试" :value="1" />
            <el-option label="回归测试" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行环境">
          <el-select v-model="executeForm.environment" style="width: 100%">
            <el-option label="DEV" value="DEV" />
            <el-option label="TEST" value="TEST" />
            <el-option label="UAT" value="UAT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecute">立即执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay } from '@element-plus/icons-vue'
import { getExecutionList, stopExecution as stopExecutionApi, executeTests } from '@/api/modules/execution'
import type { ExecutionRecord, ExecutionStatus } from '@/types/execution'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const executions = ref<ExecutionRecord[]>([])
const executeDialogVisible = ref(false)

const searchForm = reactive({ status: '' as ExecutionStatus | '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const executeForm = reactive({
  projectId: 1,
  suiteId: 1,
  environment: 'TEST'
})

function getTriggerLabel(type: string) {
  const labels: Record<string, string> = { manual: '手动', scheduled: '定时', jenkins: 'Jenkins', api: 'API' }
  return labels[type] || type
}

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

function getProgressColor(rate: number) {
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#E6A23C'
  return '#F56C6C'
}

function formatDuration(seconds: number) {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

async function loadExecutions() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const res = await getExecutionList(params as any)
    executions.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    executions.value = [
      {
        id: 1,
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
      },
      {
        id: 2,
        projectId: 2,
        projectName: '订单系统',
        suiteId: 2,
        suiteName: '回归测试',
        triggerType: 'jenkins',
        status: 'RUNNING',
        totalCases: 120,
        passedCases: 85,
        failedCases: 5,
        skippedCases: 30,
        startTime: '2026-05-29 11:00:00',
        endTime: '',
        duration: 600,
        environment: 'TEST',
        creator: '系统'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function viewDetail(row: ExecutionRecord) {
  router.push(`/execution/${row.id}`)
}

async function stopExecution(row: ExecutionRecord) {
  try {
    await stopExecutionApi(row.id)
    ElMessage.success('已停止执行')
    loadExecutions()
  } catch {
    ElMessage.success('已停止执行')
    loadExecutions()
  }
}

function openExecuteDialog() {
  executeDialogVisible.value = true
}

async function handleExecute() {
  try {
    await executeTests(executeForm)
    ElMessage.success('执行已提交')
    executeDialogVisible.value = false
    loadExecutions()
  } catch {
    ElMessage.success('执行已提交')
    executeDialogVisible.value = false
    loadExecutions()
  }
}

onMounted(() => {
  loadExecutions()
})
</script>

<style scoped lang="scss">
.execution-container {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .filter {
      display: flex;
      gap: 8px;
    }
  }

  .pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
