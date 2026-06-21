<template>
  <div class="container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">📦 耗材库存列表</span>
          <el-button v-if="isAdmin" type="primary" @click="dialogVisible = true">新建入库</el-button>
        </div>
      </template>

      <el-table :data="stockList" border stripe height="300px">
        <el-table-column prop="name" label="耗材名称" />
        <el-table-column prop="specification" label="规格" />
        <el-table-column prop="count" label="当前库存">
          <template #default="scope">
            <span :style="{ color: scope.row.count < 10 ? 'red' : 'green', fontWeight: 'bold' }">
              {{ scope.row.count }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openApply(scope.row)">申请领用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">📝 领用申请记录</span>
        </div>
      </template>

      <el-table :data="applyList" border stripe>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column prop="consumableName" label="耗材名称" />
        <el-table-column prop="num" label="申请数量" width="100" />
        <el-table-column prop="userName" label="申请人" />
        <el-table-column prop="userNo" label="学号/工号" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="审核" width="180" v-if="isAdmin">
          <template #default="scope">
            <div v-if="scope.row.status === '审核中'">
              <el-button size="small" type="success" @click="handleAudit(scope.row.id, '已通过')">通过</el-button>
              <el-button size="small" type="danger" @click="handleAudit(scope.row.id, '已驳回')">驳回</el-button>
            </div>
            <span v-else style="color: #ccc">已归档</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">📊 库存变更日志</span>
          <el-button type="primary" text @click="fetchAll">刷新</el-button>
        </div>
      </template>

      <el-table :data="changeLogs" border stripe max-height="320">
        <el-table-column prop="createdAt" label="变更时间" width="180" />
        <el-table-column prop="consumableName" label="耗材名称" min-width="140" />
        <el-table-column prop="changeType" label="类型" width="150" />
        <el-table-column prop="beforeCount" label="变更前" width="90" />
        <el-table-column prop="afterCount" label="变更后" width="90" />
        <el-table-column prop="deltaCount" label="差值" width="90">
          <template #default="scope">
            <span :style="{ color: scope.row.deltaCount >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ scope.row.deltaCount >= 0 ? '+' : '' }}{{ scope.row.deltaCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备ID" width="120" />
        <el-table-column prop="operator" label="操作者" width="100" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="耗材入库" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"> <el-input v-model="form.name" /> </el-form-item>
        <el-form-item label="规格"> <el-input v-model="form.specification" /> </el-form-item>
        <el-form-item label="数量"> <el-input-number v-model="form.count" :min="1" /> </el-form-item>
        <el-form-item label="单位"> <el-input v-model="form.unit" /> </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const stockList = ref([]) // 库存列表
const applyList = ref([]) // 申请记录
const changeLogs = ref([]) // 库存变更
const dialogVisible = ref(false)
const form = ref({ name: '', specification: '', count: 10, unit: '' })

// 判断是否管理员
const isAdmin = computed(() => localStorage.getItem('role') === 'admin')

// 获取两个列表数据
const fetchAll = async () => {
  try {
    const [res1, res2, res3] = await Promise.all([
      request.get('/consumable/list'),
      request.get('/consumable/apply-list'),
      request.get('/consumable/change-log')
    ])
    if (res1.code === 200) stockList.value = res1.data
    if (res2.code === 200) applyList.value = res2.data
    if (res3.code === 200) changeLogs.value = res3.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取耗材数据失败')
  }
}

// 提交申请 (弹窗询问数量)
const openApply = (row) => {
  ElMessageBox.prompt(`申请领用：${row.name} (当前库存:${row.count})`, '填写数量', {
    confirmButtonText: '提交申请',
    cancelButtonText: '取消',
    inputPattern: /^[1-9]\d*$/,
    inputErrorMessage: '请输入正整数'
  }).then(async ({ value }) => {
    const num = parseInt(value)
    if (num > row.count) return ElMessage.warning('申请数量不能超过当前库存')
    
    const res = await request.post('/consumable/apply', { id: row.id, num: num })
    if (res.code === 200) {
      ElMessage.success('申请已提交')
      fetchAll() // 刷新下面的记录表
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(() => {})
}

// 管理员审核
const handleAudit = async (id, status) => {
  try {
    const res = await request.post('/consumable/audit', { id, status })
    if (res.code === 200) {
      ElMessage.success('审核完成')
      fetchAll()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '审核失败')
  }
}

// 入库
const handleAdd = async () => {
  try {
    const res = await request.post('/consumable/add', form.value)
    if (res.code === 200) {
      ElMessage.success(res.msg || '入库成功')
      dialogVisible.value = false
      form.value = { name: '', specification: '', count: 10, unit: '' }
      fetchAll()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '入库失败')
  }
}

const getStatusType = (status) => {
  if (status === '已通过') return 'success'
  if (status === '已驳回') return 'danger'
  return 'warning'
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
