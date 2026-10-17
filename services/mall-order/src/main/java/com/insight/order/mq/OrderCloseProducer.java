package com.insight.order.mq;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 关单延时消息生产者:下单成功后投递一条"30 分钟后检查这单"的消息。
 * 延时由 Broker 负责,不是消费者 sleep——消息到消费者手里时,正好是支付截止时间。
 */
@Slf4j
@Component
public class OrderCloseProducer {

    public static final String TOPIC = "order-close";

    private final DefaultMQProducer producer;
    private final int delayLevel;

    public OrderCloseProducer(@Value("${rocketmq.name-server}") String nameServer,
                              @Value("${rocketmq.close-delay-level}") int delayLevel) {
        this.delayLevel = delayLevel;
        producer = new DefaultMQProducer("order-close-producer-group");
        producer.setNamesrvAddr(nameServer);
        try {
            producer.start();
        } catch (Exception e) {
            // MQ 挂了不能挡住下单:启动失败只记日志,发送失败同样只记日志
            log.error("RocketMQ 生产者启动失败, nameServer={}", nameServer, e);
        }
    }

    /** 投递延时关单消息。失败只记日志、不阻断下单(生产环境用事务消息/本地消息表兜底) */
    public void sendCloseMessage(Long orderId) {
        Message msg = new Message(TOPIC,
                ("{\"orderId\":" + orderId + "}").getBytes(StandardCharsets.UTF_8));
        msg.setDelayTimeLevel(delayLevel);
        try {
            producer.send(msg);
        } catch (Exception e) {
            log.error("关单延时消息发送失败, orderId={}", orderId, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
