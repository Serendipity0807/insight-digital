package com.insight.order.client;

import lombok.Data;
import java.util.List;

/** 库存锁定契约镜像:字段名与 mall-inventory 的 LockRequest 一致 */
@Data
public class LockRequest {

    private Long orderId;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}
