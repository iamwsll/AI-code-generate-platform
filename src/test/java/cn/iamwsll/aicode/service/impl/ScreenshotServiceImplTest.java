package cn.iamwsll.aicode.service.impl;

import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试：真实调用 url2pic，并将结果上传到真实 OSS。
 * 仅当 url2pic 和 OSS 配置齐全且环境可联网时才会执行。
 */
@SpringBootTest
class ScreenshotServiceImplTest {

    @Autowired
    private ScreenshotServiceImpl screenshotService;

    @Value("${url2pic.api-key:}")
    private String apiKey;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucket;

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Test
    void generateAndUploadScreenshot_success_realOss() {
        // 缺少外部依赖时跳过，避免在 CI / 离线环境失败
        Assumptions.assumeTrue(StrUtil.isNotBlank(apiKey), "需要 url2pic.api-key");
        Assumptions.assumeTrue(StrUtil.isNotBlank(bucket) && StrUtil.isNotBlank(endpoint), "需要完整 OSS 配置");

        String result = screenshotService.generateAndUploadScreenshot("https://code.iamwsll.cn/dist/Dtqq8s/");

        Assertions.assertTrue(StrUtil.isNotBlank(result), "返回的封面地址不能为空");
        String expectedPrefix = "https://" + bucket + "." + endpoint + "/screenshots/";
        Assertions.assertTrue(result.startsWith(expectedPrefix),
                () -> "返回地址前缀不符合预期: " + result);
    }
}
