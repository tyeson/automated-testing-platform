export interface JenkinsConfig {
  id: number
  projectId: number
  projectName: string
  name: string
  jenkinsUrl: string
  username: string
  apiToken: string
  jobName: string
  status: 0 | 1
  lastBuildNumber: number
  lastBuildStatus: string
  createTime: string
  updateTime: string
}

export interface JenkinsConfigParams {
  projectId?: number
  name?: string
  page: number
  pageSize: number
}

export interface JenkinsBuildStatus {
  building: boolean
  result: string
  number: number
  url: string
  duration: number
  timestamp: number
}

export interface JenkinsBuildLog {
  text: string
  hasMore: boolean
  offset: number
}
