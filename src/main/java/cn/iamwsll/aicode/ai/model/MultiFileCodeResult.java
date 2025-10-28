package cn.iamwsll.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
/**
 * 多文件代码生成结果
 */
@Description("生成多文件代码的结果")
@Data
public class MultiFileCodeResult {

    /**
     * 生成的HTML代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 生成的CSS代码
     */
    @Description("CSS代码")
    private String cssCode;

    /**
     * 生成的JavaScript代码
     */
    @Description("JavaScript代码")
    private String jsCode;

    /**
     * 代码描述
     */
    @Description("代码描述")
    private String description;
}