<template>
  <div class="assistant-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header">
              <span>智能摘要</span>
              <el-button type="primary" text @click="fetchAll">刷新</el-button>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="耗材种类">{{ inventory.totalKinds }}</el-descriptions-item>
            <el-descriptions-item label="预警耗材">{{ inventory.warningKinds }}</el-descriptions-item>
            <el-descriptions-item label="待审核预约">{{ pending.pendingAppointment }}</el-descriptions-item>
            <el-descriptions-item label="待审核领用">{{ pending.pendingApply }}</el-descriptions-item>
            <el-descriptions-item label="今日预约">{{ pending.todayAppointment }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="panel-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>快捷执行</span>
              <el-tag size="small">{{ chatMode }}</el-tag>
            </div>
          </template>

          <div class="action-block">
            <div class="action-title">创建预约</div>
            <el-form label-position="top" :model="appointmentForm">
              <el-form-item label="实验室">
                <el-select v-model="appointmentForm.labName" placeholder="选择实验室">
                  <el-option v-for="item in labs" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期">
                <el-date-picker
                  v-model="appointmentForm.date"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择预约日期"
                />
              </el-form-item>
              <el-button type="primary" :loading="executing" @click="executeAppointment">提交预约</el-button>
            </el-form>
          </div>

          <div class="action-block">
            <div class="action-title">提交领用</div>
            <el-form label-position="top" :model="applyForm">
              <el-form-item label="耗材">
                <el-select v-model="applyForm.id" placeholder="选择耗材">
                  <el-option
                    v-for="item in consumables"
                    :key="item.id"
                    :label="`${item.name} / 库存 ${item.count}${item.unit}`"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="applyForm.num" :min="1" />
              </el-form-item>
              <el-button type="success" :loading="executing" @click="executeApply">提交领用</el-button>
            </el-form>
          </div>

          <div v-if="isAdmin" class="action-block">
            <div class="action-title">确认告警</div>
            <el-form label-position="top" :model="alertForm">
              <el-form-item label="待确认告警">
                <el-select v-model="alertForm.id" placeholder="选择告警">
                  <el-option
                    v-for="item in pendingAlerts"
                    :key="item.id"
                    :label="`${item.title} / ${item.level}`"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-button type="warning" :loading="executing" @click="executeAlertAck">确认告警</el-button>
            </el-form>
          </div>

          <el-input
            v-if="executeOutput"
            type="textarea"
            :rows="10"
            :model-value="executeOutput"
            readonly
          />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="panel-card">
          <template #header>
            <div class="card-header">
              <span>智能助手对话</span>
              <el-button type="primary" text @click="fetchTools">刷新工具</el-button>
            </div>
          </template>
          <div class="chat-box">
            <div v-for="(item, index) in messages" :key="index" class="chat-item" :class="item.role">
              <div class="role">{{ item.role === 'user' ? '你' : '助手' }}</div>
              <div class="content">{{ item.content }}</div>
            </div>
          </div>
          <div class="chat-actions">
            <el-input
              v-model="question"
              type="textarea"
              :rows="3"
              placeholder="例如：总结最近库存变化、列出当前告警、分析今天待审批任务。"
            />
            <el-button type="primary" :loading="sending" @click="sendQuestion">发送</el-button>
          </div>

          <div class="tool-area">
            <div class="tool-header">工具调用</div>
            <div class="tool-buttons">
              <el-button
                v-for="tool in tools"
                :key="tool.name"
                size="small"
                @click="invokeTool(tool.name)"
              >
                {{ tool.description }}
              </el-button>
            </div>
            <el-input
              v-if="toolOutput"
              type="textarea"
              :rows="12"
              :model-value="toolOutput"
              readonly
            />
          </div>
        </el-card>

        <el-card class="panel-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>Nacos 配置中心</span>
              <el-button type="primary" text @click="fetchNacos">刷新</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="启用">{{ nacos.enabled }}</el-descriptions-item>
            <el-descriptions-item label="已连接">{{ nacos.connected }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ nacos.serverAddr || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Group">{{ nacos.group || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Data ID">{{ nacos.dataId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Readiness">{{ nacos.readiness || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-table
            style="margin-top: 16px;"
            :data="nacosCompare.items || []"
            border
            max-height="360"
          >
            <el-table-column prop="key" label="配置键" min-width="180" />
            <el-table-column prop="localValue" label="本地值" min-width="180" show-overflow-tooltip />
            <el-table-column prop="remoteValue" label="远端值" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="compareTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-input
            v-if="nacosEffective"
            style="margin-top: 16px;"
            type="textarea"
            :rows="10"
            :model-value="nacosEffective"
            readonly
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const inventory = ref({ totalKinds: 0, warningKinds: 0 })
const pending = ref({ pendingAppointment: 0, pendingApply: 0, todayAppointment: 0 })
const tools = ref([])
const consumables = ref([])
const alerts = ref([])
const question = ref('')
const sending = ref(false)
const executing = ref(false)
const chatMode = ref('local')
const toolOutput = ref('')
const executeOutput = ref('')
const nacos = ref({})
const nacosCompare = ref({ items: [] })
const nacosEffective = ref('')
const messages = ref([
  { role: 'assistant', content: '可以直接问我库存、待审批任务、设备状态、告警和配置差异，也可以在左侧直接执行预约、领用和告警确认。' }
])

const labs = ['人工智能实验室', '微电子实验室', '网络安全实验室', '机器人实验室']
const appointmentForm = ref({
  labName: '人工智能实验室',
  date: '',
  userNo: localStorage.getItem('userNo') || '',
  user: localStorage.getItem('userName') || ''
})
const applyForm = ref({ id: null, num: 1 })
const alertForm = ref({ id: '' })

const isAdmin = computed(() => localStorage.getItem('role') === 'admin')
const pendingAlerts = computed(() => alerts.value.filter(item => item.status !== '已确认'))

const fetchAll = async () => {
  try {
    const [inventoryRes, pendingRes, consumableRes, alertRes] = await Promise.all([
      request.get('/agent/summary/inventory'),
      request.get('/agent/summary/pending'),
      request.get('/consumable/list'),
      request.get('/alert/list')
    ])

    if (inventoryRes.code === 200) inventory.value = inventoryRes.data
    if (pendingRes.code === 200) pending.value = pendingRes.data
    if (consumableRes.code === 200) consumables.value = consumableRes.data
    if (alertRes.code === 200) alerts.value = alertRes.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取智能助手摘要失败')
  }
}

const fetchTools = async () => {
  try {
    const res = await request.get('/agent/tools')
    if (res.code === 200) tools.value = res.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取工具列表失败')
  }
}

const invokeTool = async (name) => {
  try {
    const res = await request.get('/agent/tool/invoke', { params: { name } })
    if (res.code === 200) {
      toolOutput.value = JSON.stringify(res.data, null, 2)
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '工具调用失败')
  }
}

const fetchNacos = async () => {
  try {
    const [statusRes, effectiveRes, compareRes] = await Promise.all([
      request.get('/nacos/status'),
      request.get('/nacos/effective'),
      request.get('/nacos/compare')
    ])
    if (statusRes.code === 200) nacos.value = statusRes.data
    if (effectiveRes.code === 200) nacosEffective.value = JSON.stringify(effectiveRes.data, null, 2)
    if (compareRes.code === 200) nacosCompare.value = compareRes.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取 Nacos 状态失败')
  }
}

const sendQuestion = async () => {
  if (!question.value.trim()) {
    ElMessage.error('请输入问题')
    return
  }

  const userMessage = question.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  sending.value = true

  try {
    const res = await request.post('/agent/chat', { message: userMessage })
    if (res.code === 200) {
      chatMode.value = res.data.mode
      messages.value.push({ role: 'assistant', content: res.data.answer })
      if (res.data.payload) {
        toolOutput.value = JSON.stringify(res.data.payload, null, 2)
      }
      question.value = ''
      fetchAll()
      fetchNacos()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '智能助手调用失败')
  } finally {
    sending.value = false
  }
}

const executeTool = async (name, args) => {
  executing.value = true
  try {
    const res = await request.post('/agent/tool/execute', { name, args })
    if (res.code === 200) {
      executeOutput.value = JSON.stringify(res.data, null, 2)
      ElMessage.success(res.data.message || '执行成功')
      await fetchAll()
      await fetchNacos()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '执行失败')
  } finally {
    executing.value = false
  }
}

const executeAppointment = async () => {
  if (!appointmentForm.value.labName || !appointmentForm.value.date) {
    ElMessage.error('请填写完整预约信息')
    return
  }
  await executeTool('appointment.create', {
    ...appointmentForm.value,
    userNo: localStorage.getItem('userNo') || '',
    user: localStorage.getItem('userName') || ''
  })
}

const executeApply = async () => {
  if (!applyForm.value.id || !applyForm.value.num) {
    ElMessage.error('请选择耗材并填写数量')
    return
  }
  await executeTool('consumable.apply', applyForm.value)
}

const executeAlertAck = async () => {
  if (!alertForm.value.id) {
    ElMessage.error('请选择待确认告警')
    return
  }
  await executeTool('alert.ack', alertForm.value)
}

const compareTagType = (status) => {
  if (status === '一致') return 'success'
  if (status === '不一致') return 'danger'
  if (status === '仅远端') return 'warning'
  if (status === '仅本地') return 'info'
  return ''
}

onMounted(() => {
  fetchAll()
  fetchTools()
  fetchNacos()
})
</script>

<style scoped>
.assistant-page { padding: 0; }
.panel-card { border-radius: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.chat-box {
  min-height: 300px;
  max-height: 430px;
  overflow-y: auto;
  padding: 8px 0;
}
.chat-item { margin-bottom: 12px; }
.chat-item .role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.chat-item .content {
  white-space: pre-wrap;
  line-height: 1.6;
  padding: 10px 12px;
  border-radius: 10px;
}
.chat-item.user .content { background: #ecf5ff; }
.chat-item.assistant .content { background: #f5f7fa; }
.chat-actions {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: end;
}
.tool-area { margin-top: 16px; }
.tool-header {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}
.tool-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.action-block {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}
.action-block:last-of-type {
  border-bottom: 0;
}
.action-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
</style>
