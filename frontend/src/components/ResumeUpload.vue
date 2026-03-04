<template>
  <div class="resume-upload-card">
    <div class="upload-section">
      <el-upload
        class="resume-uploader"
        drag
        action="/api/interview/upload-resume"
        :on-success="handleSuccess"
        :on-error="handleError"
        :limit="1"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将简历拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF, Word, TXT 格式。上传后将自动匹配最佳职位。
          </div>
        </template>
      </el-upload>
    </div>

    <!-- Results Section -->
    <div v-if="analysis" class="analysis-results">
      <el-divider>匹配结果</el-divider>
      <div class="match-info">
        <el-tag type="success" size="large">自动匹配职位: {{ matchedJob }}</el-tag>
      </div>
      <div class="analysis-content" v-html="formattedAnalysis"></div>
      <div class="actions">
        <el-button type="primary" size="large" @click="startInterview">进入面试间</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

const emit = defineEmits(['start'])
const analysis = ref('')
const sessionId = ref('')
const matchedJob = ref('')

const handleSuccess = (res) => {
  sessionId.value = res.sessionId
  analysis.value = res.analysis
  matchedJob.value = res.matchedJob
  ElMessage.success('简历上传成功并已自动匹配职位')
}

const formattedAnalysis = computed(() => {
  return analysis.value ? marked(analysis.value) : ''
})

const startInterview = () => {
  emit('start', {
    sessionId: sessionId.value,
    analysis: analysis.value,
    jobTitle: matchedJob.value
  })
}
</script>

<style scoped>
.resume-upload-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.match-info {
  text-align: center;
  margin: 20px 0;
}
.analysis-content {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.6;
  color: var(--text-primary);
}

.start-btn {
  margin-top: 10px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
