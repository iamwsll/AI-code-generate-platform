package cn.iamwsll.aicode.manager;

import cn.iamwsll.aicode.config.OSSClientConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OSS对象存储管理器
 *
 * @author iamwsll
 */
@Component
@Slf4j
public class OSSManager {

    @Resource
    private OSSClientConfig ossClientConfig;

    @Resource
    private OSS ossClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucketName(),key, file);
        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 OSS 并返回访问 URL
     *
     * @param key  OSS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url ="https://"+ossClientConfig.getBucketName()+"." + ossClientConfig.getEndpoint()+"/"+ key;
            log.info("文件上传OSS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传OSS失败，返回结果为空");
            return null;
        }
    }
}
