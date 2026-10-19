package com.insight.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.common.mq.OrderEventMessage;
import com.insight.common.result.PageResult;
import com.insight.notify.dto.MessageVO;
import com.insight.notify.entity.SiteMessage;
import com.insight.notify.mapper.SiteMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class NotifyService {

    @Autowired
    private SiteMessageMapper siteMessageMapper;

    /** 我的站内信:按 id 倒序(雪花 id 单调递增,倒序 = 新的在前),分页 */
    public PageResult<MessageVO> page(Long userId, int pageNum, int pageSize) {
        Page<SiteMessage> p = new Page<>(pageNum, pageSize);
        siteMessageMapper.selectPage(p, new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, userId)
                .orderByDesc(SiteMessage::getId));
        List<MessageVO> records = p.getRecords().stream().map(m -> {
            MessageVO vo = new MessageVO();
            vo.setId(m.getId());
            vo.setTitle(m.getTitle());
            vo.setContent(m.getContent());
            vo.setIsRead(m.getIsRead());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).toList();
        return PageResult.of(p.getTotal(), records);
    }

    /** 未读数:前端消息铃铛角标(notify ② 的前端课用) */
    public long unreadCount(Long userId) {
        return siteMessageMapper.selectCount(new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, userId)
                .eq(SiteMessage::getIsRead, 0));
    }

    /** 标记已读。rows==0 有两种可能:已读过(幂等,静默成功)或消息不存在/不属于我(报错)——查一次区分 */
    public void markRead(Long userId, Long messageId) {
        int rows = siteMessageMapper.update(null,
                new LambdaUpdateWrapper<SiteMessage>()
                        .set(SiteMessage::getIsRead, 1)
                        .eq(SiteMessage::getId, messageId)
                        .eq(SiteMessage::getUserId, userId)   // 只改自己的:越权更新直接 0 行
                        .eq(SiteMessage::getIsRead, 0));     // 只改未读的:重复点自然幂等
        if (rows > 0) {
            return;
        }
        SiteMessage m = siteMessageMapper.selectById(messageId);
        if (m == null || !m.getUserId().equals(userId)) {
            // 不存在/是别人的:统一报"不存在",不暴露消息到底存不存在(防探测)
            throw new BusinessException(ErrorCode.NOTIFY_MESSAGE_NOT_FOUND);
        }
    }

    /** 消费订单事件 → 生成站内信。
     *  消费幂等两层:①同一事件重投,bizId 相同,uk_biz 唯一索引直接 DuplicateKey,捕获后忽略;
     *  ②不同事件类型(如 CANCELLED)不同 bizId,互不干扰。 */
    public void handleOrderEvent(OrderEventMessage msg) {
        if (!OrderEventMessage.TYPE_CREATED.equals(msg.getType())) {
            log.info("未处理的事件类型, type={}", msg.getType());
            return;
        }
        SiteMessage m = new SiteMessage();
        m.setUserId(msg.getUserId());
        m.setBizId(msg.getOrderId() + "-" + msg.getType());   // 一单一事件一条消息:幂等键
        m.setTitle("下单成功");
        m.setContent("您的订单 " + msg.getOrderId() + " 已创建成功,实付 ¥"
                + new BigDecimal(msg.getPayAmount()).movePointLeft(2) + ",请在 30 分钟内完成支付");
        m.setIsRead(0);
        try {
            siteMessageMapper.insert(m);
            log.info("站内信已生成, orderId={}, userId={}", msg.getOrderId(), msg.getUserId());
        } catch (DuplicateKeyException e) {
            log.info("站内信已存在(消息重投),忽略, bizId={}", m.getBizId());
        }
    }
}
