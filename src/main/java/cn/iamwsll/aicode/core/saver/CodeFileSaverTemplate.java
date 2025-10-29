package cn.iamwsll.aicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iamwsll.aicode.constant.AppConstant;
import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码保存器模板方法抽象类
 *
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存代码文件
     * @Param appId 应用ID
     * @param result 代码结果
     * @return 保存后的目录
     */
    public final File saveCode(T result, Long appId) {
        //1.验证输入
        validateInput(result);
        //2.build unique dir
        String baseDirPath = buildUniqueDir(appId);
        //3.保存文件
        saveFiles(result, baseDirPath);
        //4.返回保存目录
        return new File(baseDirPath);
    }

    /**
     * 验证输入(可以子类覆盖)
     *
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码结果不能为空");
        }
    }

    /**
     * 保存文件(子类实现)
     *
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);


    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID(bizType:业务类型
     * @Param appId 应用ID
     * @return 唯一目录路径
     */
    protected String buildUniqueDir(Long appId) {
        if(appId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用ID不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     * @param dirPath 目录路径
     * @param filename 文件名
     * @param content 文件内容
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 获取代码类型枚举(子类实现)
     *
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();
}
