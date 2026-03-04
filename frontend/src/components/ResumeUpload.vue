<template>
  <div class="glass-card upload-container">
    <h2 class="gradient-text">Welcome to AI Interview</h2>
    <p class="description">Please provide the Job Description and upload your resume.</p>
    
    <div class="jd-section">
      <el-input
        v-model="jd"
        type="textarea"
        :rows="4"
        placeholder="Paste Job Description here (Optional)..."
      />
    </div>

    <el-upload
      class="upload-demo"
      drag
      action="/api/interview/upload-resume"
      :data="{ jd: jd }"
      :on-success="handleSuccess"
      :on-error="handleError"
      :limit="1"
      accept=".pdf,.doc,.docx,.txt"
      v-if="!analysisResults"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        Drop resume here or <em>click to upload</em>
      </div>
    </el-upload>

    <div v-if="analysisResults" class="analysis-results glass-card">
      <h3>Resume Analysis Report</h3>
      <div class="analysis-content" v-html="formattedAnalysis"></div>
      <el-button type="primary" @click="startInterview" class="start-btn">Start Interview</el-button>
      <el-button @click="reset">Upload Another</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['uploaded', 'start'])

const jd = ref('')
const analysisResults = ref(null)
const uploadResponse = ref(null)

const handleSuccess = (response) => {
  ElMessage.success('Resume uploaded and analyzed successfully!')
  analysisResults.value = response.analysis
  uploadResponse.value = response
  emit('uploaded', response)
}

const handleError = (err) => {
  ElMessage.error('Failed to upload resume: ' + err.message)
}

const formattedAnalysis = computed(() => {
  return analysisResults.value ? analysisResults.value.replace(/\n/g, '<br>') : ''
})

const startInterview = () => {
  emit('start', uploadResponse.value)
}

const reset = () => {
  analysisResults.value = null
  uploadResponse.value = null
}
</script>

<style scoped>
.upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 500px;
  margin: 0 auto;
}

.description {
  color: var(--text-secondary);
  text-align: center;
}

.upload-demo {
  width: 100%;
}

.jd-section {
  width: 100%;
  margin-bottom: 20px;
}

.analysis-results {
  width: 100%;
  padding: 20px;
  text-align: left;
  animation: fadeIn 0.5s ease;
}

.analysis-content {
  margin: 15px 0;
  line-height: 1.6;
  font-size: 0.95rem;
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
