package cn.iamwsll.aicode.core;

import cn.iamwsll.aicode.ai.AiCodeGeneratorService;
import cn.iamwsll.aicode.ai.model.HtmlCodeResult;
import cn.iamwsll.aicode.ai.model.MultiFileCodeResult;
import cn.iamwsll.aicode.core.parser.CodeParserExecutor;
import cn.iamwsll.aicode.core.saver.CodeFileSaverExecutor;
import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Ai 代码生成门面类,组合代码生成和保存功能
 * 门面设计模式
 */
@Slf4j
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
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, codeGenTypeEnum);
            }
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型:" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 统一根据用户输入和代码生成类型生成并保存代码(流式)
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum);
            }
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型:" + codeGenTypeEnum.getValue());
        };
    }


    /**
     * 统一处理代码流(流式)
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 流式结果
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum) {
        //字符串拼接器
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式调用完成后，保存代码到文件
                String completeCode = codeBuilder.toString();
                //解析完成的总字符串
                Object parserResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                //把Result保存起来
                File saveDir = CodeFileSaverExecutor.executeSaver(parserResult, codeGenTypeEnum);
                log.info("保存成功,目录为{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败,错误信息: {}", e.getMessage());
            }
        });
    }
}
