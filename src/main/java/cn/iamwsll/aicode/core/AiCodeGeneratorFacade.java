package cn.iamwsll.aicode.core;

import cn.hutool.json.JSONUtil;
import cn.iamwsll.aicode.ai.AiCodeGeneratorService;
import cn.iamwsll.aicode.ai.AiCodeGeneratorServiceFactory;
import cn.iamwsll.aicode.ai.model.HtmlCodeResult;
import cn.iamwsll.aicode.ai.model.MultiFileCodeResult;
import cn.iamwsll.aicode.ai.model.message.AiResponseMessage;
import cn.iamwsll.aicode.ai.model.message.ToolExecutedMessage;
import cn.iamwsll.aicode.ai.model.message.ToolRequestMessage;
import cn.iamwsll.aicode.core.parser.CodeParserExecutor;
import cn.iamwsll.aicode.core.saver.CodeFileSaverExecutor;
import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一根据用户输入和代码生成类型生成并保存代码
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId       应用ID
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        //获取对应的AI代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, codeGenTypeEnum,appId);
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
     * @Param appId 应用ID
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        //获取对应的AI代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum,appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);//把tokenStream转换为Flux<String>
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
     * @Param appId 应用ID
     * @return 流式结果.这个结果实际上还是原始的codestream,只是增加了一些额外的操作
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
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
                File saveDir = CodeFileSaverExecutor.executeSaver(parserResult, codeGenTypeEnum,appId);
                log.info("保存成功,目录为{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败,错误信息: {}", e.getMessage());
            }
        });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     * 适配器模式:使用适配器将 TokenStream 转换为 Flux<String>,就像插座转换一样.
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

}
