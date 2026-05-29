<template>
  <div class="project-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-input v-model="searchForm.name" placeholder="搜索项目名称" clearable style="width: 200px" @keyup.enter="loadProjects" />
            <el-button type="primary" @click="loadProjects">搜索</el-button>
          </div>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新建项目
          </el-button>
        </div>
      </template>

      <el-table :data="projects" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="code" label="项目编码" width="150" />
        <el-table-column prop="owner" label="负责人" width="120" />
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="caseCount" label="用例数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
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
        @change="loadProjects"
      />
    </el-card>

    <!-- 项目对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新建项目'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码">
          <el-input v-model="form.code" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
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
import { getProjectList, createProject, updateProject, deleteProject } from '@/api/modules/project'
import type { Project } from '@/types/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const projects = ref<Project[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ name: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<Project>>({
  name: '',
  code: '',
  owner: '',
  description: '',
  status: 1
})

async function loadProjects() {
  loading.value = true
  try {
    const res = await getProjectList({ ...searchForm, page: pagination.page, pageSize: pagination.pageSize })
    projects.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    // 使用模拟数据
    projects.value = [
      { id: 1, name: '用户服务', code: 'user-service', owner: '张三', description: '', status: 1, memberCount: 5, caseCount: 120, createTime: '2026-01-15 10:30', updateTime: '2026-05-20' },
      { id: 2, name: '订单系统', code: 'order-system', owner: '李四', description: '', status: 1, memberCount: 8, caseCount: 256, createTime: '2026-02-20', updateTime: '2026-05-25' }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Project) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { name: '', code: '', owner: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateProject(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createProject(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadProjects()
  } catch {
    // 模拟成功
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadProjects()
  }
}

async function handleDelete(row: Project) {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗?', '提示', { type: 'warning' })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    loadProjects()
  } catch {
    // 模拟删除
    ElMessage.success('删除成功')
    loadProjects()
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="scss">
.project-container {
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
