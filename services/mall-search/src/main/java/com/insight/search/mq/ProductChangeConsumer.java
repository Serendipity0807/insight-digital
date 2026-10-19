package com.insight.search.mq;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.mq.ProductChangeMessage;
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
 * 商品变更消息消费者:收到消息 = ES 里有一个文档要变。
 * UPSERT → 覆盖写(与全量脚本同一个别名、同一个 _id,天然幂等);
 * DELETE → 删文档(先 exists 探路:对不存在的 _id 执行 delete,ES 返回 404 会被当成异常抛)。
 * 失败返回 RECONSUME_LATER 重投——配合覆盖写的幂等性,重试多少次都不会产生脏数据。
 */
@Slf4j
@Component
public class ProductChangeConsumer {

    /** 写入/删除都走别名:以后换索引只动别名指向,代码一个字不改 */
    private static final String ALIAS = "product_v1";

    private final DefaultMQPushConsumer consumer;

    @Autowired
    public ProductChangeConsumer(@Value("${rocketmq.name-server}") String nameServer,
                                 ElasticsearchClient esClient,
                                 ObjectMapper objectMapper) {
        consumer = new DefaultMQPushConsumer("product-change-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        try {
            consumer.subscribe("product-change", "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (org.apache.rocketmq.common.message.MessageExt msg : msgs) {
                    try {
                        ProductChangeMessage change = objectMapper.readValue(
                                new String(msg.getBody(), StandardCharsets.UTF_8),
                                ProductChangeMessage.class);
                        apply(esClient, change);
                    } catch (Exception e) {
                        // 写 ES 失败:消息重投,幂等设计保证重试安全
                        log.error("商品变更消费失败, msgId={}", msg.getMsgId(), e);
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

    /** 按消息类型更新 ES */
    private void apply(ElasticsearchClient esClient, ProductChangeMessage change) throws Exception {
        String docId = String.valueOf(change.getSpuId());
        if (ProductChangeMessage.TYPE_UPSERT.equals(change.getType())) {
            // refresh=true:演示用,写完立刻可搜;生产环境让它按默认 1 秒刷,省资源
            esClient.index(i -> i.index(ALIAS)
                    .id(docId)
                    .document(change.getDoc())
                    .refresh(Refresh.True));
            log.info("ES 文档已更新, spuId={}", change.getSpuId());
        } else if (ProductChangeMessage.TYPE_DELETE.equals(change.getType())) {
            boolean exists = esClient.exists(e -> e.index(ALIAS).id(docId)).value();
            if (exists) {
                esClient.delete(d -> d.index(ALIAS).id(docId).refresh(Refresh.True));
                log.info("ES 文档已删除, spuId={}", change.getSpuId());
            } else {
                log.info("ES 文档不存在,跳过删除, spuId={}", change.getSpuId());
            }
        } else {
            log.warn("未知消息类型,忽略, type={}, spuId={}", change.getType(), change.getSpuId());
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.shutdown();
    }
}
