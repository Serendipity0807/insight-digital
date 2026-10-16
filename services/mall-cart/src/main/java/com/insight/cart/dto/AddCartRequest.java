package com.insight.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddCartRequest {

    @NotNull(message = "商品规格不能为空")
    @Positive(message = "商品规格必须为正数")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 99, message = "单个商品最多 99 件")
    private Integer quantity;
}
