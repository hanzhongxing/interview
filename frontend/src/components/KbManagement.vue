<template>
  <div class="management-container">
    <div class="action-bar">
      <el-upload
        action="/api/kb/upload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :show-file-list="false"
      >
        <el-button type="primary">上传知识资料</el-button>
      </el-upload>
    </div>
    
    <el-table :data="files" style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="文件名" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="danger" @click="handleDelete(scope.row.name)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const files = ref([])
const loading = ref(false)

const fetchFiles = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/kb/files')
    files.value = res.data.map(name => ({ name }))
  } catch (error) {
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSuccess = () => {
  ElMessage.success('上传成功并已入库')
  fetchFiles()
}

const handleError = () => {
  ElMessage.error('上传失败')
}

const handleDelete = async (fileName) => {
  try {
    await ElMessageBox.confirm('确定删除此资料吗？')
    await axios.delete(`/api/kb/files/${fileName}`)
    ElMessage.success('删除成功')
    fetchFiles()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchFiles)
</script>

<style scoped>
.management-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.action-bar {
  margin-bottom: 20px;
}
</style>
