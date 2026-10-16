package com.insight.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuantityRequest {

    // 假设 skuId 通过 @PathVariable 在 URL 中传递，所以这里只校验数量
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 99, message = "单个商品最多 99 件")
    private Integer quantity;
}