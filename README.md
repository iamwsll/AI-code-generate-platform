# AI Code Generate Platform

项目效果体验:[code.iamwsll.cn](https://code.iamwsll.cn) 


## 项目简介
AI Code Generate Platform 是一个面向业务开发者的低门槛 AI 代码生成平台。用户只需输入自然语言描述即可生成单页 HTML、成套多文件代码或可直接运行的 Vue 项目，平台会自动完成代码解析、文件落地、在线预览和直接部署。

## 核心能力
- 自然语言生成多种形态的代码产物（HTML、HTML-CSS-JS混合项目、Vue 项目），支持流式响应与实时预览。
- LangChain4j + DeepSeek/Qwen 等大模型能力编排，结合工具调用完成静态资源抓取、截图及 OSS 部署。
- 后台统一管理应用、对话与生成历史，提供代码回溯、二次编辑和部署地址获取。
- Redis + Caffeine 双层缓存、Redisson 分布式锁与限流组件保障并发安全。
- Knife4j 提供开箱即用的在线 API 文档，便于联调与二次开发。
- 前端基于 Vue3 + Vite + Ant Design Vue，内置登录、作品管理、应用对话等业务场景。
- **远程构建支持**：支持将Vue项目的 npm install 和构建过程迁移到远程服务器，降低主服务器内存占用。


## 技术栈
- Backend: Spring Boot 3.5, LangChain4j, LangGraph4j, MyBatis-Flex, Redis, MySQL, Redisson, Selenium, Aliyun OSS SDK
- Frontend: Vue 3, Vite, TypeScript, Pinia, Vue Router, Ant Design Vue
- DevOps & Tooling: Maven, npm, eslint, prettier, vue-tsc, WebDriverManager

## 目录概览
```
ai-code/
|-- pom.xml                  # 后端 Maven 配置
|-- src/main/java            # Spring Boot 主代码
|-- src/main/resources       # 配置文件 & 模板
|-- ai-code-frontend/        # Vue 前端工程
|-- sql/create_table.sql     # 初始化数据库脚本
|-- docs/                    # 业务与接口文档
|-- tmp/code_output/         # AI 生成代码落地目录
|-- tmp/code_deploy/         # 自动部署中间产物
|-- README.md                # 当前说明文档
```

## 环境要求 及 部署过程

待填坑.

## 业务流程概览
1. 用户在首页输入应用描述，触发后端 AI 编排流程。
2. 服务端根据生成类型（HTML/Multi-file/Vue Project）选择模型与工具链，并实时推送流式响应。
3. 完整代码产物解析后持久化到 `tmp/code_output`，同时生成部署地址及预览资源。
4. 前端提供对话历史、应用管理、管理员后台等视图，支持二次发布与 OSS 静态托管。

## 扩展指引
- **模型切换**：调整 `application-local.yml` 中的 `langchain4j` 配置即可更换模型或供应商。
- **对象存储**：若无需 OSS，可实现自定义的 `CodeFileSaver` 并在 `CodeFileSaverExecutor` 中替换策略。
- **新增生成类型**：实现对应的 `AiCodeGeneratorService`、解析器与保存器，注册到工厂及枚举中。
- **前端主题**：统一样式入口位于 `src/layouts` 与 `src/assets/styles`，可按需自定义。
- **远程构建**：如需降低主服务器内存占用，可启用远程构建功能，详见 `docs/远程构建服务器部署指南.md`。

## 常见问题
- **模型调用超时报错**：经常出现在前端对话里显示read time out.这是因为一次调用大模型的耗时过长,导致langchain4j框架超时.可以修改src/main/java/dev/langchain4j/model/openai/OpenAiStreamingChatModel.java下的.connectTimeout(getOrDefault(builder.timeout, ofSeconds(300)))这一行自定义超时时间.不过目前应该这个值应该是够了.
- **出现json解析失败提示**: 这种一般是没有在配置文件里给模型设置maxtoken,从而导致模型提供的默认mac token过短,进而导致输出被截断
- **Redis 连接失败**：填坑.
- **Selenium 截图失败**：确保你在服务器安装了chrome,并且给机器预留了足够的内存.
- **日志提示权限错误**：确认服务拥有写入权限。
- **npm install 内存占用高**：可以启用远程构建功能，将构建过程迁移到专门的构建服务器上。详见 `docs/远程构建服务器部署指南.md`。

## 相关文档
- `docs/应用接口文档.md`：核心业务接口说明
- `docs/用户接口文档.md`：用户体系相关 API
- `docs/对话历史接口文档.md`、`docs/静态资源接口文档.md`：生成历史与资源托管说明

## 协作规范
---
如需更多帮助，可在 Issues 中提出需求.