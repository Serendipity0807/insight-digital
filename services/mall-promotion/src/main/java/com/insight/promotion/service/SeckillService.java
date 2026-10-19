package com.insight.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insight.promotion.dto.SeckillGoodsVO;
import com.insight.promotion.dto.SessionVO;
import com.insight.promotion.entity.SeckillItem;
import com.insight.promotion.entity.SeckillSession;
import com.insight.promotion.mapper.SeckillItemMapper;
import com.insight.promotion.mapper.SeckillSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SeckillService {

    @Autowired
    private SeckillSessionMapper sessionMapper;

    @Autowired
    private SeckillItemMapper itemMapper;

    /** 场次列表:全部启用场次,按开始时间排(前端按时间算状态,服务端不掺和) */
    public List<SessionVO> listSessions() {
        log.info("查询秒杀场次列表");
        return sessionMapper.selectList(
                        new LambdaQueryWrapper<SeckillSession>()
                                .eq(SeckillSession::getStatus, 1)
                                .orderByAsc(SeckillSession::getStartTime))
                .stream()
                .map(s -> new SessionVO(
                        s.getId(), s.getName(), s.getStartTime(), s.getEndTime(), s.getStatus()))
                .toList();
    }

    /** 场次商品列表。场次不存在/停用 → 空列表(前端显示空态即可,不报错) */
    public List<SeckillGoodsVO> listGoods(Long sessionId) {
        log.info("查询场次商品, sessionId={}", sessionId);
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getStatus() != 1) {
            return List.of();
        }
        return itemMapper.selectList(
                        new LambdaQueryWrapper<SeckillItem>()
                                .eq(SeckillItem::getSessionId, sessionId))
                .stream()
                .map(i -> new SeckillGoodsVO(
                        i.getId(), i.getSkuId(), i.getSeckillPrice(), i.getStock()))
                .toList();
    }
}
