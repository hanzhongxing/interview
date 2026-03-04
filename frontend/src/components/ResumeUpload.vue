<template>
  <div class="resume-upload-card" v-loading="isAnalyzing" element-loading-text="正在分析简历并匹配岗位，请稍候...">
    <div class="upload-section">
      <el-upload
        class="resume-uploader"
        drag
        action="/api/interview/upload-resume"
        :on-success="handleSuccess"
        :on-error="handleError"
        :before-upload="handleBeforeUpload"
        :limit="1"
        :show-file-list="false"
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

    <!-- Match Results / Selection Dialog -->
    <el-dialog v-model="selectionVisible" title="匹配到多个适合岗位" width="500px">
      <div class="selection-list">
        <p>根据您的简历，我们发现了多个适合的职位，请选择您想申请的一个：</p>
        <el-radio-group v-model="selectedJobId" class="job-radio-group">
          <el-radio v-for="job in matchedJobs" :key="job.id" :label="job.id" border>
            {{ job.title }}
          </el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="selectionVisible = false">取消</el-button>
        <el-button type="primary" :loading="analyzing" @click="confirmSelection" :disabled="!selectedJobId">
          确认并开始面试
        </el-button>
      </template>
    </el-dialog>

    <!-- Results Section (Optional fallback if user wants to see analysis first, though requirements say auto-start) -->
    <div v-if="analysis && matchStatus === 'SINGLE_FALLBACK'" class="analysis-results">
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
import axios from 'axios'

const emit = defineEmits(['start'])
const analysis = ref('')
const sessionId = ref('')
const matchedJob = ref('')
const matchedJobs = ref([])
const matchStatus = ref('')
const fileName = ref('')

const selectionVisible = ref(false)
const selectedJobId = ref('')
const analyzing = ref(false)
const isAnalyzing = ref(false)

const handleBeforeUpload = () => {
  isAnalyzing.value = true
  return true
}

const handleSuccess = (res) => {
  isAnalyzing.value = false
  fileName.value = res.fileName
  sessionId.value = res.sessionId
  matchStatus.value = res.matchStatus

  if (res.matchStatus === 'SINGLE') {
    analysis.value = res.analysis
    matchedJob.value = res.matchedJob
    ElMessage.success('匹配到岗位：' + res.matchedJob + '，正在进入面试间...')
    startInterview()
  } else if (res.matchStatus === 'MULTIPLE') {
    matchedJobs.value = res.matches
    selectionVisible.value = true
  } else if (res.matchStatus === 'NONE') {
    ElMessage.warning('根据简历未能匹配到岗位')
  }
}

const handleError = () => {
  isAnalyzing.value = false
  ElMessage.error('上传失败，请稍后重试')
}

const confirmSelection = async () => {
  analyzing.value = true
  try {
    const res = await axios.post('/api/interview/select-job', {
      fileName: fileName.value,
      jobId: selectedJobId.value
    })
    analysis.value = res.data.analysis
    matchedJob.value = res.data.matchedJob
    selectionVisible.value = false
    ElMessage.success('已选择职位，准备开始面试')
    startInterview()
  } catch (error) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
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
  color: #303133;
}

.job-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 15px;
}

.job-radio-group :deep(.el-radio) {
  margin-right: 0;
  width: 100%;
}

.start-btn {
  margin-top: 10px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
