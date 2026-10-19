package com.insight.notify.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.notify.entity.SmsLog;
import com.insight.notify.mapper.SmsLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 短信任务消费者:notify 自产自销的另一半。
 * "发送短信"本项目是模拟(真实项目这里调第三方短信 API——秒级耗时、可能超时,
 * 所以必须异步:接口秒回,发送在后台慢慢做,失败 RECONSUME_LATER 重试)。
 * 幂等:status 0→1 条件更新,0 行 = 已发过(消息重投),跳过。
 */
@Slf4j
@Component
public class SmsJobConsumer {

    public SmsJobConsumer(@Value("${rocketmq.name-server}") String nameServer,
                          ObjectMapper objectMapper,
                          SmsLogMapper smsLogMapper) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("notify-sms-job-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(SmsTaskMessage.TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    SmsTaskMessage task = objectMapper.readValue(msg.getBody(), SmsTaskMessage.class);

                    // 模拟发送:真实项目这里是第三方短信 API 调用
                    log.info("[模拟发送短信] phone={}, scene={}, content={}",
                            task.getPhone(), task.getScene(), task.getContent());

                    // 状态回写:0→1 条件更新。0 行 = 重投且已发过,幂等跳过
                    int rows = smsLogMapper.update(null,
                            new LambdaUpdateWrapper<SmsLog>()
                                    .set(SmsLog::getStatus, SmsLog.STATUS_SUCCESS)
                                    .eq(SmsLog::getBizId, task.getBizId())
                                    .eq(SmsLog::getStatus, SmsLog.STATUS_PENDING));
                    if (rows == 0) {
                        log.info("短信任务已处理过(重投),跳过, bizId={}", task.getBizId());
                    }
                } catch (Exception e) {
                    log.error("短信任务处理失败, msgId={}, 将重投", msg.getMsgId(), e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        log.info("短信任务消费者已启动, topic={}", SmsTaskMessage.TOPIC);
    }
}
