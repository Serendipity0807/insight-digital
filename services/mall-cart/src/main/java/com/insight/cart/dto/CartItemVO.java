package com.insight.cart.dto;

import lombok.Data;

/** 购物车条目响应 */
@Data
public class CartItemVO {
    private Long skuId;
    private Long spuId;      // 商品id(点击跳详情用)
    private String skuName;  // 商品名
    private String spec;     // 规格
    private String image;    // 图片
    private Integer price;   // 单价(分)
    private Integer quantity;
    private Boolean checked;
}

