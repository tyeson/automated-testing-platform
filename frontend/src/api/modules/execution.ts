import request from '@/api'
import type { ExecutionRecord, ExecutionLog, ExecutionParams, ExecuteParams, TriggerExecutionParams, ExecutionStatusResponse } from '@/types/execution'
import type { PageResult } from '@/types/common'

export function executeTests(data: ExecuteParams) {
  return request<{ executionId: number }>({
    url: '/executions',
    method: 'POST',
    data
  })
}

export function triggerExecution(data: TriggerExecutionParams) {
  return request<{ executionId: number }>({
    url: '/executions/trigger',
    method: 'POST',
    data
  })
}

export function getExecutionList(params: ExecutionParams) {
  return request<PageResult<ExecutionRecord>>({
    url: '/executions',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, projectId: params.projectId, status: params.status }
  })
}

export function getExecutionDetail(id: number) {
  return request<ExecutionRecord>({
    url: `/executions/${id}`,
    method: 'GET'
  })
}

export function getExecutionLogs(executionId: number) {
  return request<ExecutionLog[]>({
    url: `/executions/${executionId}/logs`,
    method: 'GET'
  })
}

export function stopExecution(executionId: number) {
  return request({
    url: `/executions/${executionId}/stop`,
    method: 'POST'
  })
}

export function getExecutionStatus(executionId: number) {
  return request<ExecutionStatusResponse>({
    url: `/executions/${executionId}/status`,
    method: 'GET'
  })
}
