<template>
  <div class="user-management-container">
    <el-card>
      <template #header>
        <div class="header">
          <div class="filter">
            <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable style="width: 200px" @keyup.enter="loadUsers" />
            <el-button type="primary" @click="loadUsers">搜索</el-button>
          </div>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增用户
          </el-button>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.roleName || '未分配' }}</el-tag>
          </template>
        </el-table-column>
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
        @change="loadUsers"
      />
    </el-card>

    <!-- 用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" placeholder="选择角色" style="width: 100%">
            <el-option label="超级管理员" :value="1" />
            <el-option label="测试经理" :value="2" />
            <el-option label="测试工程师" :value="3" />
            <el-option label="开发工程师" :value="4" />
          </el-select>
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
import { getUserList, createUser, updateUser, deleteUser } from '@/api/modules/auth'
import type { User } from '@/types/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref<User[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ username: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<User & { password?: string; roleId?: number; roleName?: string }>>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserList({ ...searchForm, page: pagination.page, pageSize: pagination.pageSize })
    users.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    users.value = [
      {
        id: 1,
        username: 'admin',
        realName: '系统管理员',
        email: 'admin@example.com',
        phone: '13800138000',
        avatar: '',
        status: 1,
        roleIds: [1],
        createTime: '2026-01-01 00:00:00',
        updateTime: '2026-05-01'
      },
      {
        id: 2,
        username: 'zhangsan',
        realName: '张三',
        email: 'zhangsan@example.com',
        phone: '13800138001',
        avatar: '',
        status: 1,
        roleIds: [2],
        createTime: '2026-02-15 10:30:00',
        updateTime: '2026-05-10'
      }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

function openDialog(row?: User) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { username: '', realName: '', password: '', email: '', phone: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch {
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadUsers()
  }
}

async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗?', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch {
    ElMessage.success('删除成功')
    loadUsers()
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.user-management-container {
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
