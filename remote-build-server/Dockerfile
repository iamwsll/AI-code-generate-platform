FROM node:18-alpine

# 设置工作目录
WORKDIR /app

# 安装Python和其他构建依赖（某些npm包需要）
RUN apk add --no-cache python3 make g++

# 复制package.json和package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install --production

# 复制服务器代码
COPY server.js ./

# 创建工作目录
RUN mkdir -p /app/workspace

# 暴露端口
EXPOSE 8124

# 设置环境变量
ENV NODE_ENV=production
ENV PORT=8124

# 启动服务器
CMD ["node", "server.js"]
