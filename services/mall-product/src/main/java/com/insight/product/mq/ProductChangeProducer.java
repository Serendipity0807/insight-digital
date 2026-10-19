package com.insight.product.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.mq.ProductChangeMessage;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 商品变更消息生产者:商品上下架后,把"ES 该变成什么样"广播出去。
 * 失败只记日志、不阻断主流程——ES 最多滞后,等下一次全量同步兜底。
 */
@Slf4j
@Component
public class ProductChangeProducer {

    public static final String TOPIC = "product-change";

    private final DefaultMQProducer producer;
    private final ObjectMapper objectMapper;

    public ProductChangeProducer(@Value("${rocketmq.name-server}") String nameServer,
                                 ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        producer = new DefaultMQProducer("product-change-producer-group");
        producer.setNamesrvAddr(nameServer);
        try {
            producer.start();
        } catch (Exception e) {
            // MQ 挂了不能挡住商品服务:启动失败只记日志,发送失败同样只记日志
            log.error("RocketMQ 生产者启动失败, nameServer={}", nameServer, e);
        }
    }

    /** 发送变更消息。失败只记日志(消息没送到 = ES 滞后,不丢业务数据) */
    public void send(ProductChangeMessage message) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(message);
            producer.send(new Message(TOPIC, body));
            log.info("商品变更消息已发送, type={}, spuId={}", message.getType(), message.getSpuId());
        } catch (Exception e) {
            log.error("商品变更消息发送失败, type={}, spuId={}",
                    message.getType(), message.getSpuId(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
