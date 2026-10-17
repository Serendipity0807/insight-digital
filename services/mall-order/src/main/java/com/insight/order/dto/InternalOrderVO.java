package com.insight.order.dto;

import lombok.Data;

@Data
public class InternalOrderVO {
    private Long orderId;
    private Long userId;
    private Integer status;
    private Integer payAmount;
}