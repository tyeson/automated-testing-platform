import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

function showMessage(type: 'error' | 'success' | 'warning' | 'info', msg: string) {
  try {
    const app = document.querySelector('#app')?.__vue_app__
    if (app) {
      const $message = app.config.globalProperties.$message
      setTimeout(() => $message[type](msg), 0)
      return
    }
  } catch (e) {
    console.error('[showMessage] error:', e)
  }
  ElMessage[type](msg)
}

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code !== 200) {
      showMessage('error', res.message || '请求失败')

      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res as any
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      const msg = error.response?.data?.message || '登录已过期，请重新登录'
      showMessage('error', msg)
      if (!window.location.pathname.includes('/login')) {
        router.push('/login')
      }
    } else {
      showMessage('error', error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

const request = <T = unknown>(config: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return service(config) as Promise<ApiResponse<T>>
}

export default request
