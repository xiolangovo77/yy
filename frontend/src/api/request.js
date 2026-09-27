import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：添加 JWT token
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回的 BaseResponse：{ code, data, message }
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')

      // 40100 - 未登录，跳转登录页
      if (res.code === 40100) {
        clearAuth()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
