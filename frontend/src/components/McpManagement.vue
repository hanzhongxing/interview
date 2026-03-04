<template>
  <div class="management-container">
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">新增 MCP 服务</el-button>
    </div>
    
    <el-table :data="configs" style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="服务名称" width="200" />
      <el-table-column prop="sseUrl" label="SSE URL" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑 MCP 服务' : '新增 MCP 服务'" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="服务名称">
          <el-input v-model="editForm.name" placeholder="例如: Search Engine" />
        </el-form-item>
        <el-form-item label="SSE URL">
          <el-input v-model="editForm.sseUrl" placeholder="http://localhost:8080/mcp/sse" />
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

const configs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editForm = ref({
  id: null,
  name: '',
  sseUrl: ''
})

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/mcp-configs')
    configs.value = res.data
  } catch (error) {
    ElMessage.error('获取 MCP 配置失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editForm.value = { id: null, name: '', sseUrl: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!editForm.value.name || !editForm.value.sseUrl) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await axios.post('/api/mcp-configs', editForm.value)
    ElMessage.success('保存成功，需重启服务以加载新工具')
    dialogVisible.value = false
    fetchConfigs()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此服务配置吗？')
    await axios.delete(`/api/mcp-configs/${id}`)
    ElMessage.success('删除成功')
    fetchConfigs()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(fetchConfigs)
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
