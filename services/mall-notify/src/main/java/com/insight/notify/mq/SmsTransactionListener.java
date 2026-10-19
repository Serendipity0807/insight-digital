package com.insight.notify.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.notify.entity.SmsLog;
import com.insight.notify.mapper.SmsLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 事务监听器:Broker 一共只问两个问题,这里两问两答。
 * 第一问"本地事务成了吗"→ 执行插库,按结果答;
 * 第二问"上次没答完,现在成了吗"(回查)→ 查库答。
 */
@Slf4j
@Component
public class SmsTransactionListener implements TransactionListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SmsLogMapper smsLogMapper;

    /**
     * 第一问:执行本地事务——这里只有一件事:短信日志插库(status=0 待发送)。
     * 插成功 → COMMIT,半消息转正,消费者才会"真正发送";
     * 插失败 → ROLLBACK,消息丢弃——日志和消息要么都有,要么都没有,两难被原子化。
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            SmsTaskMessage task = objectMapper.readValue(msg.getBody(), SmsTaskMessage.class);
            SmsLog logRow = new SmsLog();
            logRow.setBizId(task.getBizId());
            logRow.setPhone(task.getPhone());
            logRow.setScene(task.getScene());
            logRow.setContent(task.getContent());
            logRow.setStatus(SmsLog.STATUS_PENDING);
            smsLogMapper.insert(logRow);
            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("短信日志落库失败,半消息回滚, bizId 未知", e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * 第二问(回查):execute 执行到一半进程崩了,Broker 来问"业务到底成了没"——查库回答。
     * 日志在 → COMMIT 补转正;不在 → ROLLBACK 丢弃;
     * 回查本身失败(数据库抖动)→ UNKNOW,让 Broker 稍后再问,而不是直接判死刑。
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        try {
            SmsTaskMessage task = objectMapper.readValue(msg.getBody(), SmsTaskMessage.class);
            Long count = smsLogMapper.selectCount(
                    new LambdaQueryWrapper<SmsLog>().eq(SmsLog::getBizId, task.getBizId()));
            return count != null && count > 0
                    ? LocalTransactionState.COMMIT_MESSAGE
                    : LocalTransactionState.ROLLBACK_MESSAGE;
        } catch (Exception e) {
            log.error("短信日志回查失败,稍后重查, bizId 未知", e);
            return LocalTransactionState.UNKNOW;
        }
    }
}
