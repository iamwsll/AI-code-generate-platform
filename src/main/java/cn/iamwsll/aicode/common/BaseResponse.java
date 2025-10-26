package cn.iamwsll.aicode.common;

import cn.iamwsll.aicode.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码在cn.iamwsll.aicode.exception.ErrorCode当中定义
     */
    private int code;

    private T data;

    /**
     * 额外返回给前端的信息
     * 这个信息可以是cn.iamwsll.aicode.exception.ErrorCode当中状态码对应的信息
     */
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
