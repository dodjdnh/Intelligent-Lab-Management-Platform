import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080', // 这里必须对应你 Spring Boot 的端口
  timeout: 5000 // 请求超时时间
})

// 2. 请求拦截器
request.interceptors.request.use(config => {
  // 每次发请求前，检查本地存储是否有 token
  const token = localStorage.getItem('satoken') 
  if (token) {
    // 如果有，就塞进 Header 里。注意：'satoken' 是 Sa-Token 默认要求的参数名
    config.headers['satoken'] = token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 3. 响应拦截器
request.interceptors.response.use(response => {
  // 直接返回数据部分，过滤掉 axios 自带的其它信息
  return response.data
}, error => {
  return Promise.reject(error)
})

export default request