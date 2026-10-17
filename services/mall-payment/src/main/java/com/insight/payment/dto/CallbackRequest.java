package com.insight.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CallbackRequest {

    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    @NotBlank(message = "渠道流水号不能为空")
    private String tradeNo;
}