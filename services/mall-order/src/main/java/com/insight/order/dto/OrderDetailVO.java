package com.insight.order.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** 订单详情:主表字段 + 明细快照列表 */
@Data
public class OrderDetailVO {

    private Long orderId;
    private Integer totalAmount;
    private Integer discountAmount;
    private Integer payAmount;
    /** 状态机:0已取消 10待支付 20已支付 30已发货 40已完成 50退款中 60已退款 */
    private Integer status;
    private String addressSnapshot;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
    private List<OrderItemVO> items;
}
