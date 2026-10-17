package com.insight.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PayRequest {

    @NotNull(message = "订单号不能为空")
    @Positive(message = "订单号必须为正数")
    private Long orderId;

    @NotBlank(message = "支付渠道不能为空")
    @Pattern(regexp = "ALIPAY|WECHAT", message = "支付渠道仅支持 ALIPAY/WECHAT")
    private String channel;
}