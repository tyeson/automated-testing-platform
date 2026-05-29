export type TestCaseType = 'UI' | 'API' | 'APP'
export type PriorityType = 'P0' | 'P1' | 'P2' | 'P3'
export type CaseStatus = 0 | 1

export interface TestCase {
  id: number
  projectId: number
  projectName: string
  name: string
  code: string
  type: TestCaseType
  priority: PriorityType
  description: string
  preCondition: string
  steps: string
  expected: string
  scriptPath: string
  tags: string[]
  suiteId: number
  suiteName: string
  status: CaseStatus
  creator: string
  reviewer: string
  createTime: string
  updateTime: string
}

export interface TestSuite {
  id: number
  projectId: number
  name: string
  description: string
  caseCount: number
  caseIds: number[]
  creator: string
  createTime: string
  updateTime: string
}

export interface Tag {
  id: number
  name: string
  color: string
  caseCount: number
}

export interface TestCaseParams {
  projectId?: number
  name?: string
  type?: TestCaseType
  priority?: PriorityType
  suiteId?: number
  tag?: string
  page: number
  pageSize: number
}
