export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  status: 0 | 1
  roleIds: number[]
  createTime: string
  updateTime: string
}

export interface Role {
  id: number
  name: string
  code: string
  description: string
  status: 0 | 1
  permissionIds: number[]
  createTime: string
  updateTime: string
}

export interface Permission {
  id: number
  name: string
  code: string
  type: 'menu' | 'button' | 'api'
  parentId: number
  path: string
  icon: string
  sort: number
  children?: Permission[]
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  username: string
  email: string
  roles: string[]
}
