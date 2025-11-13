package cn.iamwsll.aicode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 远程构建服务配置类
 * 
 * @author iamwsll
 */
@Configuration
@ConfigurationProperties(prefix = "remote.build")
@Data
public class RemoteBuildConfig {

    /**
     * 是否启用远程构建（默认false，使用本地构建）
     */
    private boolean enabled = false;

    /**
     * 远程构建服务器地址
     */
    private String serverUrl;

    /**
     * API认证密钥（可选）
     */
    private String apiKey;

    /**
     * 连接超时时间（秒）
     */
    private int connectTimeout = 30;

    /**
     * 读取超时时间（秒）- npm install可能需要较长时间
     */
    private int readTimeout = 600;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
}
