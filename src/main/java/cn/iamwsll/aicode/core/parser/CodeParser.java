package cn.iamwsll.aicode.core.parser;

/**
 * 代码解析器策略接口
 * 使用策略模式，对外提供一个策略接口来调用，实际上调用的是实例化类对象
 * @author iamwsll
 */
public interface CodeParser <T>{
    /**
     * 解析代码内容
     * @param codeContent 代码内容
     * @return 解析结果
     */
    T parseCode(String codeContent);
}
