import request from '@/api'
import type { JenkinsConfig, JenkinsConfigParams, JenkinsBuildStatus, JenkinsBuildLog } from '@/types/jenkins'
import type { PageResult } from '@/types/common'

export function getJenkinsConfigs(params: JenkinsConfigParams) {
  return request<PageResult<JenkinsConfig>>({
    url: '/jenkins/configs',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, projectId: params.projectId, name: params.name }
  })
}

export function createJenkinsConfig(data: Partial<JenkinsConfig>) {
  return request({
    url: '/jenkins/configs',
    method: 'POST',
    data
  })
}

export function updateJenkinsConfig(id: number, data: Partial<JenkinsConfig>) {
  return request({
    url: `/jenkins/configs/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteJenkinsConfig(id: number) {
  return request({
    url: `/jenkins/configs/${id}`,
    method: 'DELETE'
  })
}

export function testJenkinsConnection(id: number) {
  return request<{ success: boolean; message: string }>({
    url: `/jenkins/configs/${id}/test`,
    method: 'POST'
  })
}

export function triggerJenkinsBuild(configId: number) {
  return request<{ buildNumber: number }>({
    url: `/jenkins/trigger/${configId}`,
    method: 'POST'
  })
}

export function getJenkinsBuildStatus(configId: number, buildNumber: number) {
  return request<JenkinsBuildStatus>({
    url: `/jenkins/status/${configId}/${buildNumber}`,
    method: 'GET'
  })
}

export function getJenkinsBuildLog(configId: number, buildNumber: number) {
  return request<JenkinsBuildLog>({
    url: `/jenkins/log/${configId}/${buildNumber}`,
    method: 'GET'
  })
}
