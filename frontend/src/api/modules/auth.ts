import request from '@/api'
import type { LoginParams, LoginResult, User } from '@/types/user'
import type { PageResult } from '@/types/common'

export function login(data: LoginParams) {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}

export function refreshToken(refreshToken: string) {
  return request<LoginResult>({
    url: '/auth/refresh',
    method: 'POST',
    data: { refreshToken }
  })
}

export function getUserInfo() {
  return request<User>({
    url: '/auth/info',
    method: 'GET'
  })
}

export function getUserMenus() {
  return request({
    url: '/user/menus',
    method: 'GET'
  })
}

export function getUserList(params: { page: number; pageSize: number; username?: string }) {
  return request<PageResult<User>>({
    url: '/users',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, username: params.username }
  })
}

export function createUser(data: Partial<User>) {
  return request({
    url: '/users',
    method: 'POST',
    data
  })
}

export function updateUser(id: number, data: Partial<User>) {
  return request({
    url: `/users/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteUser(id: number) {
  return request({
    url: `/users/${id}`,
    method: 'DELETE'
  })
}
