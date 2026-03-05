<template>
  <div class="interview-viewport">
    <!-- Left: Digital Human -->
    <div class="avatar-column glass-card">
      <div class="enable_speech">
        <el-switch v-model="enableSpeech" active-color="#13ce66" inactive-color="#ff4949" style="margin-bottom: 10px;">
          {{ enableSpeech ? '语音已启用' : '语音已禁用' }}
        </el-switch>
      </div>
      <div class="avatar-container">
        <div class="avatar-placeholder" :class="{ 'is-speaking': isSpeaking }">
          <div class="avatar-circle"></div>
          <div class="voice-waves" v-if="isSpeaking">
            <span></span><span></span><span></span><span></span><span></span><span></span><span></span>
          </div>
        </div>
        <div class="avatar-status">
          <span class="status-dot" :class="status.type"></span>
          {{ status.text }}
        </div>
      </div>
    </div>

    <!-- Right: Chat Interface -->
    <div class="chat-column glass-card">
      <div class="chat-messages" ref="messageContainer">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          :class="['message-bubble', msg.role]"
        >
          <div class="content">{{ msg.content }}</div>
        </div>
        <div v-if="isAIThinking" class="message-bubble assistant thinking">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
        </div>
      </div>
      
      <div class="chat-input">
        <div class="input-actions" v-if="!isAIThinking">
          <el-button 
            :type="isRecording ? 'danger' : 'primary'" 
            circle
            @mousedown="startRecording"
            @mouseup="stopRecording"
            @mouseleave="stopRecording"
            v-if="supportVOice"
          >
            <el-icon><microphone /></el-icon>
          </el-button>
          <span class="recording-hint" v-if="isRecording">松开结束...</span>
        </div>
        <el-input
          v-model="userInput"
          placeholder="既然来了，就聊两句吧..."
          @keyup.enter="sendMessage"
          :disabled="isAIThinking"
        >
          <template #append>
            <el-button @click="sendMessage" :loading="isAIThinking">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { Microphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  sessionId: String,
  analysis: String
})

const messages = ref([
  { role: 'assistant', content: '正在连接面试官...' }
])
const userInput = ref('')
const isAIThinking = ref(false)
const isSpeaking = ref(false)
const status = ref({ type: 'online', text: 'Initializing...' })
const messageContainer = ref(null)
const enableSpeech = ref(false)
const supportVOice = ref(!!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia))
const isRecording = ref(false)
let mediaRecorder = null
let audioChunks = []
let audioPlayer = null

let stompClient = null

onMounted(() => {
  connectWebSocket()
})

const connectWebSocket = () => {
  const socket = new SockJS('/ws-interview')
  stompClient = Stomp.over(socket)
  stompClient.debug = null
  
  stompClient.connect({}, () => {
    status.value = { type: 'online', text: 'Interviewer Ready' }
    stompClient.subscribe('/topic/interview/' + props.sessionId, (message) => {
      const data = JSON.parse(message.body)
      handleIncomingContent(data)
    })
    
    // Start interview with a greeting
    sendInitialMessage()
  })
}

const sendInitialMessage = () => {
  stompClient.send('/app/chat', {}, JSON.stringify({
    sessionId: props.sessionId,
    message: "你好，我已经准备好了。请基于我的简历开始面试。我的简历分析如下：" + props.analysis
  }))
  isAIThinking.value = true
}

const handleIncomingContent = (data) => {
  if (data.type === 'delta') {
    isAIThinking.value = false
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      if (lastMsg.content.includes('正在连接')) {
          lastMsg.content = data.content
      } else {
          lastMsg.content += data.content
      }
    } else {
      messages.value.push({ role: 'assistant', content: data.content })
    }
    scrollToBottom()
  } else if (data.type === 'complete') {
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      if(enableSpeech.value){
        playAIResponse(lastMsg.content)
      }
    }
  } else if (data.type === 'error') {
    isAIThinking.value = false
    isSpeaking.value = false
    status.value = { type: 'error', text: 'Connection Error' }
  }
}

const playAIResponse = async (text) => {
  if (!text) return
  try {
    const response = await fetch('/api/audio/speech', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text })
    })

    if (!response.ok) throw new Error('TTS request failed')

    const reader = response.body.getReader()
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)()
    
    // We expect 16kHz PCM 16bit mono (OpenAI default if not specified otherwise, 
    // but user manually set it, we'll assume standard web frequency for now)
    const SAMPLE_RATE = 24000 // OpenAI tts-1 pcm standard is usually 24k
    
    let startTime = audioCtx.currentTime
    isSpeaking.value = true

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      // Convert PCM 16-bit LE to Float32
      const pcm16 = new Int16Array(value.buffer, value.byteOffset, value.byteLength / 2)
      const float32 = new Float32Array(pcm16.length)
      for (let i = 0; i < pcm16.length; i++) {
        float32[i] = pcm16[i] / 32768
      }
      
      const audioBuffer = audioCtx.createBuffer(1, float32.length, SAMPLE_RATE)
      audioBuffer.getChannelData(0).set(float32)
      
      const source = audioCtx.createBufferSource()
      source.buffer = audioBuffer
      source.connect(audioCtx.destination)
      
      source.start(startTime)
      startTime += audioBuffer.duration
      
      source.onended = () => {
        // This is tricky for individual chunks. Better to track the whole stream.
      }
    }
    
    // Approximate end of speech
    setTimeout(() => {
        isSpeaking.value = false
    }, (startTime - audioCtx.currentTime) * 1000)

  } catch (error) {
    console.error('Failed to play AI response stream', error)
    isSpeaking.value = false
  }
}

let sttSocket = null

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    
    // Open STT WebSocket
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    // Use the same host as the app, but adjust for the STT endpoint
    const host = window.location.host
    sttSocket = new WebSocket(`${protocol}//${host}/ws/stt`)
    sttSocket.binaryType = 'arraybuffer'
    
    sttSocket.onmessage = (event) => {
      // Received partial transcription
      userInput.value = event.data
    }

    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
    
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0 && sttSocket.readyState === WebSocket.OPEN) {
        sttSocket.send(event.data)
      }
    }
    
    mediaRecorder.onstop = () => {
      if (sttSocket) {
        sttSocket.close()
      }
      stream.getTracks().forEach(track => track.stop())
    }
    
    // Capture every 500ms for more "real-time" feel
    mediaRecorder.start(500)
    isRecording.value = true
  } catch (err) {
    ElMessage.error('无法访问麦克风')
    console.error(err)
  }
}

const stopRecording = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
  }
}

const sendMessage = () => {
  if (!userInput.value.trim() || isAIThinking.value) return
  
  const msg = userInput.value
  messages.value.push({ role: 'user', content: msg })
  userInput.value = ''
  isAIThinking.value = true
  
  stompClient.send('/app/chat', {}, JSON.stringify({
    sessionId: props.sessionId,
    message: msg
  }))
  
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.interview-viewport {
  display: flex;
  gap: 20px;
  height: 100%;
}

.avatar-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.chat-column {
  flex: 1.5;
  display: flex;
  flex-direction: column;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.avatar-placeholder {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s;
}

.avatar-placeholder.is-speaking {
  box-shadow: 0 0 40px var(--primary-color);
  transform: scale(1.05);
}

.avatar-circle {
  width: 80%;
  height: 80%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
}

.voice-waves {
  position: absolute;
  bottom: -40px;
  display: flex;
  gap: 5px;
  height: 30px;
  align-items: center;
}

.voice-waves span {
  width: 4px;
  height: 10px;
  background: var(--primary-color);
  border-radius: 2px;
  animation: wave 0.5s ease-in-out infinite alternate;
}

@keyframes wave {
  from { height: 10px; }
  to { height: 30px; }
}

.status-dot.online { background: #10b981; }
.status-dot.error { background: #ef4444; }

.voice-waves span:nth-child(2) { animation-delay: 0.1s; }
.voice-waves span:nth-child(3) { animation-delay: 0.2s; }
.voice-waves span:nth-child(4) { animation-delay: 0.15s; }
.voice-waves span:nth-child(5) { animation-delay: 0.25s; }
.voice-waves span:nth-child(6) { animation-delay: 0.05s; }
.voice-waves span:nth-child(7) { animation-delay: 0.3s; }

.message-bubble.thinking {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 10px 16px;
}

.thinking .dot {
  width: 6px;
  height: 6px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: pulse 1s infinite alternate;
}

.thinking .dot:nth-child(2) { animation-delay: 0.2s; }
.thinking .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes pulse {
  from { opacity: 0.3; transform: scale(0.8); }
  to { opacity: 1; transform: scale(1.2); }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-bubble {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
}

.message-bubble.user {
  align-self: flex-end;
  background: var(--primary-color);
}

.message-bubble.assistant {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.1);
}

.chat-input {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.recording-hint {
  font-size: 14px;
  color: #ef4444;
  animation: blink 1s infinite alternate;
}

@keyframes blink {
  from { opacity: 0.5; }
  to { opacity: 1; }
}
</style>
