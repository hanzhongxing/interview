<template>
  <div class="app-container">
    <header class="header">
      <h1 class="gradient-text">AI Interview Room</h1>
    </header>
    <main class="main-content">
      <div v-if="!isInterviewStarted" class="setup-room">
        <ResumeUpload @start="handleStart" />
      </div>
      <div v-else class="interview-room">
        <InterviewRoom :sessionId="sessionId" :analysis="candidateAnalysis" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ResumeUpload from './components/ResumeUpload.vue'
import InterviewRoom from './components/InterviewRoom.vue'

const isInterviewStarted = ref(false)
const sessionId = ref('')
const candidateAnalysis = ref('')

const handleStart = (data) => {
  sessionId.value = data.sessionId
  candidateAnalysis.value = data.analysis
  isInterviewStarted.value = true
}
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 20px 40px;
  text-align: center;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.setup-room, .interview-room {
  width: 100%;
  max-width: 1200px;
  height: 90%;
}
</style>
