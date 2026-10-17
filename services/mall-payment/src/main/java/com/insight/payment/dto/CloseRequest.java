package com.insight.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 关闭支付单请求(契约镜像:与 order 侧同构) */
@Data
public class CloseRequest {

    @NotNull
    @Positive
    private Long orderId;
}
