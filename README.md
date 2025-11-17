# AI Code Generate Platform

项目效果体验:[code.iamwsll.cn](https://code.iamwsll.cn) 


## 项目简介
AI Code Generate Platform 是一个面向业务开发者的低门槛 AI 代码生成平台。用户只需输入自然语言描述即可生成单页 HTML、成套多文件代码或可直接运行的 Vue 项目，平台会自动完成代码解析、文件落地、在线预览和直接部署。

## 效果演示

![alt text](docs/image/1763366060322)
登录后,即可进行使用.输入一句话:
![alt text](docs/image/1763366137103)
点击发送后,立刻进入生成页面.后台会使用一个小模型,自动根据你的prompt,选择使用以下三种生成方式:HTML、HTML-CSS-JS混合项目、Vue 项目.并且开始生成.考虑到速度和陈本.目前在上线的网站里,使用的是Qwen/Qwen3-Coder-30B-A3B-Instruct.
![alt text](docs/image/1763366238753)
生成结果会在右侧进行实时预览.
如果你有不满意的地方,点击右上角"编辑模式",选中你想要修改的元素,发送即可修改:
![alt text](docs/image/1763366419729)
得到你想要的内容,之后,可以在右上角点击部署:
![alt text](docs/image/1763366486196)
你可以直接点击"访问网站",得到部署后的网站.同时,你也可以点击右上角"下载代码",进行代码的下载.
在vue模式下,AI会有调用工具的能力:
![alt text](docs/image/1763366628551)
在网站首页的下方,管理员可以展示一些精选的案例:
![alt text](docs/image/1763366671259)
你也可以点进去查看历史对话等.
同时有基础的用户和应用管理:
![alt text](docs/image/1763366817759)



## 核心能力
- 自然语言生成多种形态的代码产物（HTML、HTML-CSS-JS混合项目、Vue 项目），支持流式响应与实时预览。
- LangChain4j + DeepSeek/Qwen 等大模型能力编排，结合工具调用完成静态资源抓取、截图及 OSS 部署。
- 后台统一管理应用、对话与生成历史，提供代码回溯、二次编辑和部署地址获取。
- Redis + Caffeine 双层缓存、Redisson 分布式锁与限流组件保障并发安全。
- Knife4j 提供开箱即用的在线 API 文档，便于联调与二次开发。
- 前端基于 Vue3 + Vite + Ant Design Vue，内置登录、作品管理、应用对话等业务场景。


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
|-- sql/                     # 数据库脚本
|-- docs/                    # 业务与接口文档
|-- tmp/code_output/         # AI 生成代码落地目录
|-- tmp/code_deploy/         # 自动部署中间产物
|-- README.md                # 当前说明文档
```

## 环境要求 及 部署过程
先大致写一写
### 本地测试
1. 将项目clone到本地.
2. 填写src/main/resources/application.yml当中的配置信息.本地的mysql redis先启动.
3. 按照src/main/resources/application.yml当中的注释里的提示,创建并且完成文件src/main/resources/application-local.yml.
4. 直接跑起来前后端.
- dashscope,pexel可以不配置.这部分只是在工作流里使用.
- 如果npm 打包(见下)只需要在本机执行,那么也remote.build.enable只需要改为false,后面的不用填.
- 如果浏览器截图服务使用url2pic服务,才需要填写api.
### 部署上线
较为复杂.目前考虑到服务器资源的压力很大,这里我采取了分布式架构进行部署:一台机器用于提供前后端服务,另外一台专门用于AI生成的vue项目 npm install/build的打包.两台机器均为2c2g.
机器的要求:
- 安装了nginx,mysql,redis.保证80端口,3306端口,6379端口的开放.jdk版本至少为21(使用了虚拟线程)
- 使用sql脚本初始化服务器.
- 如果你想在本地使用浏览器截图服务,那么你需要安装一个chrome,并且安装中文字体.
考虑到2c2g实在是太小了,我目前使用的jvm调参的参数:
```bash
    nohup java \
 -Xms256m -Xmx256m \
 -XX:MaxMetaspaceSize=192m \
 -XX:CompressedClassSpaceSize=96m \
 -XX:ReservedCodeCacheSize=64m \
 -XX:MaxDirectMemorySize=64m \
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=200 \
 -XX:+UseStringDeduplication \
 -jar ./ai-code-0.2.1-SNAPSHOT.jar \
 --spring.profiles.active=prod > app.log 2>&1 &
```



## 常见问题
- **模型调用超时报错**：经常出现在前端对话里显示read time out.这是因为一次调用大模型的耗时过长,导致langchain4j框架超时.可以修改src/main/java/dev/langchain4j/model/openai/OpenAiStreamingChatModel.java下的.connectTimeout(getOrDefault(builder.timeout, ofSeconds(300)))这一行自定义超时时间.不过目前应该这个值应该是够了.
- **出现json解析失败提示**: 这种一般是没有在配置文件里给模型设置maxtoken,从而导致模型提供的默认mac token过短,进而导致输出被截断
- **Redis 连接失败**：填坑.
- **Selenium 截图失败**：确保你在服务器安装了chrome,并且给机器预留了足够的内存.这很重要.

## 相关文档
请在docs里查看.

## 协作规范
---
如需更多帮助，可在 Issues 中提出需求.