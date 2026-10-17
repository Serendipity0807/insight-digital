package com.insight.order.client;

import com.insight.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** mall-payment 的 Feign 客户端 */
@FeignClient(name = "mall-payment")
public interface PaymentClient {

    /** 关单时关闭支付单(已支付则不动) */
    @PostMapping("/internal/payment/close")
    Result<Void> close(@RequestBody ClosePaymentRequest request);
}
