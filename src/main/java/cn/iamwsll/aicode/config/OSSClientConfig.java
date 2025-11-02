package cn.iamwsll.aicode.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS配置类
 * 
 * @author iamwsll
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class OSSClientConfig {

    /**
     * 域名
     */
    private String endpoint;

    /**
     * ID
     */
    private String accessKeyId;

    /**
     * 密钥（注意不要泄露）
     */
    private String accessKeySecret;

    /**
     * 桶名
     */
    private String bucketName;

    /**
     * 地域
     */
    private String region;

    @Bean
    public OSS ossClient() {
        // 初始化用户身份信息
        DefaultCredentialProvider provider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        // 配置客户端参数
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 显式声明使用V4签名算法
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        // 初始化OSS客户端
        return OSSClientBuilder.create()
                .credentialsProvider(provider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .endpoint(endpoint)
                .build();
    }
}
