<template>
  <div class="devices-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备中心</span>
          <div>
            <el-button type="primary" @click="openDialog()">绑定设备</el-button>
            <el-button type="primary" text @click="fetchDevices">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="devices" border stripe>
        <el-table-column prop="deviceId" label="设备编号" width="180" />
        <el-table-column prop="area" label="区域" width="120" />
        <el-table-column prop="consumableName" label="绑定耗材" width="180" />
        <el-table-column prop="topic" label="MQTT Topic" width="220" />
        <el-table-column prop="conversionMode" label="换算模式" width="140" />
        <el-table-column prop="unitWeight" label="单件重量" width="120" />
        <el-table-column prop="tareWeight" label="皮重" width="100" />
        <el-table-column prop="lastValue" label="最近值" width="120" />
        <el-table-column prop="transport" label="通信方式" width="120" />
        <el-table-column prop="lastSeen" label="最近上报" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在线' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="openDialog(scope.row)">编辑绑定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="设备绑定" width="460px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备编号">
          <el-input v-model="form.deviceId" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="绑定耗材">
          <el-select v-model="form.consumableId" clearable style="width: 100%">
            <el-option
              v-for="item in consumables"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="form.area" />
        </el-form-item>
        <el-form-item label="MQTT Topic">
          <el-input v-model="form.topic" placeholder="lab/devices/xxx" />
        </el-form-item>
        <el-form-item label="换算模式">
          <el-select v-model="form.conversionMode" style="width: 100%">
            <el-option label="直接取值" value="DIRECT_VALUE" />
            <el-option label="重量换算数量" value="WEIGHT_TO_COUNT" />
          </el-select>
        </el-form-item>
        <el-form-item label="单件重量">
          <el-input-number v-model="form.unitWeight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="皮重">
          <el-input-number v-model="form.tareWeight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBinding">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const devices = ref([])
const consumables = ref([])
const dialogVisible = ref(false)
const form = ref({
  id: null,
  deviceId: '',
  consumableId: null,
  area: '',
  topic: '',
  conversionMode: 'DIRECT_VALUE',
  unitWeight: null,
  tareWeight: null
})
const streamBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
let eventSource = null

const fetchDevices = async () => {
  try {
    const res = await request.get('/device/list')
    if (res.code === 200) devices.value = res.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取设备列表失败')
  }
}

const fetchConsumables = async () => {
  try {
    const res = await request.get('/consumable/list')
    if (res.code === 200) consumables.value = res.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取耗材列表失败')
  }
}

const openDialog = (row = null) => {
  if (row) {
    form.value = {
      id: row.id || null,
      deviceId: row.deviceId || '',
      consumableId: row.consumableId || null,
      area: row.area || '',
      topic: row.topic || '',
      conversionMode: row.conversionMode || 'DIRECT_VALUE',
      unitWeight: row.unitWeight || null,
      tareWeight: row.tareWeight || null
    }
  } else {
    form.value = {
      id: null,
      deviceId: '',
      consumableId: null,
      area: '',
      topic: '',
      conversionMode: 'DIRECT_VALUE',
      unitWeight: null,
      tareWeight: null
    }
  }
  dialogVisible.value = true
}

const saveBinding = async () => {
  try {
    const res = await request.post('/device/binding/save', form.value)
    if (res.code === 200) {
      ElMessage.success('绑定已保存')
      dialogVisible.value = false
      fetchDevices()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '保存绑定失败')
  }
}

onMounted(() => {
  fetchDevices()
  fetchConsumables()
  const token = localStorage.getItem('satoken')
  if (token) {
    eventSource = new EventSource(`${streamBaseUrl}/stream/events?satoken=${encodeURIComponent(token)}`)
    eventSource.addEventListener('device.status.changed', fetchDevices)
  }
})

onUnmounted(() => {
  eventSource?.close()
})
</script>

<style scoped>
.devices-page { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
