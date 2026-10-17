package com.insight.payment.controller;

import com.insight.common.result.Result;
import com.insight.payment.dto.CloseRequest;
import com.insight.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 服务间内部接口:不经网关,Feign 直连 */
@RestController
@RequestMapping("/internal/payment")
public class InternalPaymentController {

    @Autowired
    private PaymentService paymentService;

    /** 关单:关闭支付单(由订单服务 Feign 调用) */
    @PostMapping("/close")
    public Result<Void> close(@RequestBody @Valid CloseRequest req) {
        paymentService.closeByOrderId(req.getOrderId());
        return Result.success();
    }
}
