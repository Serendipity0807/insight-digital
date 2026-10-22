package com.insight.order.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 订单列表项:列表只给摘要,明细另查详情接口 */
@Data
public class OrderListItemVO {

    private Long orderId;
    /** 实付金额(分) */
    private Integer payAmount;
    /** 状态机:0已取消 10待支付 20已支付 30已发货 40已完成 50退款中 60已退款 */
    private Integer status;
    private LocalDateTime createdAt;
}
