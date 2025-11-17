package cn.iamwsll.aicode.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class WebScreenshotUtilsTest {

    @Test
    @Disabled("依赖外部截图服务，避免自动化测试时调用第三方接口")
    void saveWebPageScreenshot() {
        String testUrl = "https://iamwsll.cn";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }
}
