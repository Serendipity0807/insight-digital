package com.insight.order.controller;

import com.insight.common.result.Result;
import com.insight.order.dto.InternalOrderVO;
import com.insight.order.dto.MarkPaidRequest;
import com.insight.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 服务间内部接口:前缀 /internal/** 不被网关路由(网关只有 /api/v1/** 的 6 条规则),
 * 外部用户拿 token 也进不来;Feign 直连服务端口才能调。这就是接口分级:危险接口关在门里。
 */
@RestController
@RequestMapping("/internal/order")
public class InternalOrderController {

    @Autowired
    private OrderService orderService;

    /** 支付服务发起支付前查订单 */
    @GetMapping("/{orderId}")
    public Result<InternalOrderVO> getOrder(@PathVariable Long orderId) {
        return Result.success(orderService.getForPayment(orderId));
    }

    /** 支付成功:改状态 + 扣库存 */
    @PostMapping("/markPaid")
    public Result<Void> markPaid(@RequestBody MarkPaidRequest req) {
        orderService.markPaid(req.getOrderId());
        return Result.success();
    }
}
