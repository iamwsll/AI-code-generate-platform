package cn.iamwsll.aicode.service;

/**
 * 截图服务(便于拆分微服务)
 */
public interface ScreenshotService {

    /**
     * 通用的截图服务
     * @param WebUrl
     * @return
     */
    String generateAndUploadScreenshot(String WebUrl);
}
