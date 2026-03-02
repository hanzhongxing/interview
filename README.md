# AI Interview Room (AI 自动面试间) 🤖

这是一个基于 **Spring Boot 3 + LangChain4j + Ollama + Vue 3 + Element Plus** 构建的智能化、全本地化 AI 面试系统。该系统集成了数字人视觉反馈、自动简历分析以及基于 RAG（检索增强生成）的专业面试流程。

---

## 🏗️ 核心特性

- **📄 智能简历匹配**: 自动解析应聘者上传的简历，并利用向量数据库与岗位需求进行深度匹配。
- **🧠 深度 RAG 面试**: 结合本地知识库（如职位描述、公司文化、技术要求），AI 面试官能够提出极具针对性的专业问题。
- **💬 实时流式对话**: 基于 WebSocket (STOMP) 实现低延迟的 AI 回复流式推送。
- **👤 数字人视觉引导**: 前端内置具有动态语音波纹反馈的“数字人”形象，模拟真实的面试交流环境。
- **🔒 全本地隐私保护**: 所有的简历文件、知识库文档、向量索引以及 LLM 推理均在本地运行，确保数据不外泄。

---

## 🛠️ 技术栈

### 后端 (Backend)
- **Spring Boot 3.3**: 基础应用框架。
- **LangChain4j**: LLM 编排，支持 RAG、内存管理和 AI 服务定义。
- **Ollama**: 本地通用大模型（Llama3）与向量模型（nomic-embed-text）。
- **WebSocket (STOMP)**: 实现全双工流式面试。
- **Apache Tika**: 多格式文档（PDF/DOCX/TXT）解析。

### 前端 (Frontend)
- **Vue 3 (Vite)**: 响应式 UI 框架。
- **Element Plus**: 现代化的组件库。
- **SockJS & Stomp.js**: 实时通信客户端。
- **Glassmorphism CSS**: 高级感透明毛玻璃设计语言。

---

## 🚀 快速启动

### 1. 准备本地模型 (Ollama)
请确保您的机器已安装 [Ollama](https://ollama.com/) 并运行以下命令下载所需模型：
```bash
# 下载对话模型
ollama pull llama3
# 下载向量模型
ollama pull nomic-embed-text
```

### 2. 后端部署
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
- 服务运行于: `http://localhost:8086`

### 3. 前端部署
```bash
cd frontend
npm install
npm run dev
```
- 访问地址: `http://localhost:3000`

---

## 📂 存储结构
项目会在根目录自动创建 `data` 文件夹：
- `/data/kb`: **岗位知识库**。请将职位描述 (JD) 或公司介绍放入此处，系统启动时会自动索引。
- `/data/resumes`: **简历仓库**。存储应聘者上传的原始简历文件。
- `/data/vectors`: **向量存储**。持久化存储文档的嵌入向量。

---

## 📸 界面预览
- **准备阶段**: 应聘者上传简历。
- **面试阶段**: 数字人面试官引导，左侧为动态交互形象，右侧为实时谈话窗口。

---

## 📝 许可证
[MIT License](LICENSE)