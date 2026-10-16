package com.insight.user.controller;

import com.insight.common.result.Result;
import com.insight.user.dto.RegisterRequest;
import com.insight.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 注册。POST /api/v1/user/register,body = {phone, password, nickname?} */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterRequest req) {
        return Result.success(userService.register(req));
    }

    /** 健康探活:验证"网关 → Nacos → mall-user"链路是否通 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
