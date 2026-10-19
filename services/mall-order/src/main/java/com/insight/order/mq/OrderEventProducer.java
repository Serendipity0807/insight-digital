package com.insight.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.mq.OrderEventMessage;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 订单事件生产者:订单生命周期一变(创建/取消),就往 order-event topic 发一条事件。
 * 谁在听?mall-notify——发站内信/短信。下单链路不管通知死活,这就是解耦。
 */
@Slf4j
@Component
public class OrderEventProducer {

    private final DefaultMQProducer producer;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(@Value("${rocketmq.name-server}") String nameServer,
                              ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        producer = new DefaultMQProducer("order-event-producer-group");
        producer.setNamesrvAddr(nameServer);
        try {
            producer.start();
        } catch (Exception e) {
            // MQ 挂了不能挡住下单:启动失败只记日志,发送失败同样只记日志
            log.error("订单事件生产者启动失败, nameServer={}", nameServer, e);
        }
    }

    /** 发订单事件。失败只记日志:通知是"尽力而为",不能反过来影响下单主流程 */
    public void send(OrderEventMessage event) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            producer.send(new Message(OrderEventMessage.TOPIC, body));
        } catch (Exception e) {
            log.error("订单事件发送失败, orderId={}, type={}", event.getOrderId(), event.getType(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
