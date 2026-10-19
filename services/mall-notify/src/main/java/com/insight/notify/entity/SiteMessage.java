package com.insight.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内信。id 用雪花:消息量大、纯追加写入,不依赖数据库自增顺序。
 */
@Data
@TableName("site_message")
public class SiteMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收人 */
    private Long userId;

    /** 业务唯一键:订单号-事件类型。uk_biz 唯一索引 = 消息幂等的数据库兜底 */
    private String bizId;

    private String title;

    private String content;

    /** 0 未读 1 已读 */
    private Integer isRead;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
