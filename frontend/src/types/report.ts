export interface ReportSummary {
  executionId: number
  projectName: string
  suiteName: string
  status: string
  totalCases: number
  passedCases: number
  failedCases: number
  passRate: number
  duration: number
  startTime: string
  endTime: string
  environment: string
}

export interface TrendData {
  date: string
  total: number
  passed: number
  failed: number
  passRate: number
}

export interface EnvironmentAnalysis {
  environment: string
  totalExecutions: number
  successCount: number
  failCount: number
  avgDuration: number
  stability: number
}

export interface FailureAnalysis {
  reason: string
  count: number
  percentage: number
  trend: 'up' | 'down' | 'stable'
}
