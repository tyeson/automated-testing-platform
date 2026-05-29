import request from '@/api'
import type { Project, ProjectParams } from '@/types/project'
import type { PageResult } from '@/types/common'

export function getProjectList(params: ProjectParams) {
  return request<PageResult<Project>>({
    url: '/projects',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, name: params.name }
  })
}

export function getProjectDetail(id: number) {
  return request<Project>({
    url: `/projects/${id}`,
    method: 'GET'
  })
}

export function createProject(data: Partial<Project>) {
  return request({
    url: '/projects',
    method: 'POST',
    data
  })
}

export function updateProject(id: number, data: Partial<Project>) {
  return request({
    url: `/projects/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteProject(id: number) {
  return request({
    url: `/projects/${id}`,
    method: 'DELETE'
  })
}
