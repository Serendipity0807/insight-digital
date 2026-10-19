package com.insight.notify.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.mq.OrderEventMessage;
import com.insight.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 订单事件消费者:订阅 order-event,订单创建/取消 → 生成站内信。
 * 失败返回 RECONSUME_LATER,Broker 按退避策略重投——配合站内信 uk_biz 唯一键,重投天然幂等。
 */
@Slf4j
@Component
public class OrderEventConsumer {

    public OrderEventConsumer(@Value("${rocketmq.name-server}") String nameServer,
                              ObjectMapper objectMapper,
                              NotifyService notifyService) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("notify-order-event-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        // 从上次消费位置续读:重启不丢消息、不重复拉取
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(OrderEventMessage.TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            // 逐条处理:一条失败只重投这一条,不拖累同批其他消息(秒杀消费者同款写法)
            for (MessageExt msg : msgs) {
                try {
                    OrderEventMessage event = objectMapper.readValue(
                            new String(msg.getBody(), StandardCharsets.UTF_8), OrderEventMessage.class);
                    notifyService.handleOrderEvent(event);
                } catch (Exception e) {
                    log.error("订单事件消费失败, msgId={}, offset={}, 将重投", msg.getMsgId(), msg.getQueueOffset(), e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        log.info("订单事件消费者已启动, topic={}", OrderEventMessage.TOPIC);
    }
}
