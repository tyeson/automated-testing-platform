import request from '@/api'
import type { Environment, EnvironmentParams } from '@/types/project'
import type { PageResult } from '@/types/common'

export function getEnvironmentList(params: EnvironmentParams) {
  return request<PageResult<Environment>>({
    url: '/environments',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, projectId: params.projectId, name: params.name, type: params.type }
  })
}

export function getEnvironmentDetail(id: number) {
  return request<Environment>({
    url: `/environments/${id}`,
    method: 'GET'
  })
}

export function createEnvironment(data: Partial<Environment>) {
  return request({
    url: '/environments',
    method: 'POST',
    data
  })
}

export function updateEnvironment(id: number, data: Partial<Environment>) {
  return request({
    url: `/environments/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteEnvironment(id: number) {
  return request({
    url: `/environments/${id}`,
    method: 'DELETE'
  })
}
