package cn.iamwsll.aicode.core.parser;

import cn.iamwsll.aicode.exception.BusinessException;
import cn.iamwsll.aicode.exception.ErrorCode;
import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 * @author iamwsll
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();
    /**
     * 执行代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 解析结果(HtmlCodeResult 或 MultiFileCodeResult)
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的CodeGenTypeEnum类型");
        };
    }
}
