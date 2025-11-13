package cn.iamwsll.aicode.service.remote;

import java.io.File;

/**
 * 远程构建服务接口
 * 用于将Vue项目的构建过程（npm install + npm run build）转移到远程服务器执行
 * 
 * @author iamwsll
 */
public interface RemoteBuildService {

    /**
     * 在远程服务器上构建Vue项目
     * 
     * @param projectDir 本地项目目录
     * @return 是否构建成功
     */
    boolean buildProject(File projectDir);

    /**
     * 检查远程构建服务是否可用
     * 
     * @return 服务是否可用
     */
    boolean isAvailable();
}
