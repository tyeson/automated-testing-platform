export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'TIMEOUT'

export type TriggerType = 'MANUAL' | 'SCHEDULED' | 'JENKINS' | 'API'

export interface ExecutionRecord {
  id: number
  projectId: number
  projectName: string
  suiteId: number
  suiteName: string
  triggerType: TriggerType | 'manual' | 'scheduled' | 'jenkins' | 'api'
  status: ExecutionStatus
  totalCases: number
  passedCases: number
  failedCases: number
  skippedCases: number
  startTime: string
  endTime: string
  duration: number
  environment: string
  creator: string
  screenshots: string[]
  videoUrl: string
}

export interface ExecutionLog {
  id: number
  executionId: number
  caseId: number
  caseName: string
  status: ExecutionStatus
  startTime: string
  endTime: string
  duration: number
  errorMessage: string
  screenshot: string
  video: string
  trace: string
  consoleLogs: string
  networkLogs: string
}

export interface ExecutionParams {
  projectId?: number
  status?: ExecutionStatus
  startTime?: string
  endTime?: string
  page: number
  pageSize: number
}

export interface ExecuteParams {
  caseIds?: number[]
  suiteId?: number
  environment: string
  projectId: number
}

export interface TriggerExecutionParams {
  projectId: number
  caseIds?: number[]
  suiteId?: number
  triggerType: TriggerType
  environment: string
}

export interface ExecutionStatusResponse {
  status: ExecutionStatus
  progress: number
}
