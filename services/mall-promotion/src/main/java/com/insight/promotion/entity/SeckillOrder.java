package com.insight.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 秒杀订单实体:对应 seckill_order 表(抢购成功的记录,④-2 写入) */
@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String requestId;

    private Long sessionId;

    private Long itemId;

    private Long userId;

    private Long skuId;

    private Integer seckillPrice;

    /** 1 已创建(未支付) */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
