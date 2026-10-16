package com.insight.order.controller;

import com.insight.common.result.Result;
import com.insight.order.dto.CreateOrderRequest;
import com.insight.order.dto.OrderVO;
import com.insight.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 创建订单(登录用户;userId 由网关从 JWT 解出放进请求头) */
    @PostMapping("/create")
    public Result<OrderVO> create(@RequestHeader("X-User-Id") Long userId,
                                  @RequestBody @Valid CreateOrderRequest req) {
        return Result.success(orderService.create(userId, req));
    }

    /** 健康探活 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
