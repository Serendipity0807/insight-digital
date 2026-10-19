package com.insight.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 秒杀商品实体:对应 seckill_item 表(场次里的商品,库存预热到 Redis) */
@Data
@TableName("seckill_item")
public class SeckillItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private Long skuId;

    /** 秒杀价(分) */
    private Integer seckillPrice;

    /** 活动库存(预热到 Redis db3,MySQL 是"底账") */
    private Integer stock;

    /** 乐观锁版本号(④-2 运营改库存用) */
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
