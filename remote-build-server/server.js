const express = require('express');
const fs = require('fs-extra');
const path = require('path');
const { execSync } = require('child_process');
const AdmZip = require('adm-zip');

const app = express();
const PORT = process.env.PORT || 8124;
const WORK_DIR = path.join(__dirname, 'workspace');
const API_KEY = process.env.API_KEY || ''; // 从环境变量读取API Key

// 确保工作目录存在
fs.ensureDirSync(WORK_DIR);

// 解析JSON请求体（增加大小限制以支持大文件）
app.use(express.json({ limit: '100mb' }));

// API Key验证中间件（可选）
const validateApiKey = (req, res, next) => {
  if (API_KEY && req.headers['x-api-key'] !== API_KEY) {
    return res.status(401).json({ success: false, message: '未授权' });
  }
  next();
};

// 健康检查接口
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

// 构建接口
app.post('/api/build', validateApiKey, async (req, res) => {
  const { projectName, fileContent } = req.body;
  
  if (!projectName || !fileContent) {
    return res.status(400).json({ 
      success: false, 
      message: '缺少必要参数' 
    });
  }

  const projectDir = path.join(WORK_DIR, projectName);
  const zipPath = path.join(WORK_DIR, `${projectName}.zip`);
  
  try {
    console.log(`[${new Date().toISOString()}] 开始构建项目: ${projectName}`);
    
    // 1. 解码并保存ZIP文件
    const zipBuffer = Buffer.from(fileContent, 'base64');
    fs.writeFileSync(zipPath, zipBuffer);
    console.log(`[${new Date().toISOString()}] ZIP文件已保存，大小: ${(zipBuffer.length / 1024 / 1024).toFixed(2)} MB`);
    
    // 2. 解压项目
    console.log(`[${new Date().toISOString()}] 解压项目...`);
    const zip = new AdmZip(zipPath);
    zip.extractAllTo(projectDir, true);
    
    // 3. 执行 npm install
    console.log(`[${new Date().toISOString()}] 执行 npm install...`);
    const installStartTime = Date.now();
    execSync('npm install', { 
      cwd: projectDir,
      stdio: 'inherit',
      timeout: 600000 // 10分钟超时
    });
    const installDuration = ((Date.now() - installStartTime) / 1000).toFixed(2);
    console.log(`[${new Date().toISOString()}] npm install 完成，耗时: ${installDuration}秒`);
    
    // 4. 执行 npm run build
    console.log(`[${new Date().toISOString()}] 执行 npm run build...`);
    const buildStartTime = Date.now();
    execSync('npm run build', { 
      cwd: projectDir,
      stdio: 'inherit',
      timeout: 300000 // 5分钟超时
    });
    const buildDuration = ((Date.now() - buildStartTime) / 1000).toFixed(2);
    console.log(`[${new Date().toISOString()}] npm run build 完成，耗时: ${buildDuration}秒`);
    
    // 5. 压缩dist目录
    console.log(`[${new Date().toISOString()}] 压缩dist目录...`);
    const distDir = path.join(projectDir, 'dist');
    if (!fs.existsSync(distDir)) {
      throw new Error('dist目录不存在');
    }
    
    const distZip = new AdmZip();
    distZip.addLocalFolder(distDir);
    const distBuffer = distZip.toBuffer();
    const distBase64 = distBuffer.toString('base64');
    console.log(`[${new Date().toISOString()}] dist目录已压缩，大小: ${(distBuffer.length / 1024 / 1024).toFixed(2)} MB`);
    
    // 6. 清理临时文件
    console.log(`[${new Date().toISOString()}] 清理临时文件...`);
    fs.removeSync(projectDir);
    fs.removeSync(zipPath);
    
    // 7. 返回结果
    const totalDuration = ((Date.now() - installStartTime + buildStartTime) / 1000).toFixed(2);
    console.log(`[${new Date().toISOString()}] 项目构建成功: ${projectName}，总耗时: ${totalDuration}秒`);
    res.json({
      success: true,
      message: '构建成功',
      distContent: distBase64,
      buildInfo: {
        installDuration: `${installDuration}秒`,
        buildDuration: `${buildDuration}秒`,
        totalDuration: `${totalDuration}秒`
      }
    });
    
  } catch (error) {
    console.error(`[${new Date().toISOString()}] 构建失败: ${error.message}`);
    console.error(error.stack);
    
    // 清理临时文件
    try {
      fs.removeSync(projectDir);
      fs.removeSync(zipPath);
    } catch (cleanupError) {
      console.error(`[${new Date().toISOString()}] 清理临时文件失败: ${cleanupError.message}`);
    }
    
    res.status(500).json({
      success: false,
      message: `构建失败: ${error.message}`
    });
  }
});

// 获取工作目录状态（可选，用于监控）
app.get('/api/status', validateApiKey, (req, res) => {
  try {
    const files = fs.readdirSync(WORK_DIR);
    const stats = {
      workDir: WORK_DIR,
      fileCount: files.length,
      files: files.slice(0, 10) // 只显示前10个文件
    };
    res.json(stats);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// 清理工作目录（可选，用于手动清理）
app.post('/api/cleanup', validateApiKey, (req, res) => {
  try {
    fs.emptyDirSync(WORK_DIR);
    console.log(`[${new Date().toISOString()}] 工作目录已清理`);
    res.json({ success: true, message: '工作目录已清理' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`========================================`);
  console.log(`远程构建服务器已启动`);
  console.log(`端口: ${PORT}`);
  console.log(`工作目录: ${WORK_DIR}`);
  console.log(`API Key: ${API_KEY ? '已设置' : '未设置（不推荐）'}`);
  console.log(`启动时间: ${new Date().toISOString()}`);
  console.log(`========================================`);
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log(`[${new Date().toISOString()}] 收到SIGTERM信号，正在关闭服务器...`);
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log(`[${new Date().toISOString()}] 收到SIGINT信号，正在关闭服务器...`);
  process.exit(0);
});
