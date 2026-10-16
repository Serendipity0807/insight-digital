package com.insight.user.controller;

import com.insight.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    /** 健康探活:验证"网关 → Nacos → mall-user"链路是否通 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
