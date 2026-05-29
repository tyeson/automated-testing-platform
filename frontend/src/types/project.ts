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
  name: string
  url: string
  type: 'DEV' | 'TEST' | 'UAT' | 'PROD'
  status: 0 | 1
}
