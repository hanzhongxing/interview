<template>
  <div class="glass-card upload-container">
    <h2 class="gradient-text">Welcome to AI Interview</h2>
    <p class="description">Please upload your resume to start the automated interview process.</p>
    
    <el-upload
      class="upload-demo"
      drag
      action="/api/interview/upload-resume"
      :on-success="handleSuccess"
      :on-error="handleError"
      :limit="1"
      accept=".pdf,.doc,.docx,.txt"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        Drop file here or <em>click to upload</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          PDF, DOCX or TXT files less than 10MB
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['uploaded'])

const handleSuccess = (response) => {
  ElMessage.success('Resume uploaded and processed successfully!')
  emit('uploaded', response) // filename/sessionId
}

const handleError = (err) => {
  ElMessage.error('Failed to upload resume: ' + err.message)
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
</style>
