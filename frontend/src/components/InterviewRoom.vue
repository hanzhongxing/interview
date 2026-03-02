<template>
  <div class="interview-viewport">
    <!-- Left: Digital Human -->
    <div class="avatar-column glass-card">
      <div class="avatar-container">
        <div class="avatar-placeholder" :class="{ 'is-speaking': isSpeaking }">
          <div class="avatar-circle"></div>
          <div class="voice-waves" v-if="isSpeaking">
            <span></span><span></span><span></span><span></span>
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
  sessionId: String
})

const messages = ref([
  { role: 'assistant', content: 'Hello! I have reviewed your resume. Let\'s begin the interview. Could you please introduce yourself?' }
])
const userInput = ref('')
const isAIThinking = ref(false)
const isSpeaking = ref(false)
const status = ref({ type: 'online', text: 'Interviewer Online' })
const messageContainer = ref(null)

let stompClient = null

onMounted(() => {
  connectWebSocket()
})

const connectWebSocket = () => {
  const socket = new SockJS('/ws-interview')
  stompClient = Stomp.over(socket)
  stompClient.debug = null // Silence debug messages
  
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/interview/' + props.sessionId, (message) => {
      const data = JSON.parse(message.body)
      handleIncomingContent(data)
    })
  })
}

const handleIncomingContent = (data) => {
  if (data.type === 'delta') {
    isSpeaking.value = true
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content += data.content
    } else {
      messages.value.push({ role: 'assistant', content: data.content })
    }
    scrollToBottom()
  } else if (data.type === 'complete') {
    isAIThinking.value = false
    isSpeaking.value = false
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

.voice-waves span:nth-child(2) { animation-delay: 0.1s; }
.voice-waves span:nth-child(3) { animation-delay: 0.2s; }
.voice-waves span:nth-child(4) { animation-delay: 0.3s; }

.status-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
}
.status-dot.online { background: #10b981; }

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
