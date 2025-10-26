package cn.iamwsll.aicode.controller;

import cn.iamwsll.aicode.common.BaseResponse;
import cn.iamwsll.aicode.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthy")
public class HealthController {

    @GetMapping("")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("I am healthy");
    }
}
