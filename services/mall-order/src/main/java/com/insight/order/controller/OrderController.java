package com.insight.order.controller;

import com.insight.common.mq.OrderEventMessage;
import com.insight.common.result.Result;
import com.insight.order.dto.CreateOrderRequest;
import com.insight.order.dto.OrderVO;
import com.insight.order.entity.Order;
import com.insight.order.mq.OrderEventProducer;
import com.insight.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderEventProducer orderEventProducer;

    /** 创建订单(登录用户;userId 由网关从 JWT 解出放进请求头) */
    @PostMapping("/create")
    public Result<OrderVO> create(@RequestHeader("X-User-Id") Long userId,
                                  @RequestBody @Valid CreateOrderRequest req) {
        OrderVO vo = orderService.create(userId, req);
        // 发送点在 create 返回后:全局事务已提交,消息永不快于数据(notify ① 的知识点)
        OrderEventMessage event = new OrderEventMessage();
        event.setType(OrderEventMessage.TYPE_CREATED);
        event.setOrderId(vo.getOrderId());
        event.setUserId(userId);
        event.setPayAmount(vo.getPayAmount());
        orderEventProducer.send(event);
        return Result.success(vo);
    }

    /** 用户主动取消(仅待支付;30 分钟超时是系统自动关,两条路共用 closeOrderInternal) */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable("orderId") Long orderId) {
        Order closed = orderService.cancel(userId, orderId);
        if (closed != null) {
            // 幂等取消(订单已关)返回 null,不发事件——不重复打扰用户
            OrderEventMessage event = new OrderEventMessage();
            event.setType(OrderEventMessage.TYPE_CANCELLED);
            event.setOrderId(closed.getId());
            event.setUserId(closed.getUserId());
            event.setPayAmount(closed.getPayAmount());
            orderEventProducer.send(event);
        }
        return Result.success();
    }

    /** 健康探活 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
