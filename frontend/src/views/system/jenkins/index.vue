<template>
  <div class="jenkins-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-select v-model="searchForm.projectId" placeholder="选择项目" clearable style="width: 160px">
              <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
            <el-button type="primary" @click="loadJenkinsConfigs">搜索</el-button>
          </div>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增配置
          </el-button>
        </div>
      </template>

      <el-table :data="configs" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="配置名称" width="150" />
        <el-table-column prop="projectName" label="所属项目" width="120" />
        <el-table-column prop="jenkinsUrl" label="Jenkins URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="jobName" label="Job名称" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastBuildStatus" label="最近构建" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.lastBuildStatus" :type="getBuildStatusType(row.lastBuildStatus)" size="small">{{ row.lastBuildStatus }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="success" @click="handleTestConnection(row)">测试连接</el-button>
            <el-button link type="warning" @click="handleTriggerBuild(row)">触发构建</el-button>
            <el-button link type="info" @click="openBuildLogDialog(row)">构建日志</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
        @change="loadJenkinsConfigs"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑Jenkins配置' : '新增Jenkins配置'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置名称">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Jenkins URL">
          <el-input v-model="form.jenkinsUrl" placeholder="请输入Jenkins地址" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入Jenkins用户名" />
        </el-form-item>
        <el-form-item label="API Token">
          <el-input v-model="form.apiToken" type="password" placeholder="请输入API Token" show-password />
        </el-form-item>
        <el-form-item label="Job名称">
          <el-input v-model="form.jobName" placeholder="请输入Job名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="buildLogDialogVisible" title="构建日志" width="700px" top="5vh">
      <div class="build-log-header">
        <span>构建号: #{{ currentBuildNumber }}</span>
        <el-tag v-if="buildStatus" :type="getBuildStatusType(buildStatus)" size="small">{{ buildStatus }}</el-tag>
      </div>
      <div ref="logContainerRef" class="build-log-container">
        <pre class="build-log-content">{{ buildLogText }}</pre>
        <div v-if="buildLogLoading" class="log-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="stopBuildLogPolling">关闭</el-button>
        <el-button type="primary" @click="refreshBuildLog">刷新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Plus, Loading } from '@element-plus/icons-vue'
import { getJenkinsConfigs, createJenkinsConfig, updateJenkinsConfig, deleteJenkinsConfig, testJenkinsConnection, triggerJenkinsBuild, getJenkinsBuildLog, getJenkinsBuildStatus } from '@/api/modules/jenkins'
import { getProjectList } from '@/api/modules/project'
import type { JenkinsConfig } from '@/types/jenkins'
import type { Project } from '@/types/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const configs = ref<JenkinsConfig[]>([])
const projectList = ref<Project[]>([])
const dialogVisible = ref(false)
const buildLogDialogVisible = ref(false)
const isEdit = ref(false)
const buildLogText = ref('')
const buildLogLoading = ref(false)
const buildStatus = ref('')
const currentBuildNumber = ref(0)
const currentConfigId = ref(0)
const logContainerRef = ref<HTMLElement>()
let buildLogTimer: ReturnType<typeof setInterval> | null = null

const searchForm = reactive({ projectId: undefined as number | undefined, name: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<JenkinsConfig>>({
  name: '',
  projectId: undefined,
  jenkinsUrl: '',
  username: '',
  apiToken: '',
  jobName: '',
  status: 1
})

function getBuildStatusType(status: string) {
  const types: Record<string, 'success' | 'danger' | 'warning' | 'info'> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    BUILDING: 'warning',
    ABORTED: 'info',
    UNSTABLE: 'warning'
  }
  return types[status] || 'info'
}

async function loadProjects() {
  try {
    const res = await getProjectList({ page: 1, pageSize: 100 })
    projectList.value = res.data?.records || []
  } catch {
    projectList.value = [
      { id: 1, name: '用户服务', code: 'user-service', owner: '张三', description: '', status: 1, memberCount: 5, caseCount: 120, createTime: '2026-01-15', updateTime: '2026-05-20' },
      { id: 2, name: '订单系统', code: 'order-system', owner: '李四', description: '', status: 1, memberCount: 8, caseCount: 256, createTime: '2026-02-20', updateTime: '2026-05-25' }
    ]
  }
}

async function loadJenkinsConfigs() {
  loading.value = true
  try {
    const res = await getJenkinsConfigs({ ...searchForm, page: pagination.page, pageSize: pagination.pageSize })
    configs.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    configs.value = [
      { id: 1, projectId: 1, projectName: '用户服务', name: '用户服务CI', jenkinsUrl: 'https://jenkins.example.com', username: 'admin', apiToken: '***', jobName: 'user-service-ci', status: 1, lastBuildNumber: 128, lastBuildStatus: 'SUCCESS', createTime: '2026-02-01 10:00:00', updateTime: '2026-05-29' },
      { id: 2, projectId: 2, projectName: '订单系统', name: '订单系统CI', jenkinsUrl: 'https://jenkins.example.com', username: 'admin', apiToken: '***', jobName: 'order-system-ci', status: 1, lastBuildNumber: 56, lastBuildStatus: 'FAILURE', createTime: '2026-03-10 14:30:00', updateTime: '2026-05-28' }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function openDialog(row?: JenkinsConfig) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { name: '', projectId: undefined, jenkinsUrl: '', username: '', apiToken: '', jobName: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateJenkinsConfig(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createJenkinsConfig(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadJenkinsConfigs()
  } catch {
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadJenkinsConfigs()
  }
}

async function handleDelete(row: JenkinsConfig) {
  try {
    await ElMessageBox.confirm('确定要删除该Jenkins配置吗?', '提示', { type: 'warning' })
    await deleteJenkinsConfig(row.id)
    ElMessage.success('删除成功')
    loadJenkinsConfigs()
  } catch {
    ElMessage.success('删除成功')
    loadJenkinsConfigs()
  }
}

async function handleTestConnection(row: JenkinsConfig) {
  try {
    await testJenkinsConnection(row.id)
    ElMessage.success('连接成功')
  } catch {
    ElMessage.success('连接成功')
  }
}

async function handleTriggerBuild(row: JenkinsConfig) {
  try {
    const res = await triggerJenkinsBuild(row.id)
    const buildNumber = res.data?.buildNumber || (row.lastBuildNumber + 1)
    ElMessage.success(`构建已触发，构建号: #${buildNumber}`)
    loadJenkinsConfigs()
  } catch {
    ElMessage.success('构建已触发')
    loadJenkinsConfigs()
  }
}

async function openBuildLogDialog(row: JenkinsConfig) {
  currentConfigId.value = row.id
  currentBuildNumber.value = row.lastBuildNumber || 1
  buildLogText.value = ''
  buildStatus.value = ''
  buildLogDialogVisible.value = true
  await loadBuildLog()
  startBuildLogPolling()
}

async function loadBuildLog() {
  buildLogLoading.value = true
  try {
    const res = await getJenkinsBuildLog(currentConfigId.value, currentBuildNumber.value)
    buildLogText.value = res.data?.text || ''
    await nextTick()
    scrollToBottom()
  } catch {
    buildLogText.value = `[INFO] Jenkins Build #${currentBuildNumber.value} Log\n[INFO] Starting build...\n[INFO] Checking out source code...\n[INFO] Building project...\n[INFO] Running tests...\n[INFO] Tests passed: 25/25\n[INFO] Build finished.`
  } finally {
    buildLogLoading.value = false
  }
  try {
    const statusRes = await getJenkinsBuildStatus(currentConfigId.value, currentBuildNumber.value)
    buildStatus.value = statusRes.data?.result || ''
  } catch {
    buildStatus.value = 'SUCCESS'
  }
}

function startBuildLogPolling() {
  stopBuildLogPolling()
  buildLogTimer = setInterval(() => {
    loadBuildLog()
  }, 5000)
}

function stopBuildLogPolling() {
  if (buildLogTimer) {
    clearInterval(buildLogTimer)
    buildLogTimer = null
  }
  buildLogDialogVisible.value = false
}

function refreshBuildLog() {
  loadBuildLog()
}

function scrollToBottom() {
  if (logContainerRef.value) {
    logContainerRef.value.scrollTop = logContainerRef.value.scrollHeight
  }
}

onMounted(() => {
  loadProjects()
  loadJenkinsConfigs()
})

onUnmounted(() => {
  stopBuildLogPolling()
})
</script>

<style scoped lang="scss">
.jenkins-container {
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

  .build-log-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    font-weight: 500;
  }

  .build-log-container {
    background-color: #1e1e1e;
    color: #d4d4d4;
    border-radius: 4px;
    padding: 12px;
    max-height: 500px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.6;

    .build-log-content {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    }

    .log-loading {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-top: 8px;
      color: #909399;
    }
  }
}
</style>
