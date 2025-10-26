package cn.iamwsll.aicode.constant;

/**
 * 用户常亮
 * 由于是interface,所以成员变量默认是public static final的
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    // endregion

}