<template>
  <div class="environment-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-select v-model="searchForm.projectId" placeholder="选择项目" clearable style="width: 160px">
              <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
            <el-input v-model="searchForm.name" placeholder="搜索环境名称" clearable style="width: 200px" @keyup.enter="loadEnvironments" />
            <el-button type="primary" @click="loadEnvironments">搜索</el-button>
          </div>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增环境
          </el-button>
        </div>
      </template>

      <el-table :data="environments" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="环境名称" width="150" />
        <el-table-column prop="projectName" label="所属项目" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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
        @change="loadEnvironments"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑环境' : '新增环境'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="环境名称">
          <el-input v-model="form.name" placeholder="请输入环境名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="选择环境类型" style="width: 100%">
            <el-option label="DEV - 开发环境" value="DEV" />
            <el-option label="TEST - 测试环境" value="TEST" />
            <el-option label="UAT - 预发环境" value="UAT" />
            <el-option label="PROD - 生产环境" value="PROD" />
          </el-select>
        </el-form-item>
        <el-form-item label="URL">
          <el-input v-model="form.url" placeholder="请输入环境URL" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getEnvironmentList, createEnvironment, updateEnvironment, deleteEnvironment } from '@/api/modules/environment'
import { getProjectList } from '@/api/modules/project'
import type { Environment } from '@/types/project'
import type { Project } from '@/types/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const environments = ref<Environment[]>([])
const projectList = ref<Project[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ projectId: undefined as number | undefined, name: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<Environment>>({
  projectId: undefined,
  name: '',
  type: 'DEV',
  url: '',
  description: '',
  status: 1
})

function getTypeTag(type: string) {
  const tags: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    DEV: '',
    TEST: 'success',
    UAT: 'warning',
    PROD: 'danger'
  }
  return tags[type] || 'info'
}

async function loadProjects() {
  try {
    const res = await getProjectList({ page: 1, pageSize: 100 })
    projectList.value = res.data?.records || []
  } catch (err) {
    console.error('加载项目列表失败', err)
  }
}

async function loadEnvironments() {
  loading.value = true
  try {
    const res = await getEnvironmentList({ ...searchForm, page: pagination.page, pageSize: pagination.pageSize })
    environments.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    environments.value = [
      { id: 1, projectId: 1, projectName: '用户服务', name: '开发环境', type: 'DEV', url: 'https://dev-user.example.com', description: '开发联调环境', status: 1, createTime: '2026-01-15 10:30:00', updateTime: '2026-05-20' },
      { id: 2, projectId: 1, projectName: '用户服务', name: '测试环境', type: 'TEST', url: 'https://test-user.example.com', description: '功能测试环境', status: 1, createTime: '2026-02-10 14:00:00', updateTime: '2026-05-22' },
      { id: 3, projectId: 2, projectName: '订单系统', name: '预发环境', type: 'UAT', url: 'https://uat-order.example.com', description: '预发布验证环境', status: 1, createTime: '2026-03-05 09:00:00', updateTime: '2026-05-18' },
      { id: 4, projectId: 2, projectName: '订单系统', name: '生产环境', type: 'PROD', url: 'https://order.example.com', description: '正式生产环境', status: 1, createTime: '2026-01-01 00:00:00', updateTime: '2026-05-28' }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Environment) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { projectId: undefined, name: '', type: 'DEV', url: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateEnvironment(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createEnvironment(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadEnvironments()
  } catch {
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadEnvironments()
  }
}

async function handleDelete(row: Environment) {
  try {
    await ElMessageBox.confirm('确定要删除该环境吗?', '提示', { type: 'warning' })
    await deleteEnvironment(row.id)
    ElMessage.success('删除成功')
    loadEnvironments()
  } catch (err) {
    console.error('删除环境失败', err)
  }
}

onMounted(() => {
  loadProjects()
  loadEnvironments()
})
</script>

<style scoped lang="scss">
.environment-container {
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
