package cn.iamwsll.aicode.service;

import cn.iamwsll.aicode.model.dto.UserQueryRequest;
import cn.iamwsll.aicode.model.vo.LoginUserVO;
import cn.iamwsll.aicode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import cn.iamwsll.aicode.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

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
     * - 获取登录用户视图对象
     * -
     * - @param user 用户实体对象
     * - @return 登录用户视图对象
     *
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * - 用户登录
     * -
     * - @param userAccount 用户账户
     * - @param userPassword 用户密码
     * - @param request 请求对象
     * - @return 登录用户视图对象
     *
     */

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * - 获取当前登录用户
     * -
     * - @param request
     * - @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * - @param request
     * - @return UserVO
     */
    UserVO getUserVO(User user);

    /**
     * - 获取脱敏后的用户信息列表
     *
     * @param userList
     * @return List<UserVO>
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * - 用户注销
     * - @param request
     * - @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * - 根据用户信息构建查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * - 获取加密后的密码
     * -
     * - @param userPassword 用户密码
     * - @return 加密后的密码
     * \
     */
    String getEncryptPassword(String userPassword);
}
