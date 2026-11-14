import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一的 axios 实例：所有请求以 /api 为前缀（由 vite 代理到 8080）
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 JWT token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一解包 Result，并处理 401 / 403
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      ElMessage.error('未登录或登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 动态引入，避免与 router 产生循环依赖
      import('../router').then((module) => module.default.push('/login'))
    } else if (status === 403) {
      ElMessage.error('禁止访问：当前角色无操作权限')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request