package com.insight.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 订单状态日志:只追加,谁也不能改 */
@Data
@TableName("order_log")
public class OrderLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    /** 流转前状态;首条为 null(订单诞生,不是从某状态"变"过来的) */
    private Integer fromStatus;
    private Integer toStatus;
    /** 触发方:user/system/admin */
    private String operator;
    private String remark;
    private LocalDateTime createdAt;
}
