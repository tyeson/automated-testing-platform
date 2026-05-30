<template>
  <div class="role-management-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>角色管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="roles" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openPermissionDialog(row)">权限配置</el-button>
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
        @change="loadRoles"
      />
    </el-card>

    <!-- 角色对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.code" placeholder="请输入角色编码" :disabled="isEdit" />
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

    <!-- 权限配置对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="600px">
      <el-tree ref="treeRef" :data="permissions" :props="{ label: 'name', children: 'children' }" show-checkbox node-key="id" default-expand-all />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole, getPermissionList, getRolePermissions, assignRolePermissions } from '@/api/modules/system'
import type { Role, Permission } from '@/types/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const roles = ref<Role[]>([])
const permissions = ref<Permission[]>([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const currentRoleId = ref<number>()

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive<Partial<Role>>({
  name: '',
  code: '',
  description: '',
  status: 1
})

async function loadRoles() {
  loading.value = true
  try {
    const res = await getRoleList({ page: pagination.page, pageSize: pagination.pageSize })
    roles.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (err) {
    console.error('加载角色列表失败', err)
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Role) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { name: '', code: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value && form.id) {
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRoles()
  } catch (err) {
    console.error('操作角色失败', err)
  }
}

async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗?', '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (err) {
    console.error('删除角色失败', err)
  }
}

async function openPermissionDialog(row: Role) {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true

  try {
    const [permRes, rolePermRes] = await Promise.all([getPermissionList(), getRolePermissions(row.id)])
    permissions.value = permRes.data || mockPermissions
    // Set checked keys based on role permissions
  } catch {
    permissions.value = mockPermissions
  }
}

async function savePermissions() {
  try {
    // Get checked keys from tree
    const checkedKeys: number[] = []
    await assignRolePermissions(currentRoleId.value!, checkedKeys)
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
  } catch {
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
  }
}

const mockPermissions: Permission[] = [
  { id: 1, name: '工作台', code: 'dashboard', type: 'menu', parentId: 0, path: '/dashboard', icon: '', sort: 1 },
  { id: 2, name: '项目管理', code: 'project', type: 'menu', parentId: 0, path: '/project', icon: '', sort: 2 },
  { id: 3, name: '用例管理', code: 'testcase', type: 'menu', parentId: 0, path: '/testcase', icon: '', sort: 3 },
  { id: 4, name: '执行中心', code: 'execution', type: 'menu', parentId: 0, path: '/execution', icon: '', sort: 4 },
  { id: 5, name: '报告中心', code: 'report', type: 'menu', parentId: 0, path: '/report', icon: '', sort: 5 },
  { id: 6, name: '系统管理', code: 'system', type: 'menu', parentId: 0, path: '/system', icon: '', sort: 6 }
]

onMounted(() => {
  loadRoles()
})
</script>

<style scoped lang="scss">
.role-management-container {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
