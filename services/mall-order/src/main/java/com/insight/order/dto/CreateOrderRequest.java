package com.insight.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/** 创建订单请求。注意:没有价格字段——金额必须服务端算,客户端传价格就是改价攻击 */
@Data
public class CreateOrderRequest {

    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<Item> items;

    /** 收货地址(下单时快照到订单) */
    @Valid
    private Address address;

    private String remark;

    @Data
    public static class Item {

        @NotNull(message = "商品规格不能为空")
        @Positive(message = "商品规格必须为正数")
        private Long skuId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        @Max(value = 99, message = "单个商品最多 99 件")
        private Integer quantity;
    }

    @Data
    public static class Address {

        @NotBlank(message = "收货人不能为空")
        private String name;

        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "收货地址不能为空")
        private String detail;
    }
}
