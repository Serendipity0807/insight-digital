package com.insight.payment.dto;

import lombok.Data;

@Data
public class PayVO {
    /** 支付单号:前端拿它去收银台 */
    private String paymentNo;
    /** 应付金额(分) */
    private Integer amount;
    private String channel;
}