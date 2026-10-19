package com.insight.promotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 场次信息:前端根据 startTime/endTime 自己算"进行中/即将开始/已结束" */
@Data
@AllArgsConstructor
public class SessionVO {

    private Long id;

    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;
}
