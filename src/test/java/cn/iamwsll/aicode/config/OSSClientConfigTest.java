package cn.iamwsll.aicode.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class OSSClientConfigTest {

    @Resource
    private OSS ossClient;

    @Test
    void ossClient() {
        List<Bucket> buckets = ossClient.listBuckets();
        System.out.println("成功连接到OSS服务，当前账号下的Bucket列表：");
        if (buckets.isEmpty()) {
            System.out.println("当前账号下暂无Bucket");
        } else {
            for (Bucket bucket : buckets) {
                System.out.println("- " + bucket.getName());
            }
        }

        // 释放资源
        ossClient.shutdown();
        System.out.println("OSS客户端已关闭");
    }
}