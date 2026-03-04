<template>
  <div class="interview-viewport">
    <!-- Left: Digital Human -->
    <div class="avatar-column glass-card">
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
        <el-input
          v-model="userInput"
          placeholder="Speak your mind..."
          @keyup.enter="sendMessage"
          :disabled="isAIThinking"
        >
          <template #append>
            <el-button @click="sendMessage" :loading="isAIThinking">Send</el-button>
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

const props = defineProps({
  sessionId: String,
  analysis: String
})

const messages = ref([
  { role: 'assistant', content: 'Connecting to AI Interviewer...' }
])
const userInput = ref('')
const isAIThinking = ref(false)
const isSpeaking = ref(false)
const status = ref({ type: 'online', text: 'Initializing...' })
const messageContainer = ref(null)

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
    isSpeaking.value = true
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      // If it was just the "Connecting..." message, replace it
      if (lastMsg.content.includes('Connecting')) {
          lastMsg.content = data.content
      } else {
          lastMsg.content += data.content
      }
    } else {
      messages.value.push({ role: 'assistant', content: data.content })
    }
    scrollToBottom()
  } else if (data.type === 'complete') {
    isSpeaking.value = false
  } else if (data.type === 'error') {
    isAIThinking.value = false
    isSpeaking.value = false
    status.value = { type: 'error', text: 'Connection Error' }
  }
}

const sendMessage = () => {
  if (!userInput.value.trim() || isAIThinking.value || isSpeaking.value) return
  
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
}
</style>
