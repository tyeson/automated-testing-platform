import request from '@/api'
import type { Role, Permission } from '@/types/user'
import type { PageResult } from '@/types/common'

export function getRoleList(params: { page: number; pageSize: number }) {
  return request<PageResult<Role>>({
    url: '/roles',
    method: 'GET',
    params: { current: params.page, size: params.pageSize }
  })
}

export function createRole(data: Partial<Role>) {
  return request({
    url: '/roles',
    method: 'POST',
    data
  })
}

export function updateRole(id: number, data: Partial<Role>) {
  return request({
    url: `/roles/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteRole(id: number) {
  return request({
    url: `/roles/${id}`,
    method: 'DELETE'
  })
}

export function getPermissionList() {
  return request<Permission[]>({
    url: '/permissions',
    method: 'GET'
  })
}

export function getRolePermissions(roleId: number) {
  return request<Permission[]>({
    url: `/roles/${roleId}/permissions`,
    method: 'GET'
  })
}

export function assignRolePermissions(roleId: number, permissionIds: number[]) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'PUT',
    data: { permissionIds }
  })
}
