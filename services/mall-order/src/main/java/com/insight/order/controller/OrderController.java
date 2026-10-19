package com.insight.order.controller;

import com.insight.common.mq.OrderEventMessage;
import com.insight.common.result.Result;
import com.insight.order.dto.CreateOrderRequest;
import com.insight.order.dto.OrderVO;
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
        // 发送点在这里而不是 Service 内,是本课最重要的一个位置决策:
        // create() 是 @GlobalTransactional,方法正常返回 = 全局事务已提交,
        // 此刻发事件,notify 收到时订单一定真实存在——"消息永不快于数据"。
        OrderEventMessage event = new OrderEventMessage();
        event.setType(OrderEventMessage.TYPE_CREATED);
        event.setOrderId(vo.getOrderId());
        event.setUserId(userId);
        event.setPayAmount(vo.getPayAmount());
        orderEventProducer.send(event);
        return Result.success(vo);
    }

    /** 健康探活 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
