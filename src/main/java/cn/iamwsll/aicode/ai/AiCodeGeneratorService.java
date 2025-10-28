package cn.iamwsll.aicode.ai;

import cn.iamwsll.aicode.ai.model.HtmlCodeResult;
import cn.iamwsll.aicode.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码
     * @param userMessage 用户提示词
     * @return AI生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHTMLCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户提示词
     * @return AI生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
