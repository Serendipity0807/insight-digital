package com.insight.cart.dto;

import lombok.Data;

/** Redis Hash 的 value 模型:一个购物车条目 */
@Data
public class CartItem {
    private Integer quantity;
    private Boolean checked;
}
