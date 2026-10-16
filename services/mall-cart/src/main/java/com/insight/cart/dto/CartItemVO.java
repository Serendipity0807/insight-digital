package com.insight.cart.dto;

import lombok.Data;

/** 购物车条目响应 */
@Data
public class CartItemVO {
    private Long skuId;
    private Integer quantity;
    private Boolean checked;
}
