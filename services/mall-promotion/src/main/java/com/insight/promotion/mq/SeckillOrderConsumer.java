package com.insight.promotion.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.promotion.service.SeckillService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 秒杀削峰消息消费者:按自己的节奏匀速处理下单请求,这就是"削峰"的出口。
 * 消费者是薄壳,业务全在 SeckillService.handleSeckillMessage。
 * 失败返回 RECONSUME_LATER 重投——整条处理幂等(Lua 原子防重 + 唯一索引兜底),重试安全;
 * 批量重投时已成功的消息会再走一遍幂等分支,无副作用。
 */
@Slf4j
@Component
public class SeckillOrderConsumer {

    private final SeckillService seckillService;
    private final ObjectMapper objectMapper;
    private final DefaultMQPushConsumer consumer;

    public SeckillOrderConsumer(@Value("${rocketmq.name-server}") String nameServer,
                                SeckillService seckillService,
                                ObjectMapper objectMapper) {
        this.seckillService = seckillService;
        this.objectMapper = objectMapper;
        consumer = new DefaultMQPushConsumer("seckill-order-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        try {
            consumer.subscribe(SeckillOrderProducer.TOPIC, "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt msg : msgs) {
                    try {
                        SeckillOrderMessage body = objectMapper.readValue(
                                msg.getBody(), SeckillOrderMessage.class);
                        seckillService.handleSeckillMessage(body);
                    } catch (Exception e) {
                        log.error("秒杀消息处理失败,将重投, msgId={}", msg.getMsgId(), e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch (Exception e) {
            log.error("RocketMQ 消费者启动失败, nameServer={}", nameServer, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.shutdown();
    }
}
