package cn.iamwsll.aicode.exception;

/**
 * 快速抛异常的工具类
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     * 抛出RuntimeException.
     * 直接使用传入的异常
     * ps:由于businessException是RuntimeException的子类,所以也可以用这个方法抛出BusinessException
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     * 抛出BusinessException
     * 使用错误码自带的信息
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     * 抛出BusinessException
     * 使用错误码,但自定义错误信息
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
