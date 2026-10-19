package com.insight.notify.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 短信任务事务消息生产者:发半消息 + 挂事务监听器。
 */
@Slf4j
@Component
public class SmsTransactionProducer {

    private final TransactionMQProducer producer;
    private final ObjectMapper objectMapper;

    public SmsTransactionProducer(@Value("${rocketmq.name-server}") String nameServer,
                                  SmsTransactionListener listener,
                                  ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        producer = new TransactionMQProducer("notify-sms-transaction-group");
        producer.setNamesrvAddr(nameServer);
        producer.setTransactionListener(listener);
        // 铁律:回查必须跑在独立线程池,不能占发送线程——
        // 否则生产者的发送与回查互相占用线程,会死锁
        producer.setExecutorService(new ThreadPoolExecutor(
                2, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                r -> new Thread(r, "sms-tx-check")));
        try {
            producer.start();
        } catch (Exception e) {
            // MQ 挂了不能让服务起不来:记日志,发送时再报错(与 OrderCloseProducer 同款策略)
            log.error("短信事务消息生产者启动失败, nameServer={}", nameServer, e);
        }
    }

    /** 发半消息。COMMIT/ROLLBACK 由监听器决定,这里只管投递 */
    public void sendHalf(SmsTaskMessage task) throws Exception {
        producer.sendMessageInTransaction(
                new Message(SmsTaskMessage.TOPIC, objectMapper.writeValueAsBytes(task)), null);
    }

    @PreDestroy
    public void shutdown() {
        producer.shutdown();
    }
}
