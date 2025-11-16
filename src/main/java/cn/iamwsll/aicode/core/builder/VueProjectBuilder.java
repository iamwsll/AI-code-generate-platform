package cn.iamwsll.aicode.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import cn.iamwsll.aicode.config.RemoteBuildProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class VueProjectBuilder {

    private final RemoteBuildProperties remoteProps;

    /**
     * 异步构建项目（不阻塞主流程）
     * java21 引入了虚拟线程，可以高效地处理大量并发任务
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath) {
        // 在单独的线程中执行构建，避免阻塞主流程
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
            }//尽量不要在异步线程抛出异常,把异常处理在线程内
            //不用配置线程池,java21虚拟线程已经很高效了
        });
    }
    /**
     * 构建 Vue 项目
     * 耗时时间很长...建议异步执行
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建 Vue 项目: {}，模式: {}", projectPath, remoteProps.isEnabled() ? "远程" : "本地");

        boolean buildSuccess = remoteProps.isEnabled()
                ? remoteBuild(projectDir)
                : localBuild(projectDir);

        if (!buildSuccess) {
            return false;
        }

        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * 本地构建流程
     */
    private boolean localBuild(File projectDir) {
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }
        return true;
    }

    /**
     * 远程构建流程：rsync 推送 → 远端 npm ci/build → rsync 拉回 dist → 清理
     */
    private boolean remoteBuild(File projectDir) {
        if (!remoteProps.isConfigReady()) {
            log.error("远程构建配置不完整，请检查 remote.build.* 配置");
            return false;
        }

        String projectName = projectDir.getName();
        String buildId = projectName + "_" + Instant.now().toEpochMilli();
        String remoteProjectPath = remoteProps.getWorkDir() + "/" + buildId;

        // 1) 推送源码
        if (!syncToRemote(projectDir, remoteProjectPath)) {
            log.error("同步到远端失败，终止构建");
            return false;
        }

        // 2) 远端构建
        if (!executeRemoteBuild(remoteProjectPath)) {
            log.error("远端 npm 构建失败");
            cleanupRemote(remoteProjectPath);
            return false;
        }

        // 3) 拉取 dist
        if (!fetchDist(projectDir, remoteProjectPath)) {
            log.error("拉取 dist 失败");
            cleanupRemote(remoteProjectPath);
            return false;
        }

        // 4) 清理远端（可配置保留）
        if (!remoteProps.isKeepRemoteWorkspace()) {
            cleanupRemote(remoteProjectPath);
        }
        return true;
    }


    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }


    /**
     * 判断当前操作系统是否为 Windows
     * @return 是否为 Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 构建适合当前操作系统的命令
     *
     * @param baseCommand 基础命令
     * @return 适合当前操作系统的命令
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }


    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            String dirLabel = (workingDir == null) ? "<no-dir>" : workingDir.getAbsolutePath();
            log.info("在目录 {} 中执行命令: {}", dirLabel, command);
            Process process = RuntimeUtil.exec(
                    null,//环境变量
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 打印标准输出与错误输出
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
                 var errReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("{}", line);
                }
                while ((line = errReader.readLine()) != null) {
                    log.error("{}", line);
                }
            }
            // 等待读取完成并设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

    private boolean executeCommand(File workingDir, List<String> command, int timeoutSeconds) {
        try {
            String dirLabel = (workingDir == null) ? "<no-dir>" : workingDir.getAbsolutePath();
            log.info("在目录 {} 中执行命令: {}", dirLabel, String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workingDir != null) {
                pb.directory(workingDir);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("{}", line);
                }
            }
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", String.join(" ", command));
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", String.join(" ", command), e.getMessage());
            return false;
        }
    }

    private boolean syncToRemote(File projectDir, String remoteProjectPath) {
        List<String> cmd = new ArrayList<>();
        cmd.add(remoteProps.getRsyncCommand());
        cmd.add("-az");
        cmd.add("--delete");
        cmd.add("--exclude");
        cmd.add("node_modules");
        cmd.add("--exclude");
        cmd.add("dist");
        cmd.add("--exclude");
        cmd.add(".git");
        if (remoteProps.getExtraRsyncFlags() != null) {
            cmd.add(remoteProps.getExtraRsyncFlags());
        }
        if (remoteProps.hasSshKey()) {
            cmd.add("-e");
            cmd.add(String.format("%s -i %s -o StrictHostKeyChecking=no", remoteProps.getSshCommand(), remoteProps.getSshKey()));
        }
        String localPath = projectDir.getAbsolutePath() + "/";
        String remote = String.format("%s@%s:%s/", remoteProps.getUser(), remoteProps.getHost(), remoteProjectPath);
        cmd.add(localPath);
        cmd.add(remote);
        return executeCommand(projectDir, cmd, remoteProps.getSyncTimeoutSeconds());
    }

    private boolean executeRemoteBuild(String remoteProjectPath) {
        List<String> cmd = new ArrayList<>();
        cmd.add(remoteProps.getSshCommand());
        if (remoteProps.hasSshKey()) {
            cmd.add("-i");
            cmd.add(remoteProps.getSshKey());
        }
        cmd.add(String.format("%s@%s", remoteProps.getUser(), remoteProps.getHost()));
        String nodeOptions = remoteProps.getNodeOptions();
        String buildCmd = String.format("cd %s && %s install && %s run build", remoteProjectPath, remoteProps.getNpmCommand(), remoteProps.getNpmCommand());
        if (nodeOptions != null && !nodeOptions.isBlank()) {
            buildCmd = String.format("cd %s && NODE_OPTIONS=\"%s\" %s install && NODE_OPTIONS=\"%s\" %s run build",
                    remoteProjectPath, nodeOptions, remoteProps.getNpmCommand(), nodeOptions, remoteProps.getNpmCommand());
        }
        cmd.add(buildCmd);
        return executeCommand(null, cmd, remoteProps.getBuildTimeoutSeconds());
    }

    private boolean fetchDist(File projectDir, String remoteProjectPath) {
        List<String> cmd = new ArrayList<>();
        cmd.add(remoteProps.getRsyncCommand());
        cmd.add("-az");
        cmd.add("--delete");
        if (remoteProps.hasSshKey()) {
            cmd.add("-e");
            cmd.add(String.format("%s -i %s -o StrictHostKeyChecking=no", remoteProps.getSshCommand(), remoteProps.getSshKey()));
        }
        String remote = String.format("%s@%s:%s/dist/", remoteProps.getUser(), remoteProps.getHost(), remoteProjectPath);
        cmd.add(remote);
        cmd.add(projectDir.getAbsolutePath() + "/dist/");
        return executeCommand(projectDir, cmd, remoteProps.getFetchTimeoutSeconds());
    }

    private void cleanupRemote(String remoteProjectPath) {
        List<String> cmd = new ArrayList<>();
        cmd.add(remoteProps.getSshCommand());
        if (remoteProps.hasSshKey()) {
            cmd.add("-i");
            cmd.add(remoteProps.getSshKey());
        }
        cmd.add(String.format("%s@%s", remoteProps.getUser(), remoteProps.getHost()));
        cmd.add(String.format("rm -rf %s", remoteProjectPath));
        boolean ok = executeCommand(null, cmd, remoteProps.getCleanupTimeoutSeconds());
        if (!ok) {
            log.warn("远端目录清理失败: {}", remoteProjectPath);
        }
    }

}
