package com.insight.order.client;

import lombok.Data;

import java.util.List;

@Data
public class DeductRequest {
    private Long orderId;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}