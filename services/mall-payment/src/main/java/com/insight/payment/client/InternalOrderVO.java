package com.insight.payment.client;

import lombok.Data;

@Data
public class InternalOrderVO {
    private Long orderId;
    private Long userId;
    private Integer status;
    private Integer payAmount;
}