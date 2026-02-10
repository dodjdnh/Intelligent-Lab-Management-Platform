<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span style="font-weight: bold;">实验室预约管理</span>
        <el-button type="primary" @click="openDialog">新增预约</el-button>
      </div>
    </template>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="labName" label="实验室名称" />
      <el-table-column prop="user" label="预约人" />
      <el-table-column prop="userNo" label="学号/工号" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" v-if="isAdmin">
        <template #default="scope">
          <div v-if="scope.row.status === '审核中'">
            <el-button size="small" type="success" @click="handleAudit(scope.row.id, '已通过')">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(scope.row.id, '已驳回')">驳回</el-button>
          </div>
          <span v-else style="color: #999; font-size: 12px;">已完成</span>
        </template>
      </el-table-column>

    </el-table>

    <el-dialog v-model="dialogVisible" title="填写预约申请" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实验室">
          <el-select v-model="form.labName" placeholder="请选择实验室" style="width: 100%">
            <el-option label="人工智能实验室" value="人工智能实验室" />
            <el-option label="微电子实验室" value="微电子实验室" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约人">
          <el-input v-model="form.user" disabled placeholder="自动获取当前登录用户" />
        </el-form-item>
        <el-form-item label="学号/工号">
          <el-input v-model="form.userNo" placeholder="请输入您的学号或工号" />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker 
            v-model="form.date" 
            type="date" 
            value-format="YYYY-MM-DD" 
            placeholder="请选择未来日期" 
            style="width: 100%" 
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">提交申请</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const dialogVisible = ref(false)
const tableData = ref([])

// === 核心逻辑 1: 判断当前是不是管理员 ===
const isAdmin = computed(() => {
  return localStorage.getItem('role') === 'admin'
})

// === 状态颜色辅助函数 ===
const getStatusType = (status) => {
  if (status === '已通过') return 'success'
  if (status === '已驳回') return 'danger'
  return 'warning' // 审核中
}

const form = ref({
  labName: '',
  user: '',
  userNo: '',
  date: ''
})

const disabledDate = (time) => {
  return time.getTime() < Date.now()
}

const openDialog = () => {
  const currentUser = localStorage.getItem('userName')
  form.value = { labName: '', user: currentUser || '', userNo: '', date: '' }
  dialogVisible.value = true
}

const fetchList = async () => {
  try {
    const res = await request.get('/appointment/list')
    if (res.code === 200) {
      tableData.value = res.data.map(item => ({
        ...item,
        user: item.userName,
        date: item.reserveDate,
        userNo: item.userNo
      }))
    }
  } catch (error) {
    console.error('获取列表失败', error)
  }
}

// === 核心逻辑 2: 审核操作 ===
const handleAudit = async (id, status) => {
  try {
    const res = await request.post('/appointment/audit', {
      id: id,
      status: status
    })
    if (res.code === 200) {
      ElMessage.success(`审核操作成功：${status}`)
      fetchList() // 刷新列表，按钮会消失变成“已完成”
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchList()
})

const handleConfirm = async () => {
  if (form.value.labName && form.value.date && form.value.userNo) {
    try {
      const res = await request.post('/appointment/add', form.value)
      if (res.code === 200) {
        ElMessage.success('预约申请已提交')
        dialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.msg)
      }
    } catch (error) {
      ElMessage.error('提交异常')
    }
  } else {
    ElMessage.error('请填写完整信息')
  }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>