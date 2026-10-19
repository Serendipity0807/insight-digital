package com.insight.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.promotion.dto.SeckillGoodsVO;
import com.insight.promotion.dto.SessionVO;
import com.insight.promotion.entity.SeckillItem;
import com.insight.promotion.entity.SeckillSession;
import com.insight.promotion.mapper.SeckillItemMapper;
import com.insight.promotion.mapper.SeckillSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SeckillService {

    /** 库存 key 模板:seckill:stock:{itemId},值 = 实时剩余库存 */
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    @Autowired
    private SeckillSessionMapper sessionMapper;

    @Autowired
    private SeckillItemMapper itemMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

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

    /** 场次商品列表。stock 显示"实时剩余":Redis 有预热数据用 Redis,没有则回退 DB 底账 */
    public List<SeckillGoodsVO> listGoods(Long sessionId) {
        log.info("查询场次商品, sessionId={}", sessionId);
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getStatus() != 1) {
            return List.of();
        }
        List<SeckillItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<SeckillItem>()
                        .eq(SeckillItem::getSessionId, sessionId));

        // 批量读实时库存:一次 multiGet 往返拿全,而不是循环 GET(N+1 教训的 Redis 版)
        List<String> keys = items.stream().map(i -> stockKey(i.getId())).toList();
        List<String> stocks = redisTemplate.opsForValue().multiGet(keys);

        return java.util.stream.IntStream.range(0, items.size())
                .mapToObj(j -> {
                    SeckillItem i = items.get(j);
                    String v = stocks.get(j);
                    int remain = v == null ? i.getStock() : Integer.parseInt(v);
                    return new SeckillGoodsVO(i.getId(), i.getSkuId(), i.getSeckillPrice(), remain);
                })
                .toList();
    }

    /** 预热:把场次所有商品的活动库存抄进 Redis。返回预热条数。
     *  生产环境由定时任务在开抢前触发;这里允许"未结束"的场次预热(种子场次是进行中的),
     *  但"防重复预热"这条真保护必须有——重复预热会把已扣减的库存刷回原值 = 超卖事故。 */
    public int warmup(Long sessionId) {
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (session.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.SECKILL_ENDED);
        }
        List<SeckillItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<SeckillItem>()
                        .eq(SeckillItem::getSessionId, sessionId));
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 防重复预热:任一库存 key 已存在就拒绝(countExistingKeys = 批量 exists,一次往返)
        Long existed = redisTemplate.countExistingKeys(
                items.stream().map(i -> stockKey(i.getId())).toList());
        if (existed != null && existed > 0) {
            throw new BusinessException(ErrorCode.SECKILL_ALREADY_WARMED);
        }
        // 抄写库存:set 覆盖写(此处全量新写,值来自 DB 底账)
        items.forEach(i -> redisTemplate.opsForValue()
                .set(stockKey(i.getId()), String.valueOf(i.getStock())));
        log.info("秒杀场次预热完成, sessionId={}, 商品数={}", sessionId, items.size());
        return items.size();
    }

    private String stockKey(Long itemId) {
        return STOCK_KEY_PREFIX + itemId;
    }
}
