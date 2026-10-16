package com.insight.order.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 下单成功响应:给前端跳收银台用的最少信息 */
@Data
public class OrderVO {

    private Long orderId;
    /** 实付金额(分) */
    private Integer payAmount;
    /** 支付截止时间:超时未支付会被系统关单 */
    private LocalDateTime expireTime;
}
