package cn.iamwsll.aicode.service.remote.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iamwsll.aicode.config.RemoteBuildConfig;
import cn.iamwsll.aicode.service.remote.RemoteBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 远程构建服务实现类
 * 通过HTTP API与远程构建服务器通信，将项目文件发送到远程服务器进行构建
 * 
 * @author iamwsll
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteBuildServiceImpl implements RemoteBuildService {

    private final RemoteBuildConfig remoteBuildConfig;

    @Override
    public boolean buildProject(File projectDir) {
        if (!remoteBuildConfig.isEnabled()) {
            log.warn("远程构建服务未启用，无法执行远程构建");
            return false;
        }

        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在或不是有效目录: {}", projectDir.getAbsolutePath());
            return false;
        }

        try {
            log.info("开始远程构建项目: {}", projectDir.getAbsolutePath());
            
            // 1. 压缩项目目录
            File zipFile = compressProject(projectDir);
            if (zipFile == null) {
                log.error("压缩项目失败");
                return false;
            }

            // 2. 发送到远程服务器
            boolean buildSuccess = sendBuildRequest(zipFile, projectDir);
            
            // 3. 清理临时文件
            FileUtil.del(zipFile);

            return buildSuccess;

        } catch (Exception e) {
            log.error("远程构建失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        if (!remoteBuildConfig.isEnabled()) {
            return false;
        }

        try {
            String healthUrl = remoteBuildConfig.getServerUrl() + "/health";
            HttpResponse response = HttpRequest.get(healthUrl)
                    .timeout(remoteBuildConfig.getConnectTimeout() * 1000)
                    .execute();
            return response.isOk();
        } catch (Exception e) {
            log.error("检查远程构建服务可用性失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 压缩项目目录为ZIP文件
     */
    private File compressProject(File projectDir) {
        try {
            String zipFileName = projectDir.getName() + "_" + System.currentTimeMillis() + ".zip";
            File zipFile = new File(System.getProperty("java.io.tmpdir"), zipFileName);
            
            log.info("压缩项目到: {}", zipFile.getAbsolutePath());
            cn.hutool.core.util.ZipUtil.zip(projectDir.getAbsolutePath(), zipFile.getAbsolutePath());
            
            return zipFile;
        } catch (Exception e) {
            log.error("压缩项目失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送构建请求到远程服务器
     */
    private boolean sendBuildRequest(File zipFile, File projectDir) {
        int retries = 0;
        while (retries < remoteBuildConfig.getMaxRetries()) {
            try {
                log.info("第 {} 次尝试发送构建请求到远程服务器", retries + 1);

                // 读取ZIP文件内容
                byte[] fileBytes = Files.readAllBytes(zipFile.toPath());
                String base64Content = Base64.getEncoder().encodeToString(fileBytes);

                // 构建请求体
                JSONObject requestBody = new JSONObject();
                requestBody.set("projectName", projectDir.getName());
                requestBody.set("fileContent", base64Content);

                // 发送HTTP请求
                String buildUrl = remoteBuildConfig.getServerUrl() + "/api/build";
                HttpRequest request = HttpRequest.post(buildUrl)
                        .timeout(remoteBuildConfig.getReadTimeout() * 1000)
                        .body(requestBody.toString());

                // 添加认证头（如果配置了）
                if (remoteBuildConfig.getApiKey() != null && !remoteBuildConfig.getApiKey().isEmpty()) {
                    request.header("X-API-Key", remoteBuildConfig.getApiKey());
                }

                HttpResponse response = request.execute();

                if (response.isOk()) {
                    // 解析响应
                    JSONObject result = JSONUtil.parseObj(response.body());
                    boolean success = result.getBool("success", false);

                    if (success) {
                        // 获取构建产物
                        String distContent = result.getStr("distContent");
                        if (distContent != null && !distContent.isEmpty()) {
                            // 解码并保存dist目录
                            return saveDistContent(distContent, projectDir);
                        }
                        return true;
                    } else {
                        String errorMsg = result.getStr("message", "未知错误");
                        log.error("远程构建失败: {}", errorMsg);
                    }
                } else {
                    log.error("远程构建请求失败，HTTP状态码: {}", response.getStatus());
                }

            } catch (Exception e) {
                log.error("发送构建请求失败: {}", e.getMessage(), e);
            }

            retries++;
            if (retries < remoteBuildConfig.getMaxRetries()) {
                try {
                    Thread.sleep(2000); // 重试前等待2秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return false;
    }

    /**
     * 保存远程构建的dist目录内容
     */
    private boolean saveDistContent(String base64Content, File projectDir) {
        try {
            byte[] zipBytes = Base64.getDecoder().decode(base64Content);
            
            // 创建临时文件
            File tempZip = File.createTempFile("dist_", ".zip");
            Files.write(tempZip.toPath(), zipBytes);

            // 解压到项目的dist目录
            File distDir = new File(projectDir, "dist");
            if (distDir.exists()) {
                FileUtil.del(distDir);
            }
            distDir.mkdirs();

            cn.hutool.core.util.ZipUtil.unzip(tempZip, distDir);
            
            // 清理临时文件
            FileUtil.del(tempZip);

            log.info("成功保存构建产物到: {}", distDir.getAbsolutePath());
            return true;

        } catch (Exception e) {
            log.error("保存构建产物失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
