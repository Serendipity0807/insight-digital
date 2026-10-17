package com.insight.payment.controller;

import com.insight.common.result.Result;
import com.insight.payment.dto.CallbackRequest;
import com.insight.payment.dto.PayRequest;
import com.insight.payment.dto.PayVO;
import com.insight.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /** 发起支付(登录用户) */
    @PostMapping("/pay")
    public Result<PayVO> pay(@RequestHeader("X-User-Id") Long userId,
                             @RequestBody @Valid PayRequest req) {
        return Result.success(paymentService.createPayment(userId, req));
    }

    /** 渠道支付成功回调(mock):生产环境由支付宝/微信服务器直调,带签名验签,不经过网关 */
    @PostMapping("/callback")
    public Result<Void> callback(@RequestBody @Valid CallbackRequest req) {
        paymentService.handleCallback(req);
        return Result.success();
    }

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}

