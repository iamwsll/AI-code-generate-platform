package cn.iamwsll.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
/**
 * HTML代码生成结果
 */
@Description("生成HTML代码文件的结果")
@Data
public class HtmlCodeResult {

    /**
     * 生成的HTML代码
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 代码描述
     */
    @Description("代码描述")
    private String description;
}