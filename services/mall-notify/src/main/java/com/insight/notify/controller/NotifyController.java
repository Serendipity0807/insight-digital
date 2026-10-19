package com.insight.notify.controller;

import com.insight.common.result.PageResult;
import com.insight.common.result.Result;
import com.insight.notify.dto.MessageVO;
import com.insight.notify.service.NotifyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/notify")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    /** 我的站内信(分页,新的在前) */
    @GetMapping("/message")
    public Result<PageResult<MessageVO>> page(@RequestHeader("X-User-Id") Long userId,
                                              @RequestParam(defaultValue = "1") @Min(1) int pageNum,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        return Result.success(notifyService.page(userId, pageNum, pageSize));
    }

    /** 未读数(前端铃铛角标) */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(notifyService.unreadCount(userId));
    }

    /** 标记已读(幂等:重复点不报错) */
    @PutMapping("/message/{id}/read")
    public Result<Void> markRead(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable("id") Long messageId) {
        notifyService.markRead(userId, messageId);
        return Result.success();
    }
}
