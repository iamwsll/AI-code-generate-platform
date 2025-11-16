package cn.iamwsll.aicode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "remote.build")
public class RemoteBuildProperties {

    /** 是否启用远程构建 */
    private boolean enabled = false;

    /** 远端主机地址 */
    private String host;

    /** 远端执行用户 */
    private String user;

    /** SSH 私钥路径，可选 */
    private String sshKey;

    /** ssh 命令，默认 ssh */
    private String sshCommand = "ssh";

    /** rsync 命令，默认 rsync */
    private String rsyncCommand = "rsync";

    /** 远端工作根目录 */
    private String workDir = "/tmp/remote-code-build";

    /** npm 命令，默认 npm，可替换为 pnpm/yarn */
    private String npmCommand = "npm";

    /** Node 运行参数，控制内存等，例如 --max_old_space_size=512 */

    private String nodeOptions;

    /** 同步超时（秒） */
    private int syncTimeoutSeconds = 180;

    /** 远端构建超时（秒） */
    private int buildTimeoutSeconds = 900;

    /** 回传 dist 超时（秒） */
    private int fetchTimeoutSeconds = 180;

    /** 远端清理超时（秒） */
    private int cleanupTimeoutSeconds = 60;

    /** 额外 rsync 参数，可为空 */
    private String extraRsyncFlags;

    /** 是否保留远端工作目录（调试用） */
    private boolean keepRemoteWorkspace = false;

    public boolean hasSshKey() {
        return sshKey != null && !sshKey.isBlank();
    }

    public boolean isConfigReady() {
        return enabled && host != null && !host.isBlank() && user != null && !user.isBlank();
    }
}
