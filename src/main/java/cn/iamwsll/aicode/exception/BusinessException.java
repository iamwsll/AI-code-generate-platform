package cn.iamwsll.aicode.exception;

import lombok.Getter;

/**
 * 自定义的业务异常.
 * 用于和框架自带的异常区分开来.
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 构造函数,使用自定义的错误码和信息来定义异常
     * @param code
     * @param message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数,直接使用错误码来定义异常
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 构造函数,方便覆盖掉错误码原本的信息
     * @param errorCode
     * @param message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
