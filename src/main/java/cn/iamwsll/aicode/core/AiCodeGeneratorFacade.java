package cn.iamwsll.aicode.core;

import cn.iamwsll.aicode.ai.AiCodeGeneratorService;
import cn.iamwsll.aicode.ai.model.HtmlCodeResult;
import cn.iamwsll.aicode.ai.model.MultiFileCodeResult;
import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
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
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型:" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 生成并保存 HTML 代码(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
        //字符串拼接器
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {

            try {
                //流式调用完成后，保存代码到文件
                String completeCode = codeBuilder.toString();
                //解析完成的总字符串
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeCode);
                //把Result保存起来
                File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("单html文件创建成功,保存到{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("单html文件保存失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 生成并保存多文件代码(流式)
     *
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //字符串拼接器
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式调用完成后，保存代码到文件
                String completeCode = codeBuilder.toString();
                //解析完成的总字符串
                MultiFileCodeResult MultiFileCodeResult = CodeParser.parseMultiFileCode(completeCode);
                //把Result保存起来
                File saveDir = CodeFileSaver.saveMultiFileCodeResult(MultiFileCodeResult);
                log.info("多文件创建成功,保存到{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("多文件保存失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 生成并保存 HTML 代码
     *
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     *
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }


}
