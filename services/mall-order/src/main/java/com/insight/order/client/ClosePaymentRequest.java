package com.insight.order.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 关闭支付单请求(契约镜像) */
@Data
public class ClosePaymentRequest {

    @NotNull
    @Positive
    private Long orderId;
}
