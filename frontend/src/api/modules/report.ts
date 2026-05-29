import request from '@/api'
import type { ReportSummary, TrendData, EnvironmentAnalysis, FailureAnalysis } from '@/types/report'
import type { PageResult } from '@/types/common'

export function getReportList(params: { page: number; pageSize: number }) {
  return request<PageResult<ReportSummary>>({
    url: '/reports',
    method: 'GET',
    params: { current: params.page, size: params.pageSize }
  })
}

export function getReportDetail(id: number) {
  return request<ReportSummary>({
    url: `/reports/${id}`,
    method: 'GET'
  })
}

export function getTrendData(days: number) {
  return request<TrendData[]>({
    url: '/reports/trend',
    method: 'GET',
    params: { days }
  })
}

export function getEnvironmentAnalysis() {
  return request<EnvironmentAnalysis[]>({
    url: '/reports/environment',
    method: 'GET'
  })
}

export function getFailureAnalysis(projectId?: number) {
  return request<FailureAnalysis[]>({
    url: '/reports/failure',
    method: 'GET',
    params: { projectId }
  })
}

export function getDashboardMetrics() {
  return request({
    url: '/reports/dashboard',
    method: 'GET'
  })
}
