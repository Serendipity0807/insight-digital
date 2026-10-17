package com.insight.payment.client;

import com.insight.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-order")
public interface OrderClient {

    /** 发起支付前查订单(内部接口,直连不走网关) */
    @GetMapping("/internal/order/{orderId}")
    Result<InternalOrderVO> getOrder(@PathVariable("orderId") Long orderId);

    /** 支付成功:改订单状态 + 扣库存(内部接口) */
    @PostMapping("/internal/order/markPaid")
    Result<Void> markPaid(@RequestBody MarkPaidRequest request);
}