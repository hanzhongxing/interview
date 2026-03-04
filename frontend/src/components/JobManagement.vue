<template>
  <div class="management-container">
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增职位</el-button>
    </div>
    
    <el-table :data="jobs" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="职位名称" width="200" />
      <el-table-column prop="description" label="职位描述" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑职位' : '新增职位'" width="600px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="职位名称">
          <el-input v-model="editForm.title" placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="职位描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="10"
            placeholder="请输入职位描述和任职要求..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const jobs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editForm = ref({
  id: null,
  title: '',
  description: ''
})

const fetchJobs = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8086/api/jobs')
    jobs.value = res.data
  } catch (error) {
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editForm.value = { id: null, title: '', description: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!editForm.value.title || !editForm.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await axios.post('http://localhost:8086/api/jobs', editForm.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchJobs()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此职位吗？')
    await axios.delete(`http://localhost:8086/api/jobs/${id}`)
    ElMessage.success('删除成功')
    fetchJobs()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchJobs)
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
