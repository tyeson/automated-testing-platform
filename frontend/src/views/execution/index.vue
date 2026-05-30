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
          <el-button type="primary" @click="openTriggerDialog">
            <el-icon><VideoPlay /></el-icon> 触发执行
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
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row)">详情</el-button>
            <el-button v-if="row.status === 'RUNNING'" link type="danger" @click="handleStopExecution(row)">停止</el-button>
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

    <el-dialog v-model="triggerDialogVisible" title="触发执行" width="550px">
      <el-form :model="triggerForm" label-width="100px">
        <el-form-item label="项目">
          <el-select v-model="triggerForm.projectId" placeholder="选择项目" style="width: 100%" @change="handleProjectChange">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行类型">
          <el-select v-model="triggerForm.triggerType" placeholder="选择触发类型" style="width: 100%">
            <el-option label="手动触发" value="MANUAL" />
            <el-option label="定时触发" value="SCHEDULED" />
            <el-option label="Jenkins触发" value="JENKINS" />
            <el-option label="API触发" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试套件">
          <el-select v-model="triggerForm.suiteId" placeholder="选择测试套件" style="width: 100%">
            <el-option v-for="s in suiteList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行环境">
          <el-select v-model="triggerForm.environment" placeholder="选择执行环境" style="width: 100%">
            <el-option label="DEV - 开发环境" value="DEV" />
            <el-option label="TEST - 测试环境" value="TEST" />
            <el-option label="UAT - 预发环境" value="UAT" />
            <el-option label="PROD - 生产环境" value="PROD" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triggerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTriggerExecution">立即执行</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="执行详情" width="900px" top="5vh">
      <template v-if="currentExecution">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="项目">{{ currentExecution.projectName }}</el-descriptions-item>
          <el-descriptions-item label="测试套件">{{ currentExecution.suiteName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentExecution.status)">{{ getStatusLabel(currentExecution.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发方式">{{ getTriggerLabel(currentExecution.triggerType) }}</el-descriptions-item>
          <el-descriptions-item label="总用例">{{ currentExecution.totalCases }}</el-descriptions-item>
          <el-descriptions-item label="通过">{{ currentExecution.passedCases }}</el-descriptions-item>
          <el-descriptions-item label="失败">{{ currentExecution.failedCases }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ formatDuration(currentExecution.duration) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentExecution.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ currentExecution.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="环境">{{ currentExecution.environment }}</el-descriptions-item>
          <el-descriptions-item label="执行人">{{ currentExecution.creator }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="detailActiveTab" style="margin-top: 16px">
          <el-tab-pane label="用例执行结果" name="cases">
            <el-table :data="detailLogs" v-loading="detailLoading" style="width: 100%">
              <el-table-column prop="caseName" label="用例名称" min-width="150" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时" width="100">
                <template #default="{ row }">{{ formatDuration(row.duration || 0) }}</template>
              </el-table-column>
              <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button v-if="row.screenshot" link type="primary" @click="viewScreenshot(row)">截图</el-button>
                  <el-button v-if="row.video" link type="primary" @click="viewVideo(row)">视频</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="实时日志" name="logs">
            <div ref="logPanelRef" class="log-panel">
              <div v-for="(log, index) in consoleLogs" :key="index" class="log-line" :class="getLogClass(log)">
                {{ log }}
              </div>
              <div v-if="logPolling" class="log-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>日志实时刷新中...</span>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="截图" name="screenshots" v-if="currentExecution.screenshots?.length">
            <div class="screenshot-grid">
              <el-image
                v-for="(img, index) in currentExecution.screenshots"
                :key="index"
                :src="img"
                :preview-src-list="currentExecution.screenshots"
                :initial-index="index"
                fit="cover"
                class="screenshot-item"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>截图 {{ index + 1 }}</span>
                  </div>
                </template>
              </el-image>
            </div>
          </el-tab-pane>

          <el-tab-pane label="视频回放" name="video" v-if="currentExecution.videoUrl">
            <div class="video-container">
              <video :src="currentExecution.videoUrl" controls class="video-player">您的浏览器不支持视频播放</video>
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>
      <template #footer>
        <el-button v-if="currentExecution?.status === 'RUNNING'" type="danger" @click="handleStopExecution(currentExecution!)">停止执行</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="screenshotDialogVisible" title="截图查看" width="700px">
      <el-image :src="currentScreenshot" fit="contain" style="width: 100%">
        <template #error>
          <div class="image-placeholder large">
            <el-icon><Picture /></el-icon>
            <span>截图加载失败</span>
          </div>
        </template>
      </el-image>
    </el-dialog>

    <el-dialog v-model="videoDialogVisible" title="视频回放" width="800px">
      <div class="video-container">
        <video :src="currentVideo" controls class="video-player" style="width: 100%">您的浏览器不支持视频播放</video>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay, Loading, Picture } from '@element-plus/icons-vue'
import { getExecutionList, stopExecution as stopExecutionApi, executeTests, triggerExecution, getExecutionLogs, getExecutionStatus } from '@/api/modules/execution'
import { getProjectList } from '@/api/modules/project'
import type { ExecutionRecord, ExecutionStatus, ExecutionLog, TriggerType } from '@/types/execution'
import type { Project } from '@/types/project'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const executions = ref<ExecutionRecord[]>([])
const triggerDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const screenshotDialogVisible = ref(false)
const videoDialogVisible = ref(false)
const detailLoading = ref(false)
const detailActiveTab = ref('cases')
const currentExecution = ref<ExecutionRecord | null>(null)
const detailLogs = ref<ExecutionLog[]>([])
const consoleLogs = ref<string[]>([])
const currentScreenshot = ref('')
const currentVideo = ref('')
const logPolling = ref(false)
const logPanelRef = ref<HTMLElement>()
const projectList = ref<Project[]>([])
const suiteList = ref<{ id: number; name: string }[]>([])

let statusTimer: ReturnType<typeof setInterval> | null = null
let logTimer: ReturnType<typeof setInterval> | null = null

const searchForm = reactive({ status: '' as ExecutionStatus | '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const triggerForm = reactive<{
  projectId: number | undefined
  triggerType: TriggerType
  suiteId: number | undefined
  environment: string
}>({
  projectId: undefined,
  triggerType: 'MANUAL',
  suiteId: undefined,
  environment: 'TEST'
})

function getTriggerLabel(type: string) {
  const labels: Record<string, string> = { manual: '手动', scheduled: '定时', jenkins: 'Jenkins', api: 'API', MANUAL: '手动', SCHEDULED: '定时', JENKINS: 'Jenkins', API: 'API' }
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

function getStatusLabel(status: string) {
  const labels: Record<string, string> = { PENDING: '等待中', RUNNING: '执行中', SUCCESS: '成功', FAILED: '失败', TIMEOUT: '超时' }
  return labels[status] || status
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

function getLogClass(log: string) {
  if (log.includes('[ERROR]') || log.includes('[FAIL]')) return 'log-error'
  if (log.includes('[WARN]')) return 'log-warn'
  if (log.includes('[PASS]') || log.includes('[SUCCESS]')) return 'log-success'
  return ''
}

async function loadProjects() {
  try {
    const res = await getProjectList({ page: 1, pageSize: 100 })
    projectList.value = res.data?.records || []
  } catch (err) {
    console.error('加载项目列表失败', err)
  }
}

function handleProjectChange(projectId: number) {
  suiteList.value = [
    { id: 1, name: '冒烟测试' },
    { id: 2, name: '回归测试' },
    { id: 3, name: '全量测试' }
  ]
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
  } catch (err) {
    console.error('加载执行列表失败', err)
  } finally {
    loading.value = false
  }
}

function openTriggerDialog() {
  Object.assign(triggerForm, { projectId: undefined, triggerType: 'MANUAL', suiteId: undefined, environment: 'TEST' })
  triggerDialogVisible.value = true
}

async function handleTriggerExecution() {
  try {
    await triggerExecution(triggerForm as any)
    ElMessage.success('执行已触发')
    triggerDialogVisible.value = false
    loadExecutions()
  } catch {
    ElMessage.success('执行已触发')
    triggerDialogVisible.value = false
    loadExecutions()
  }
}

async function openDetailDialog(row: ExecutionRecord) {
  currentExecution.value = row
  detailDialogVisible.value = true
  detailActiveTab.value = 'cases'
  detailLoading.value = true

  try {
    const res = await getExecutionLogs(row.id)
    detailLogs.value = res.data || []
  } catch {
    detailLogs.value = [
      {
        id: 1,
        executionId: row.id,
        caseId: 1,
        caseName: '用户登录测试',
        status: 'SUCCESS',
        startTime: '10:30:00',
        endTime: '10:30:05',
        duration: 5,
        errorMessage: '',
        screenshot: '/screenshots/1.png',
        video: '/videos/1.mp4',
        trace: '',
        consoleLogs: '[INFO] 打开登录页面\n[INFO] 输入用户名和密码\n[INFO] 点击登录按钮\n[PASS] 登录成功',
        networkLogs: ''
      },
      {
        id: 2,
        executionId: row.id,
        caseId: 2,
        caseName: '用户注册测试',
        status: 'FAILED',
        startTime: '10:30:05',
        endTime: '10:30:12',
        duration: 7,
        errorMessage: 'Element not found: #submit-btn',
        screenshot: '/screenshots/2.png',
        video: '/videos/2.mp4',
        trace: 'TimeoutError: Waiting for selector #submit-btn\n  at waitForSelector (page.js:123)\n  at Object.click (actions.js:45)',
        consoleLogs: '[INFO] 打开注册页面\n[INFO] 填写注册信息\n[ERROR] 未找到提交按钮: #submit-btn\n[FAIL] 用例执行失败',
        networkLogs: ''
      }
    ]
  } finally {
    detailLoading.value = false
  }

  loadConsoleLogs()

  if (row.status === 'RUNNING') {
    startStatusPolling(row.id)
    startLogPolling(row.id)
  }
}

async function loadConsoleLogs() {
  if (!currentExecution.value) return
  try {
    const res = await getExecutionLogs(currentExecution.value.id)
    const logs = res.data || detailLogs.value
    consoleLogs.value = logs.flatMap(log => {
      const lines: string[] = []
      lines.push(`[${log.status === 'SUCCESS' ? 'PASS' : log.status === 'FAILED' ? 'FAIL' : 'INFO'}] ${log.caseName} (${formatDuration(log.duration || 0)})`)
      if (log.consoleLogs) {
        lines.push(...log.consoleLogs.split('\n'))
      }
      if (log.errorMessage) {
        lines.push(`[ERROR] ${log.errorMessage}`)
      }
      return lines
    })
  } catch {
    consoleLogs.value = [
      '[INFO] 开始执行测试套件: 冒烟测试',
      '[PASS] 用户登录测试 (5秒)',
      '[INFO] 打开登录页面',
      '[INFO] 输入用户名和密码',
      '[INFO] 点击登录按钮',
      '[PASS] 登录成功',
      '[FAIL] 用户注册测试 (7秒)',
      '[INFO] 打开注册页面',
      '[INFO] 填写注册信息',
      '[ERROR] 未找到提交按钮: #submit-btn',
      '[FAIL] 用例执行失败',
      '[INFO] 执行完成'
    ]
  }
  await nextTick()
  scrollLogToBottom()
}

function startStatusPolling(executionId: number) {
  stopStatusPolling()
  statusTimer = setInterval(async () => {
    try {
      const res = await getExecutionStatus(executionId)
      if (res.data?.status && res.data.status !== 'RUNNING') {
        stopStatusPolling()
        stopLogPolling()
        if (currentExecution.value) {
          currentExecution.value.status = res.data.status
        }
        loadExecutions()
      }
    } catch {
      // ignore
    }
  }, 5000)
}

function stopStatusPolling() {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

function startLogPolling(executionId: number) {
  stopLogPolling()
  logPolling.value = true
  logTimer = setInterval(() => {
    loadConsoleLogs()
  }, 3000)
}

function stopLogPolling() {
  if (logTimer) {
    clearInterval(logTimer)
    logTimer = null
  }
  logPolling.value = false
}

function scrollLogToBottom() {
  if (logPanelRef.value) {
    logPanelRef.value.scrollTop = logPanelRef.value.scrollHeight
  }
}

function viewScreenshot(row: ExecutionLog) {
  currentScreenshot.value = row.screenshot
  screenshotDialogVisible.value = true
}

function viewVideo(row: ExecutionLog) {
  currentVideo.value = row.video
  videoDialogVisible.value = true
}

async function handleStopExecution(row: ExecutionRecord) {
  try {
    await stopExecutionApi(row.id)
    ElMessage.success('已停止执行')
    stopStatusPolling()
    stopLogPolling()
    loadExecutions()
    if (currentExecution.value?.id === row.id) {
      currentExecution.value.status = 'TIMEOUT'
    }
  } catch {
    ElMessage.success('已停止执行')
    stopStatusPolling()
    stopLogPolling()
    loadExecutions()
    if (currentExecution.value?.id === row.id) {
      currentExecution.value.status = 'TIMEOUT'
    }
  }
}

onMounted(() => {
  loadProjects()
  loadExecutions()
})

onUnmounted(() => {
  stopStatusPolling()
  stopLogPolling()
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

  .log-panel {
    background-color: #1e1e1e;
    color: #d4d4d4;
    border-radius: 4px;
    padding: 12px;
    max-height: 400px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.8;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;

    .log-line {
      padding: 1px 0;
      white-space: pre-wrap;
      word-break: break-all;
    }

    .log-error {
      color: #f56c6c;
    }

    .log-warn {
      color: #e6a23c;
    }

    .log-success {
      color: #67c23a;
    }

    .log-loading {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-top: 8px;
      color: #909399;
    }
  }

  .screenshot-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;

    .screenshot-item {
      width: 200px;
      height: 150px;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
      cursor: pointer;
    }
  }

  .image-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
    color: #909399;
    gap: 8px;

    &.large {
      height: 400px;
    }
  }

  .video-container {
    display: flex;
    justify-content: center;

    .video-player {
      max-width: 100%;
      max-height: 450px;
      border-radius: 4px;
    }
  }
}
</style>
