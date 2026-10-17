package com.insight.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MarkPaidRequest {

    @NotNull(message = "订单号不能为空")
    @Positive(message = "订单号必须为正数")
    private Long orderId;
}