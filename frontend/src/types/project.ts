export interface Project {
  id: number
  name: string
  code: string
  owner: string
  description: string
  status: 0 | 1
  memberCount: number
  caseCount: number
  createTime: string
  updateTime: string
}

export interface ProjectParams {
  name?: string
  code?: string
  page: number
  pageSize: number
}

export interface Environment {
  id: number
  projectId: number
  projectName: string
  name: string
  url: string
  type: 'DEV' | 'TEST' | 'UAT' | 'PROD'
  description: string
  status: 0 | 1
  createTime: string
  updateTime: string
}

export interface EnvironmentParams {
  projectId?: number
  name?: string
  type?: string
  page: number
  pageSize: number
}
