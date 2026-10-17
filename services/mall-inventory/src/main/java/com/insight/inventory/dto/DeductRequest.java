package com.insight.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/** 支付扣减请求:只扣 locked,saleable 在锁定时已扣过 */
@Data
public class DeductRequest {

    @NotNull(message = "订单号不能为空")
    @Positive(message = "订单号必须为正数")
    private Long orderId;

    @Valid
    @NotEmpty(message = "扣减明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "商品规格不能为空")
        @Positive(message = "商品规格必须为正数")
        private Long skuId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        private Integer quantity;
    }
}
