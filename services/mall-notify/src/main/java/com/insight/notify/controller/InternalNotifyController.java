package com.insight.notify.controller;

import com.insight.common.result.Result;
import com.insight.notify.dto.SmsRequest;
import com.insight.notify.service.NotifyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 服务间接口:/internal/** 不经网关,只允许服务网内直连(蓝图铁律)。
 * 别的服务用 Feign 调这里;本课验收用 Apifox 直连 8089。
 */
@RestController
@RequestMapping("/internal/notify")
public class InternalNotifyController {

    @Autowired
    private NotifyService notifyService;

    /** 发短信:落 sms_log + 事务消息转 notify-job 异步发送,接口秒回 */
    @PostMapping("/sms")
    public Result<Void> sendSms(@RequestBody @Valid SmsRequest req) {
        notifyService.sendSms(req.getPhone(), req.getScene(), req.getContent());
        return Result.success();
    }
}
