<template>
  <el-container class="app-wrapper">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <h2 class="gradient-text">AI Interview</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        @select="handleMenuSelect"
      >
        <el-menu-item index="interview">
          <el-icon><ChatDotRound /></el-icon>
          <span>面试房间</span>
        </el-menu-item>
        <el-menu-item index="jobs">
          <el-icon><Briefcase /></el-icon>
          <span>职位管理</span>
        </el-menu-item>
        <el-menu-item index="mcp">
          <el-icon><Connection /></el-icon>
          <span>MCP服务</span>
        </el-menu-item>
        <el-menu-item index="kb">
          <el-icon><Collection /></el-icon>
          <span>知识库</span>
        </el-menu-item>
        <el-menu-item index="settings">
          <el-icon><Setting /></el-icon>
          <span>大模型配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h3>{{ menuTitle }}</h3>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <el-alert
          v-if="!isLlmConfigured"
          title="大模型未配置"
          type="warning"
          description="当前尚未配置可用的大模型，面试功能将无法使用。请前往配置页面进行设置。"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <el-button type="primary" size="small" @click="handleOpenSettings" style="margin-top: 10px;">
              立即配置
            </el-button>
          </template>
        </el-alert>

        <div v-if="activeMenu === 'interview'" class="content-view">
          <div v-if="!isInterviewStarted" class="setup-room">
            <ResumeUpload @start="handleStart" />
          </div>
          <div v-else class="interview-room">
            <InterviewRoom :sessionId="sessionId" :analysis="candidateAnalysis" />
          </div>
        </div>
        <div v-else-if="activeMenu === 'jobs'" class="content-view">
          <JobManagement />
        </div>
        <div v-else-if="activeMenu === 'mcp'" class="content-view">
          <McpManagement />
        </div>
        <div v-else-if="activeMenu === 'kb'" class="content-view">
          <KbManagement />
        </div>
      </el-main>
    </el-container>
    
    <LlmConfigDialog ref="settingsDialog" @saved="onConfigSaved" />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  ChatDotRound, Briefcase, Connection, 
  Collection, Setting, Warning
} from '@element-plus/icons-vue'
import axios from 'axios'
import ResumeUpload from './components/ResumeUpload.vue'
import InterviewRoom from './components/InterviewRoom.vue'
import LlmConfigDialog from './components/LlmConfigDialog.vue'
import JobManagement from './components/JobManagement.vue'
import McpManagement from './components/McpManagement.vue'
import KbManagement from './components/KbManagement.vue'

const activeMenu = ref('interview')
const isInterviewStarted = ref(false)
const sessionId = ref('')
const candidateAnalysis = ref('')
const settingsDialog = ref(null)
const isLlmConfigured = ref(true)

const menuTitle = computed(() => {
  const titles = {
    interview: 'AI 面试房间',
    jobs: '职位管理',
    mcp: 'MCP 服务维护',
    kb: '知识库维护',
    settings: '大模型配置'
  }
  return titles[activeMenu.value] || ''
})

const checkStatus = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/status')
    isLlmConfigured.value = response.data.llmConfigured
  } catch (error) {
    console.error('Failed to check system status', error)
  }
}

onMounted(() => {
  checkStatus()
})

const handleMenuSelect = (index) => {
  if (index === 'settings') {
    handleOpenSettings()
    return
  }
  activeMenu.value = index
}

const handleOpenSettings = () => {
  settingsDialog.value?.show()
}

const handleStart = (data) => {
  sessionId.value = data.sessionId
  candidateAnalysis.value = data.analysis
  isInterviewStarted.value = true
}

const onConfigSaved = () => {
  checkStatus()
}
</script>

<style>
/* Global styles for the app */
body {
  margin: 0;
  padding: 0;
  font-family: 'Inter', sans-serif;
  background-color: #f5f7fa;
}

.app-wrapper {
  height: 100vh;
}

.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.logo h2 {
  margin: 0;
  font-size: 1.5rem;
}

.el-menu-vertical {
  border-right: none !important;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 30px;
}

.header-content h3 {
  margin: 0;
  font-weight: 500;
  color: #303133;
}

.main-content {
  padding: 24px;
  overflow-y: auto;
}

.content-view {
  height: 100%;
}

.setup-room, .interview-room {
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.gradient-text {
  background: linear-gradient(45deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
