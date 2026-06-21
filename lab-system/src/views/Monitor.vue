<template>
  <el-card class="monitor-card">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <span style="font-weight: bold;">实验室视频实时监控</span>
        </div>
        <el-alert
          v-if="hasAlert"
          title="系统警告：2号区域检测到异常入侵！"
          type="error"
          effect="dark"
          :closable="false"
          show-icon
          class="top-alert"
        />
      </div>
    </template>

    <el-row :gutter="20">
      <el-col :span="12" v-for="item in monitors" :key="item.id">
        
        <div class="video-container" :class="{ 'alert-mode': item.isAlert }">
          
          <div class="monitor-info">
            <span class="live-tag" :class="{ 'red-text': item.isAlert }">
              {{ item.isAlert ? '● WARNING' : '● REC' }}
            </span>
            <span class="camera-id">CAM-0{{ item.id }}</span>
          </div>

          <div v-if="item.isAlert" class="alert-content">
            <img :src="item.imgSrc" class="snapshot-img" />
            <div class="alert-overlay">
              <p>检测到异常目标</p>
              <p>已锁定画面 {{ currentTime }}</p>
            </div>
          </div>

          <div v-else class="normal-content">
            <div class="scanning-line"></div>
            <div class="placeholder-text">实验室{{ item.area }}正常监测中...</div>
          </div>

          <div class="timestamp">{{ currentTime }}</div>
          <div class="signal-icon">
             {{ item.isAlert ? '⚠ Signal Loss' : '📶 High Signal' }}
          </div>
        </div>

      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import alertImg from '../assets/alert_snapshot.png'

const currentTime = ref(new Date().toLocaleString())
let timer = null
let eventSource = null
const streamBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const monitors = ref([])

// 计算属性：判断当前是否有任意一个监控在报警
const hasAlert = computed(() => monitors.value.some(m => m.isAlert))

const fetchMonitor = async () => {
  try {
    const res = await request.get('/device/monitor')
    if (res.code === 200) {
      monitors.value = (res.data.devices || []).map((item, index) => ({
        id: index + 1,
        isAlert: item.lastValue !== null && Number(item.lastValue) < 10,
        area: item.area,
        imgSrc: alertImg,
        deviceId: item.deviceId,
        status: item.status
      }))
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取监控数据失败')
  }
}

onMounted(() => {
  fetchMonitor()
  timer = setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)

  const token = localStorage.getItem('satoken')
  if (token) {
    eventSource = new EventSource(`${streamBaseUrl}/stream/events?satoken=${encodeURIComponent(token)}`)
    eventSource.addEventListener('device.status.changed', fetchMonitor)
    eventSource.addEventListener('alert.created', fetchMonitor)
  }
})

onUnmounted(() => {
  clearInterval(timer)
  eventSource?.close()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-alert {
  width: auto;
  padding: 8px 15px;
  margin-left: 20px;
}

.video-container {
  background: #000;
  height: 260px;
  margin-bottom: 20px;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00ff00;
  font-family: 'Courier New', Courier, monospace;
  border: 1px solid #333;
  transition: all 0.3s;
}

/* === 新增：报警状态的样式 === */
.video-container.alert-mode {
  border: 2px solid #F56C6C; /* 红色边框 */
  box-shadow: 0 0 15px rgba(245, 108, 108, 0.5); /* 红色发光 */
  animation: redPulse 1s infinite alternate; /* 呼吸灯效果 */
}

@keyframes redPulse {
  from { box-shadow: 0 0 5px rgba(245, 108, 108, 0.3); }
  to { box-shadow: 0 0 20px rgba(245, 108, 108, 0.8); }
}

.red-text {
  color: #F56C6C !important;
  font-weight: bold;
  animation: blink 0.5s infinite; /* 快速闪烁 */
}

/* 图片样式 */
.alert-content {
  width: 100%;
  height: 100%;
  position: relative;
}

.snapshot-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8; /* 稍微暗一点，配合监控感 */
  filter: contrast(1.2) grayscale(0.2);
}

.alert-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(245, 108, 108, 0.8);
  color: white;
  padding: 10px 20px;
  text-align: center;
  font-weight: bold;
  border: 1px solid red;
}

/* === 原有样式保持不变 === */
.monitor-info { position: absolute; top: 10px; left: 10px; right: 10px; display: flex; justify-content: space-between; font-size: 12px; z-index: 2; text-shadow: 1px 1px 2px #000; }
.live-tag { color: #ff0000; animation: blink 1s infinite; }
.timestamp { position: absolute; bottom: 10px; left: 10px; font-size: 12px; opacity: 0.8; z-index: 2; text-shadow: 1px 1px 2px #000; }
.signal-icon { position: absolute; bottom: 10px; right: 10px; font-size: 10px; z-index: 2; }

/* 扫描线动画 */
.scanning-line {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 2px;
  background: rgba(0, 255, 0, 0.2);
  box-shadow: 0 0 10px rgba(0, 255, 0, 0.5);
  animation: scan 4s linear infinite;
  z-index: 1;
}

@keyframes scan { 0% { top: 0; } 100% { top: 100%; } }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0; } 100% { opacity: 1; } }
</style>
