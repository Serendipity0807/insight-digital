package com.insight.inventory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/** 释放库存请求(契约镜像:与 inventory 侧同构,不 import 对方模块) */
@Data
public class ReleaseRequest {

    @NotNull
    private Long orderId;

    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Long skuId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}
