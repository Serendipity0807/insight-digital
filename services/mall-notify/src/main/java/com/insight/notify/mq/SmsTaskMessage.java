package com.insight.notify.mq;

import lombok.Data;

/**
 * 短信任务消息:notify 自产自销——接口落库后,由 notify-job topic 的消费者"真正发送"。
 * 自产自销的消息只在服务内部流转,契约放本服务包里,不进 common:
 * common 只放跨服务的协议(OrderEventMessage 那种),否则会膨胀成垃圾场。
 */
@Data
public class SmsTaskMessage {

    public static final String TOPIC = "notify-job";

    /** 与 sms_log.biz_id 一一对应:消费者据此回写状态,回查据此判断业务是否落地 */
    private String bizId;

    private String phone;

    private String scene;

    private String content;
}
