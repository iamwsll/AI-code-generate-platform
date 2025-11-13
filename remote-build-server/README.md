# 远程构建服务器

这是一个简单的Node.js服务器，用于接收Vue项目并执行npm install和构建。

## 快速开始

### 方式一：直接运行

#### 1. 安装依赖

```bash
npm install
```

#### 2. 启动服务器

```bash
# 不使用API Key（不推荐用于生产环境）
npm start

# 使用API Key（推荐）
API_KEY=your-secret-key npm start

# 自定义端口
PORT=8124 API_KEY=your-secret-key npm start
```

#### 3. 使用PM2管理（推荐用于生产环境）

```bash
# 安装PM2
npm install -g pm2

# 启动服务
pm2 start server.js --name remote-build-server -- --env API_KEY=your-secret-key

# 查看日志
pm2 logs remote-build-server

# 设置开机自启动
pm2 startup
pm2 save

# 停止服务
pm2 stop remote-build-server

# 重启服务
pm2 restart remote-build-server
```

### 方式二：使用Docker（推荐）

#### 1. 使用docker-compose启动

```bash
# 设置API Key（可选）
export API_KEY=your-secret-key

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

#### 2. 使用Docker直接运行

```bash
# 构建镜像
docker build -t remote-build-server .

# 运行容器
docker run -d \
  --name remote-build-server \
  -p 8124:8124 \
  -e API_KEY=your-secret-key \
  remote-build-server

# 查看日志
docker logs -f remote-build-server

# 停止容器
docker stop remote-build-server
```

## API接口

### 健康检查

```bash
GET /health
```

**响应示例：**
```json
{
  "status": "ok"
}
```

### 构建项目

```bash
POST /api/build
Content-Type: application/json
X-API-Key: your-secret-key (如果设置了API_KEY)
```

**请求体：**
```json
{
  "projectName": "vue-project-123456",
  "fileContent": "base64编码的项目ZIP文件内容"
}
```

**响应示例（成功）：**
```json
{
  "success": true,
  "message": "构建成功",
  "distContent": "base64编码的dist目录ZIP文件内容",
  "buildInfo": {
    "installDuration": "45.32秒",
    "buildDuration": "12.56秒",
    "totalDuration": "57.88秒"
  }
}
```

**响应示例（失败）：**
```json
{
  "success": false,
  "message": "构建失败: npm install 失败"
}
```

### 查看状态（可选）

```bash
GET /api/status
X-API-Key: your-secret-key (如果设置了API_KEY)
```

### 清理工作目录（可选）

```bash
POST /api/cleanup
X-API-Key: your-secret-key (如果设置了API_KEY)
```

## 环境变量

- `PORT`: 服务器端口，默认8124
- `API_KEY`: API认证密钥，建议在生产环境设置

## 目录结构

```
remote-build-server/
├── server.js           # 服务器主文件
├── package.json        # 依赖配置
├── workspace/          # 工作目录（自动创建）
└── README.md          # 说明文档
```

## 系统要求

- Node.js >= 18.0.0
- npm >= 9.0.0
- 建议内存: 4GB+
- 建议磁盘: 50GB+

## 安全建议

1. **设置API Key**: 生产环境务必设置API_KEY
2. **使用HTTPS**: 建议配置Nginx反向代理并启用HTTPS
3. **网络隔离**: 限制只允许主服务器访问
4. **定期清理**: 定期清理workspace目录
5. **监控**: 监控磁盘空间和内存使用情况

## 性能优化

1. **npm镜像**: 配置淘宝npm镜像加速下载
   ```bash
   npm config set registry https://registry.npmmirror.com
   ```

2. **npm缓存**: 保留npm缓存目录加速安装
   ```bash
   npm config set cache /path/to/npm-cache
   ```

3. **并发限制**: 如需支持并发构建，建议使用队列机制

## 故障排查

### 构建失败

1. 检查Node.js版本是否满足要求
2. 检查npm镜像源是否可用
3. 检查磁盘空间是否充足
4. 查看服务器日志获取详细错误信息

### 服务无响应

1. 检查服务器是否运行: `pm2 status`
2. 查看日志: `pm2 logs remote-build-server`
3. 检查防火墙设置
4. 检查端口是否被占用

## 许可证

MIT
