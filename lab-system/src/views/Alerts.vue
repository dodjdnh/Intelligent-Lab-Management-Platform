<template>
  <div class="alerts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警中心</span>
          <el-button type="primary" text @click="fetchAlerts">刷新</el-button>
        </div>
      </template>

      <el-table :data="alerts" border stripe>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag :type="levelTag(scope.row.level)">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" />
        <el-table-column prop="message" label="内容" />
        <el-table-column prop="sourceId" label="来源设备" width="140" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              v-if="scope.row.status !== '已确认'"
              size="small"
              type="success"
              @click="ack(scope.row.id)"
            >
              确认
            </el-button>
            <span v-else style="color: #999;">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const alerts = ref([])
const streamBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
let eventSource = null

const fetchAlerts = async () => {
  try {
    const res = await request.get('/alert/list')
    if (res.code === 200) alerts.value = res.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取告警列表失败')
  }
}

const ack = async (id) => {
  try {
    const res = await request.post('/alert/ack', { id })
    if (res.code === 200) {
      ElMessage.success('告警已确认')
      fetchAlerts()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '确认告警失败')
  }
}

const levelTag = (level) => {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}

onMounted(() => {
  fetchAlerts()
  const token = localStorage.getItem('satoken')
  if (token) {
    eventSource = new EventSource(`${streamBaseUrl}/stream/events?satoken=${encodeURIComponent(token)}`)
    eventSource.addEventListener('alert.created', fetchAlerts)
  }
})

onUnmounted(() => {
  eventSource?.close()
})
</script>

<style scoped>
.alerts-page { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
