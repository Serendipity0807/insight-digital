package com.insight.order.dto;

import lombok.Data;

/** 订单明细项:商品快照(下单那一刻的名字/规格/价格) */
@Data
public class OrderItemVO {

    private Long skuId;
    private String skuName;
    private String spec;
    private String image;
    /** 下单时单价快照(分) */
    private Integer price;
    private Integer quantity;
    /** 小计 = 单价 × 数量(分) */
    private Integer totalAmount;
}
