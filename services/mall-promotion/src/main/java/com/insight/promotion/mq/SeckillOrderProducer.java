package com.insight.promotion.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 秒杀削峰消息生产者:下单请求不直接扣库存,先扔进队列排队。
 * 失败只记日志——结果快照会一直停在"排队中"直到过期,前端提示稍后重试即可。
 */
@Slf4j
@Component
public class SeckillOrderProducer {

    public static final String TOPIC = "seckill-order";

    private final DefaultMQProducer producer;
    private final ObjectMapper objectMapper;

    public SeckillOrderProducer(@Value("${rocketmq.name-server}") String nameServer,
                                ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        producer = new DefaultMQProducer("seckill-order-producer-group");
        producer.setNamesrvAddr(nameServer);
        try {
            producer.start();
        } catch (Exception e) {
            log.error("RocketMQ 生产者启动失败, nameServer={}", nameServer, e);
        }
    }

    public void send(SeckillOrderMessage message) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(message);
            producer.send(new Message(TOPIC, body));
            log.info("秒杀削峰消息已发送, requestId={}, userId={}", message.getRequestId(), message.getUserId());
        } catch (Exception e) {
            log.error("秒杀削峰消息发送失败, requestId={}", message.getRequestId(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
