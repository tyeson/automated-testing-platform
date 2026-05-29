import request from '@/api'
import type { TestCase, TestSuite, Tag, TestCaseParams } from '@/types/testcase'
import type { PageResult } from '@/types/common'

export function getTestCaseList(params: TestCaseParams) {
  return request<PageResult<TestCase>>({
    url: '/testcases',
    method: 'GET',
    params: { current: params.page, size: params.pageSize, name: params.name }
  })
}

export function getTestCaseDetail(id: number) {
  return request<TestCase>({
    url: `/testcases/${id}`,
    method: 'GET'
  })
}

export function createTestCase(data: Partial<TestCase>) {
  return request({
    url: '/testcases',
    method: 'POST',
    data
  })
}

export function updateTestCase(id: number, data: Partial<TestCase>) {
  return request({
    url: `/testcases/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteTestCase(id: number) {
  return request({
    url: `/testcases/${id}`,
    method: 'DELETE'
  })
}

export function batchDeleteTestCases(ids: number[]) {
  return request({
    url: '/testcase/batch',
    method: 'DELETE',
    data: { ids }
  })
}

export function getSuiteList(projectId: number) {
  return request<TestSuite[]>({
    url: `/testcase/suites`,
    method: 'GET',
    params: { projectId }
  })
}

export function createSuite(data: Partial<TestSuite>) {
  return request({
    url: '/testcase/suite',
    method: 'POST',
    data
  })
}

export function updateSuite(id: number, data: Partial<TestSuite>) {
  return request({
    url: `/testcase/suite/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteSuite(id: number) {
  return request({
    url: `/testcase/suite/${id}`,
    method: 'DELETE'
  })
}

export function getTagList() {
  return request<Tag[]>({
    url: '/testcase/tags',
    method: 'GET'
  })
}

export function createTag(data: { name: string; color: string }) {
  return request({
    url: '/testcase/tag',
    method: 'POST',
    data
  })
}
