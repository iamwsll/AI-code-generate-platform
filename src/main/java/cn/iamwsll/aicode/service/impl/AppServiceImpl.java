package cn.iamwsll.aicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.iamwsll.aicode.model.entity.App;
import cn.iamwsll.aicode.mapper.AppMapper;
import cn.iamwsll.aicode.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/iamwsll">iamwsll</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
