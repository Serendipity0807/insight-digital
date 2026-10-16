package com.insight.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 订单明细:商品信息全是快照——下单那一刻的名字/规格/图片/价格,永不变 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    /** 回溯用:哪个商品(下架了也能点回去看) */
    private Long spuId;
    private Long skuId;
    /** 快照字段 */
    private String skuName;
    private String spec;
    private String image;
    /** 下单时单价快照(分) */
    private Integer price;
    private Integer quantity;
    /** 小计 = 单价 × 数量(分) */
    private Integer totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
