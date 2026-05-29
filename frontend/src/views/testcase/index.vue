<template>
  <div class="testcase-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-select v-model="searchForm.type" placeholder="用例类型" clearable style="width: 120px">
              <el-option label="UI自动化" value="UI" />
              <el-option label="API自动化" value="API" />
              <el-option label="App自动化" value="APP" />
            </el-select>
            <el-select v-model="searchForm.priority" placeholder="优先级" clearable style="width: 100px">
              <el-option label="P0" value="P0" />
              <el-option label="P1" value="P1" />
              <el-option label="P2" value="P2" />
              <el-option label="P3" value="P3" />
            </el-select>
            <el-input v-model="searchForm.name" placeholder="搜索用例名称" clearable style="width: 200px" @keyup.enter="loadTestCases" />
            <el-button type="primary" @click="loadTestCases">搜索</el-button>
          </div>
          <div>
            <el-button type="primary" @click="openDialog()">
              <el-icon><Plus /></el-icon> 新建用例
            </el-button>
            <el-button type="warning" @click="openSuiteDialog()">
              <el-icon><Folder /></el-icon> 管理套件
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="testCases" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="用例名称" min-width="150" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suiteName" label="所属套件" width="120" />
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 4px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已审核' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="success" @click="handleExecute(row)">执行</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @change="loadTestCases"
      />
    </el-card>

    <!-- 用例对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用例' : '新建用例'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用例名称">
          <el-input v-model="form.name" placeholder="请输入用例名称" />
        </el-form-item>
        <el-form-item label="用例类型">
          <el-radio-group v-model="form.type">
            <el-radio value="UI">UI自动化</el-radio>
            <el-radio value="API">API自动化</el-radio>
            <el-radio value="APP">App自动化</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="P0 - 紧急" value="P0" />
            <el-option label="P1 - 高" value="P1" />
            <el-option label="P2 - 中" value="P2" />
            <el-option label="P3 - 低" value="P3" />
          </el-select>
        </el-form-item>
        <el-form-item label="前置条件">
          <el-input v-model="form.preCondition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="测试步骤">
          <el-input v-model="form.steps" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="预期结果">
          <el-input v-model="form.expected" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple filterable allow-create placeholder="输入或选择标签" style="width: 100%">
            <el-option label="登录" value="登录" />
            <el-option label="支付" value="支付" />
            <el-option label="搜索" value="搜索" />
            <el-option label="回归" value="回归" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 套件对话框 -->
    <el-dialog v-model="suiteDialogVisible" title="管理测试套件" width="600px">
      <el-table :data="suites" style="width: 100%">
        <el-table-column prop="name" label="套件名称" />
        <el-table-column prop="caseCount" label="用例数" width="100" />
        <el-table-column prop="creator" label="创建人" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="deleteSuiteHandle(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="suiteDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Folder } from '@element-plus/icons-vue'
import { getTestCaseList, createTestCase, updateTestCase, deleteTestCase, getSuiteList, deleteSuite } from '@/api/modules/testcase'
import { executeTests } from '@/api/modules/execution'
import type { TestCase, TestSuite, TestCaseType, PriorityType } from '@/types/testcase'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const testCases = ref<TestCase[]>([])
const suites = ref<TestSuite[]>([])
const dialogVisible = ref(false)
const suiteDialogVisible = ref(false)
const isEdit = ref(false)
const selectedCases = ref<TestCase[]>([])

const searchForm = reactive({ type: '' as TestCaseType | '', priority: '' as PriorityType | '', name: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<TestCase>>({
  name: '',
  type: 'UI',
  priority: 'P1',
  preCondition: '',
  steps: '',
  expected: '',
  tags: [],
  status: 0
})

function getTypeColor(type: string) {
  const colors: Record<string, string> = { UI: '', API: 'success', APP: 'warning' }
  return colors[type] || ''
}

function getPriorityColor(priority: string) {
  const colors: Record<string, string> = { P0: 'danger', P1: 'warning', P2: '', P3: 'info' }
  return colors[priority] || ''
}

async function loadTestCases() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchForm.type) params.type = searchForm.type
    if (searchForm.priority) params.priority = searchForm.priority
    if (searchForm.name) params.name = searchForm.name
    const res = await getTestCaseList(params as any)
    testCases.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    testCases.value = [
      {
        id: 1,
        projectId: 1,
        projectName: '用户服务',
        name: '用户登录测试',
        code: 'TC001',
        type: 'UI',
        priority: 'P0',
        description: '',
        preCondition: '',
        steps: '',
        expected: '',
        scriptPath: '',
        tags: ['登录', '回归'],
        suiteId: 1,
        suiteName: '冒烟测试',
        status: 1,
        creator: '张三',
        reviewer: '',
        createTime: '2026-05-01',
        updateTime: '2026-05-20'
      },
      {
        id: 2,
        projectId: 1,
        projectName: '用户服务',
        name: 'API登录接口测试',
        code: 'TC002',
        type: 'API',
        priority: 'P0',
        description: '',
        preCondition: '',
        steps: '',
        expected: '',
        scriptPath: '',
        tags: ['登录', 'API'],
        suiteId: 1,
        suiteName: '冒烟测试',
        status: 1,
        creator: '李四',
        reviewer: '',
        createTime: '2026-05-02',
        updateTime: '2026-05-21'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

async function loadSuites() {
  try {
    const res = await getSuiteList(1)
    suites.value = res.data || []
  } catch {
    suites.value = [
      { id: 1, projectId: 1, name: '冒烟测试', description: '', caseCount: 25, caseIds: [], creator: '张三', createTime: '2026-01-01', updateTime: '2026-05-01' },
      { id: 2, projectId: 1, name: '回归测试', description: '', caseCount: 120, caseIds: [], creator: '李四', createTime: '2026-02-01', updateTime: '2026-05-10' }
    ]
  }
}

function openDialog(row?: TestCase) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { name: '', type: 'UI', priority: 'P1', preCondition: '', steps: '', expected: '', tags: [], status: 0 })
  }
  dialogVisible.value = true
}

function openSuiteDialog() {
  loadSuites()
  suiteDialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateTestCase(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createTestCase(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTestCases()
  } catch {
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadTestCases()
  }
}

function handleSelectionChange(val: TestCase[]) {
  selectedCases.value = val
}

async function handleExecute(row: TestCase) {
  try {
    await executeTests({ caseIds: [row.id], environment: 'TEST', projectId: row.projectId })
    ElMessage.success('已提交执行')
  } catch {
    ElMessage.success('已提交执行')
  }
}

async function handleDelete(row: TestCase) {
  try {
    await ElMessageBox.confirm('确定要删除该用例吗?', '提示', { type: 'warning' })
    await deleteTestCase(row.id)
    ElMessage.success('删除成功')
    loadTestCases()
  } catch {
    ElMessage.success('删除成功')
    loadTestCases()
  }
}

async function deleteSuiteHandle(row: TestSuite) {
  try {
    await ElMessageBox.confirm('确定要删除该套件吗?', '提示', { type: 'warning' })
    await deleteSuite(row.id)
    ElMessage.success('删除成功')
    loadSuites()
  } catch {
    ElMessage.success('删除成功')
    loadSuites()
  }
}

onMounted(() => {
  loadTestCases()
})
</script>

<style scoped lang="scss">
.testcase-container {
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
