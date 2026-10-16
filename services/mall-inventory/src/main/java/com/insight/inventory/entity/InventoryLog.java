package com.insight.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 库存流水:只追加的账本,谁也不能改 */
@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long skuId;
    /** 1下单锁定 2支付扣减 3取消释放 4退款回补 5秒杀预占 */
    private Integer changeType;
    /** 变动数量(正负:锁定记负,释放记正) */
    private Integer quantity;
    private Long orderId;
    private LocalDateTime createdAt;
}
