package com.insight.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 站内信出参:实体有的字段不是全给前端,bizId/userId 是内部字段,不暴露 */
@Data
public class MessageVO {

    private Long id;

    private String title;

    private String content;

    /** 0 未读 1 已读 */
    private Integer isRead;

    private LocalDateTime createdAt;
}
