export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = unknown> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface PageParams {
  page: number
  pageSize: number
}
