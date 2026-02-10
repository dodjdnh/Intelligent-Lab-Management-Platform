<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>智慧实验室管理系统</h2>
      <el-form :model="loginForm">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名" 
            prefix-icon="User" 
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock" 
            show-password 
          />
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="handleLogin">进入系统</el-button>
      </el-form> </el-card> </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 导入图标
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loginForm = ref({ username: '', password: '' })

const handleLogin = async () => {
  try {
    const res = await request.post('/auth/login', loginForm.value)
    if (res.code === 200) {
      localStorage.setItem('satoken', res.data.token)
      localStorage.setItem('role', res.data.role)
      localStorage.setItem('userName', loginForm.value.username)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('登录异常，请检查后端服务')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.login-card { width: 400px; padding: 20px; }
h2 { margin-bottom: 30px; color: #409EFF; }
</style>