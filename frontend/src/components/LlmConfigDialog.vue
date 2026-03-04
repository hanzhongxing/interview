<template>
  <el-dialog
    v-model="dialogVisible"
    title="大模型配置"
    width="700px"
    @open="fetchConfigs"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="聊天模型" name="CHAT">
        <div class="model-list">
          <el-table :data="chatConfigs" style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="modelName" label="模型名称" />
            <el-table-column label="启用" width="80">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.active"
                  @change="handleActivate(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="editConfig(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteConfig(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="add-btn">
            <el-button type="primary" @click="addNewConfig('CHAT')">添加聊天模型</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="向量模型" name="VECTOR">
        <!-- ... existing vector content ... -->
        <div class="model-list">
          <el-table :data="vectorConfigs" style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="modelName" label="模型名称" />
            <el-table-column label="启用" width="80">
              <template #default="scope">
                <el-switch v-model="scope.row.active" @change="handleActivate(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="editConfig(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteConfig(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="add-btn">
            <el-button type="primary" @click="addNewConfig('VECTOR')">添加向量模型</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="语音合成 (TTS)" name="SPEECH">
        <div class="model-list">
          <el-table :data="speechConfigs" style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="modelName" label="模型名称" />
            <el-table-column label="启用" width="80">
              <template #default="scope">
                <el-switch v-model="scope.row.active" @change="handleActivate(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="editConfig(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteConfig(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="add-btn">
            <el-button type="primary" @click="addNewConfig('SPEECH')">添加语音合成模型</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="语音转文字 (STT)" name="TRANSCRIPTION">
        <div class="model-list">
          <el-table :data="transcriptionConfigs" style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="modelName" label="模型名称" />
            <el-table-column label="启用" width="80">
              <template #default="scope">
                <el-switch v-model="scope.row.active" @change="handleActivate(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="editConfig(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteConfig(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="add-btn">
            <el-button type="primary" @click="addNewConfig('TRANSCRIPTION')">添加语音识别模型</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Edit Form Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="editForm.id ? '编辑配置' : '新增配置'"
      width="500px"
      append-to-body
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="配置名称">
          <el-input v-model="editForm.name" placeholder="例如: DeepSeek, Ollama, GPT-4" />
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="editForm.baseUrl" placeholder="https://api.openai.com/v1" />
          <div class="form-tip">
            如果是 Ollama，通常是 http://localhost:11434/v1
          </div>
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="editForm.apiKey" type="password" placeholder="sk-..." />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="editForm.modelName" placeholder="例如: gpt-4, llama3, deepseek-chat" />
        </el-form-item>
        <el-form-item label="Temperature" v-if="editForm.modelType === 'CHAT'">
          <el-input-number v-model="editForm.temperature" :step="0.1" :min="0" :max="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const dialogVisible = ref(false)
const activeTab = ref('CHAT')
const configs = ref([])
const editVisible = ref(false)
const editForm = ref({
  id: '',
  name: '',
  modelType: 'CHAT',
  baseUrl: '',
  modelName: '',
  apiKey: '',
  temperature: 0.7,
  active: false
})

const chatConfigs = computed(() => configs.value.filter(c => c.modelType === 'CHAT'))
const vectorConfigs = computed(() => configs.value.filter(c => c.modelType === 'VECTOR'))
const speechConfigs = computed(() => configs.value.filter(c => c.modelType === 'SPEECH'))
const transcriptionConfigs = computed(() => configs.value.filter(c => c.modelType === 'TRANSCRIPTION'))

const fetchConfigs = async () => {
  try {
    const res = await axios.get('/api/llm-configs')
    configs.value = res.data
  } catch (error) {
    ElMessage.error('获取配置失败')
  }
}

const handleActivate = async (row) => {
  if (!row.active) {
    // If user tries to turn off the current active one, we should probably warn or just let it be (system will fail gracefully)
    // Actually, turn it back on and tell them they must have one active.
    fetchConfigs()
    return
  }
  try {
    await axios.post(`/api/llm-configs/${row.id}/activate`)
    ElMessage.success('已切换大模型，部分配置需重启服务生效')
    fetchConfigs()
  } catch (error) {
    ElMessage.error('激活失败')
    fetchConfigs()
  }
}

const addNewConfig = (modelType) => {
  editForm.value = {
    id:'',
    name: '',
    modelType: modelType,
    baseUrl: '',
    modelName: '',
    apiKey: '',
    temperature: 0.7,
    active: false
  }
  editVisible.value = true
}

const editConfig = (row) => {
  editForm.value = { ...row }
  editVisible.value = true
}

const saveConfig = async () => {
  if (!editForm.value.name || !editForm.value.baseUrl || !editForm.value.modelName) {
    ElMessage.warning('请填写完整必要信息')
    return
  }
  
  try {
    await axios.post('/api/llm-configs', editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchConfigs()
    // Trigger an event so App.vue can re-check status
    emit('saved')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const emit = defineEmits(['saved'])


const deleteConfig = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此配置吗？')
    await axios.delete(`/api/llm-configs/${id}`)
    ElMessage.success('删除成功')
    fetchConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

defineExpose({
  show: () => { dialogVisible.value = true }
})
</script>

<style scoped>

.model-list {
  padding: 10px 0;
}
.add-btn {
  margin-top: 20px;
  text-align: right;
}

.model-list {
  padding: 10px 0;
}
.add-btn {
  margin-top: 20px;
  text-align: right;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
