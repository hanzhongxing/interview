# AI Interview Room (AI 自动面试间) 🤖

[English](#english) | [中文](#中文)

---

<a name="english"></a>
## English

### 🌟 Project Overview
AI Interview Room is an intelligent, high-performance interview system built with **Spring Boot 3 + LangChain4j + Vue 3 + Element Plus**. It features a realistic digital human interface, automated resume analysis, and a professional interview process powered by RAG (Retrieval-Augmented Generation).

The system supports **Streaming AI Voice (TTS)** and **Real-time Voice Input (STT)** to provide a seamless, low-latency conversational experience.

### 🚀 Core Features
- **📄 Intelligent Resume Matching**: Automatically parses resumes and matches them against job requirements using vector search. Supports single-match auto-entry and multi-match selection.
- **🎙️ Real-time Streaming Voice**: 
    - **Streaming TTS**: AI responses are synthesized into PCM streams and played via Web Audio API for near-zero latency.
    - **Streaming STT**: User voice is streamed via binary WebSockets for real-time transcription ("Text-as-you-speak").
- **👤 Digital Human Interface**: Dynamic avatar with voice-reactive animations and modern "Glassmorphism" UI.
- **🧠 Advanced RAG Engine**: Context-aware interviewing based on local job descriptions, company culture, and technical requirements.
- **🛠️ Dynamic Configuration**: Manage LLM models (OpenAI/Ollama), MCP services, and job postings through a centralized management dashboard.
- **🔒 Privacy-Focused**: Supports local-first deployment (Ollama) to ensure data security.

### 🛠️ Tech Stack
- **Backend**: Spring Boot 3.3, LangChain4j, OpenAI/Ollama API, WebSocket (STOMP & Binary), OkHttp.
- **Frontend**: Vue 3 (Vite), Element Plus, Web Audio API, SockJS.
- **DevOps**: Maven, Node.js.

### 📦 Quick Start
1. **Configure Models**: Open the management dashboard, set up your LLM (Chat, Embedding, TTS, STT).
2. **Setup Data**: Upload Job Descriptions in "Job Management".
3. **Run**:
    - Backend: `mvn spring-boot:run` (Port: 8086)
    - Frontend: `npm run dev` (Port: 3000)

---

<a name="中文"></a>
## 中文

### 🌟 项目简介
AI 自动面试间是一款基于 **Spring Boot 3 + LangChain4j + Vue 3 + Element Plus** 构建的高性能智能化面试系统。它集成了拟真数字人交互、自动化简历分析以及基于 RAG（检索增强生成）的专业面试流程。

系统全面支持 **流式 AI 语音（TTS）** 与 **实时语音输入（STT）**，打造极低延迟的沉浸式对话体验。

### 🚀 核心特性
- **📄 智能简历匹配**: 自动解析应聘者简历，并利用向量数据库进行深度岗位匹配。支持单岗位自动跳转及多岗位手动选择。
- **🎙️ 流式语音交互**:
    - **流式播放 (TTS)**: AI 回复采用 PCM 流式合成，前端通过 Web Audio API 实时解析播放，实现秒级响应。
    - **实时转录 (STT)**: 求职者语音通过二进制 WebSocket 实时传输，实现“边说边出字”的极致交互。
- **👤 数字人视觉引导**: 内置具有动态语音感应动画的数字人形象，结合“毛玻璃”设计语言，营造高端面试氛围。
- **🧠 深度 RAG 引擎**: 结合本地岗位描述、企业文化及技术要求，AI 面试官能够提出极具专业性的针对性问题。
- **🛠️ 动态配置管理**: 支持在管理后台动态配置多个大模型（OpenAI/Ollama）、MCP 远程服务及岗位信息。
- **🔒 隐私与安全**: 支持全本地化部署（配合 Ollama），确保简历及业务数据不出私有网络。

### 🛠️ 技术栈
- **后端**: Spring Boot 3.3, LangChain4j, OpenAI/Ollama API, WebSocket (STOMP & Binary), OkHttp.
- **前端**: Vue 3 (Vite), Element Plus, Web Audio API, SockJS.
- **基础**: Maven, Node.js.

### 📦 快速启动
1. **模型配置**: 进入管理后台，配置所需的聊天、向量、语音流、转录模型路径。
2. **职位准备**: 在“职位管理”中上传岗位描述文档（JD）。
3. **运行**:
    - 后端: `mvn spring-boot:run` (端口: 8086)
    - 前端: `npm run dev` (端口: 3000)

---

## 📸 预览与文档
- **流程**: 简历上传 -> 自动匹配 -> 情感化对话 -> 评估反馈。
- **更多细节**: 请参考项目内的 `walkthrough.md`。

## 📝 许可证
[MIT License](LICENSE)