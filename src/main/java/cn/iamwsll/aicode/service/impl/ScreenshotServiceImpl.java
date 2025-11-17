package cn.iamwsll.aicode.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.exception.ThrowUtils;
import cn.iamwsll.aicode.manager.OSSManager;
import cn.iamwsll.aicode.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Value("${url2pic.api-url:https://url2pic.php127.com/api/url2pic}")
    private String url2PicApiUrl;

    @Value("${url2pic.api-key:}")
    private String url2PicApiKey;

    @Resource
    private OSSManager ossManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        // 1. 生成本地截图
        String localScreenshotPath = downloadScreenshotViaApi(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        try {
            // 2. 上传到对象存储
            String ossUrl = uploadScreenshotToOSS(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(ossUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, ossUrl);
            return ossUrl;
        } finally {
            // 3. 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToOSS(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成 OOS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String oosKey = generateScreenshotKey(fileName);
        return ossManager.uploadFile(oosKey, screenshotFile);
    }

    /**
     * 调用 url2pic API 生成截图并下载到本地
     *
     * @param webUrl 目标网页
     * @return 本地压缩后 jpg 文件路径
     */
    private String downloadScreenshotViaApi(String webUrl) {
        // 构建临时目录
        String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                + File.separator + UUID.randomUUID().toString().substring(0, 8);
        FileUtil.mkdir(rootPath);

        // 调用第三方接口
        String response = HttpUtil.post(url2PicApiUrl, cn.hutool.core.lang.Dict.create()
                .set("key", url2PicApiKey)
                .set("url", webUrl)
                .set("width", 1440)
                .set("type", "png"));
        cn.hutool.json.JSONObject jsonObj = cn.hutool.json.JSONUtil.parseObj(response);
        ThrowUtils.throwIf(jsonObj.getInt("code", 0) != 1, ErrorCode.OPERATION_ERROR,
                "截图接口调用失败：" + jsonObj.getStr("msg"));
        String downloadLink = jsonObj.getJSONObject("data").getStr("download_link");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadLink), ErrorCode.OPERATION_ERROR, "截图接口未返回下载链接");

        // 下载 png
        String pngPath = rootPath + File.separator + RandomUtil.randomNumbers(5) + ".png";
        HttpUtil.downloadFile(downloadLink, FileUtil.file(pngPath));
        ThrowUtils.throwIf(!FileUtil.exist(pngPath), ErrorCode.OPERATION_ERROR, "下载截图失败");

        // 只保留顶部 810 像素区域
        BufferedImage image = ImgUtil.read(FileUtil.file(pngPath));
        int cropHeight = Math.min(810, image.getHeight());
        String croppedPngPath = rootPath + File.separator + RandomUtil.randomNumbers(5) + "_cropped.png";
        ImgUtil.cut(
                image,
                FileUtil.file(croppedPngPath),
                new Rectangle(0, 0, image.getWidth(), cropHeight)
        );

        // 转换为 jpg 以减少体积并与现有命名保持一致
        String compressedPath = rootPath + File.separator + RandomUtil.randomNumbers(5) + "_compressed.jpg";
        ImgUtil.convert(FileUtil.file(croppedPngPath), FileUtil.file(compressedPath));

        // 清理中间文件
        FileUtil.del(pngPath);
        FileUtil.del(croppedPngPath);
        return compressedPath;
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/11/2/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
