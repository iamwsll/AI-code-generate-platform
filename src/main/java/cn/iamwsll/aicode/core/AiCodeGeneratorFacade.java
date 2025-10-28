package cn.iamwsll.aicode.core;

import cn.iamwsll.aicode.ai.AiCodeGeneratorService;
import cn.iamwsll.aicode.ai.model.HtmlCodeResult;
import cn.iamwsll.aicode.ai.model.MultiFileCodeResult;
import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Ai 代码生成门面类,组合代码生成和保存功能
 * 门面设计模式
 */
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一根据用户输入和代码生成类型生成并保存代码
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型:" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 生成并保存 HTML 代码
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }


}
