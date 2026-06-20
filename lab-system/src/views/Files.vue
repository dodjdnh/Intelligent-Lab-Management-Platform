<template>
  <div class="files-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件中心</span>
          <el-button type="primary" @click="triggerUpload">上传文件</el-button>
          <input ref="fileInput" type="file" style="display: none" @change="handleFileChange" />
        </div>
      </template>

      <el-table :data="files" border stripe>
        <el-table-column prop="name" label="文件名" />
        <el-table-column prop="size" label="大小">
          <template #default="scope">{{ formatSize(scope.row.size) }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column prop="path" label="存储路径" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const files = ref([])
const fileInput = ref(null)

const fetchFiles = async () => {
  try {
    const res = await request.get('/file/list')
    if (res.code === 200) files.value = res.data
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '获取文件列表失败')
  }
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      ElMessage.success('文件上传成功')
      fetchFiles()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '文件上传失败')
  } finally {
    event.target.value = ''
  }
}

const formatSize = (size) => {
  if (!size && size !== 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  return `${(size / (1024 * 1024)).toFixed(2)} MB`
}

onMounted(fetchFiles)
</script>

<style scoped>
.files-page { padding: 0; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
