package com.insight.order.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.mq.OrderEventMessage;
import com.insight.order.entity.Order;
import com.insight.order.service.OrderService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 关单延时消息消费者:收到消息 = 有一笔订单到了支付截止时间。
 * 关单成功后补发 CANCELLED 事件 → mall-notify 生成"订单已关闭"站内信。
 * 异常返回 RECONSUME_LATER(稍后重投),配合 closeOrder 的 @GlobalTransactional
 * 回滚,失败会自动重试——这就是"至少一次投递 + 全局事务"的组合拳。
 */
@Slf4j
@Component
public class OrderCloseConsumer {

    private final DefaultMQPushConsumer consumer;

    @Autowired
    public OrderCloseConsumer(@Value("${rocketmq.name-server}") String nameServer,
                              OrderService orderService,
                              ObjectMapper objectMapper,
                              OrderEventProducer orderEventProducer) {
        consumer = new DefaultMQPushConsumer("order-close-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        try {
            consumer.subscribe(OrderCloseProducer.TOPIC, "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (org.apache.rocketmq.common.message.MessageExt msg : msgs) {
                    try {
                        JsonNode node = objectMapper.readTree(
                                new String(msg.getBody(), StandardCharsets.UTF_8));
                        Order closed = orderService.closeOrder(node.get("orderId").asLong());
                        if (closed != null) {
                            // 真关成了才发事件:0 行 = 已支付/已取消/重复消息,
                            // 那种情况绝不发"订单已关闭"——用户已支付的订单收到取消通知就是事故
                            OrderEventMessage event = new OrderEventMessage();
                            event.setType(OrderEventMessage.TYPE_CANCELLED);
                            event.setOrderId(closed.getId());
                            event.setUserId(closed.getUserId());
                            event.setPayAmount(closed.getPayAmount());
                            orderEventProducer.send(event);
                        }
                    } catch (Exception e) {
                        // 业务失败(如释放库存冲突):消息重投,配合全局事务回滚后再试
                        log.error("关单处理失败, msgId={}", msg.getMsgId(), e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch (Exception e) {
            // MQ 没起来不能让服务起不来:记日志,服务照常启动(Broker 恢复后重启本服务)
            log.error("RocketMQ 消费者启动失败, nameServer={}", nameServer, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.shutdown();
    }
}
