<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span style="font-weight: bold;">人员权限管理</span>
        <el-button type="primary" @click="openDialog()">+ 新增用户</el-button>
      </div>
    </template>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="姓名" />
      <el-table-column prop="userNo" label="学号/工号" />
      <el-table-column prop="role" label="角色">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
            {{ scope.row.role === 'admin' ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleResetPwd(scope.row)">重置密码</el-button>
          <el-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.username" placeholder="登录用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.userNo" placeholder="请输入学号/工号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码">
          <el-input v-model="form.password" placeholder="默认 123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({ id: null, username: '', userNo: '', role: 'student', password: '' })

// 1. 获取列表
const fetchList = async () => {
  const res = await request.get('/user/list')
  if (res.code === 200) tableData.value = res.data
}

// 2. 打开弹窗 (新增或编辑)
const openDialog = (row = null) => {
  if (row) {
    // 编辑模式：回填数据
    form.value = { ...row } // 复制对象
  } else {
    // 新增模式：清空数据
    form.value = { id: null, username: '', userNo: '', role: 'student', password: '' }
  }
  dialogVisible.value = true
}

// 3. 保存 (新增/更新)
const handleSave = async () => {
  if (!form.value.username || !form.value.role) return ElMessage.error('请填写完整信息')
  
  const res = await request.post('/user/save', form.value)
  if (res.code === 200) {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchList()
  } else {
    ElMessage.error(res.msg)
  }
}

// 4. 删除
const handleDelete = async (id) => {
  const res = await request.delete(`/user/delete/${id}`)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchList()
  }
}

// 5. 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确定要将 [${row.username}] 的密码重置为 123456 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await request.post('/user/reset-pwd', { id: row.id })
    if (res.code === 200) ElMessage.success('密码重置成功')
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>