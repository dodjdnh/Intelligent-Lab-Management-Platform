<template>
  <div class="home-container">
    <el-row :gutter="20">
      
      <el-col :span="isAdmin ? 8 : 24">
        <el-card shadow="hover" class="data-card" style="border-left: 5px solid #409EFF">
          <div class="card-header">
            <span class="label">📅 今日预约</span>
            <el-tag type="primary">实时</el-tag>
          </div>
          <div class="card-value">{{ stats.todayReserve }} <span class="unit">次</span></div>
        </el-card>
      </el-col>
      
      <el-col :span="8" v-if="isAdmin">
        <el-card shadow="hover" class="data-card" style="border-left: 5px solid #E6A23C">
          <div class="card-header">
            <span class="label">⚠️ 耗材预警</span>
            <el-tag type="warning">库存不足</el-tag>
          </div>
          <div class="card-value">{{ stats.consumableWarning }} <span class="unit">种</span></div>
        </el-card>
      </el-col>

      <el-col :span="8" v-if="isAdmin">
        <el-card shadow="hover" class="data-card" style="border-left: 5px solid #F56C6C">
          <div class="card-header">
            <span class="label">📝 待办审批</span>
            <el-tag type="danger">急需处理</el-tag>
          </div>
          <div class="card-value">{{ stats.pendingTask }} <span class="unit">个</span></div>
        </el-card>
      </el-col>

    </el-row>

    <el-card style="margin-top: 20px; height: 400px; display: flex; justify-content: center; align-items: center;">
      <div style="text-align: center; color: #909399">
        <h1>欢迎使用智慧实验室管理系统</h1>
        <p>请点击左侧菜单进行预约或耗材管理</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

// 获取当前角色
const isAdmin = localStorage.getItem('role') === 'admin'

const stats = ref({
  todayReserve: 0,
  consumableWarning: 0,
  pendingTask: 0
})

const fetchStats = async () => {
  try {
    const res = await request.get('/home/stats')
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取首页数据失败', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.home-container { padding: 0; }
.data-card { cursor: pointer; transition: all 0.3s; }
.data-card:hover { transform: translateY(-5px); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.label { font-size: 16px; color: #606266; font-weight: bold; }
.card-value { font-size: 32px; font-weight: bold; color: #303133; }
.unit { font-size: 14px; color: #909399; margin-left: 5px; }
</style>