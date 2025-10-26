package cn.iamwsll.aicode.service;

import com.mybatisflex.core.service.IService;
import cn.iamwsll.aicode.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/iamwsll">iamwsll</a>
 */
public interface UserService extends IService<User> {

    /**
     * - 用户注册
     * -
     * - @param userAccount 用户账户
     * - @param userPassword 用户密码
     * - @param checkPassword 校验密码
     * - @return 新用户 id
     * \
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * - 获取加密后的密码
     * -
     * - @param userPassword 用户密码
     * - @return 加密后的密码
     * \
     */
    String getEncryptPassword(String userPassword);
}
