package com.insight.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信发送日志。状态机:0 待发送 → 1 成功(消费者"发送"后回写)。
 * biz_id 是事务消息的锚点:回查查它、消费者回写也查它。
 */
@Data
@TableName("sms_log")
public class SmsLog {

    /** 0 待发送(事务消息落库时) */
    public static final int STATUS_PENDING = 0;
    /** 1 发送成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 2 发送失败(预留:真实发送失败时改,靠重试恢复) */
    public static final int STATUS_FAILED = 2;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务唯一键(UUID),唯一索引 uk_biz */
    private String bizId;

    private String phone;

    private String scene;

    private String content;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
