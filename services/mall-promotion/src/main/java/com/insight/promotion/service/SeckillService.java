package com.insight.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.promotion.dto.SeckillGoodsVO;
import com.insight.promotion.dto.SeckillOrderResponseVO;
import com.insight.promotion.dto.SeckillResultVO;
import com.insight.promotion.dto.SessionVO;
import com.insight.promotion.entity.SeckillItem;
import com.insight.promotion.entity.SeckillOrder;
import com.insight.promotion.entity.SeckillSession;
import com.insight.promotion.mapper.SeckillItemMapper;
import com.insight.promotion.mapper.SeckillOrderMapper;
import com.insight.promotion.mapper.SeckillSessionMapper;
import com.insight.promotion.mq.SeckillOrderMessage;
import com.insight.promotion.mq.SeckillOrderProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Service
public class SeckillService {

    /** 库存 key:seckill:stock:{itemId},值 = 实时剩余库存 */
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    /** 抢到标记 key:seckill:user:{itemId},Set 存已抢到的 userId(防重) */
    private static final String USER_KEY_PREFIX = "seckill:user:";

    /** 结果快照 key:seckill:result:{requestId},JSON = SeckillResultVO */
    private static final String RESULT_KEY_PREFIX = "seckill:result:";

    /** 结果快照存活 30 分钟:排队+消费早就完成,过期自动清理 */
    private static final long RESULT_TTL_MINUTES = 30;

    /** Lua 原子扣减脚本:检查+扣减+防重标记四步一次完成,见 resources/lua/seckill_deduct.lua */
    private static final DefaultRedisScript<Long> SECKILL_DEDUCT_SCRIPT;

    static {
        SECKILL_DEDUCT_SCRIPT = new DefaultRedisScript<>();
        SECKILL_DEDUCT_SCRIPT.setLocation(new ClassPathResource("lua/seckill_deduct.lua"));
        SECKILL_DEDUCT_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private SeckillSessionMapper sessionMapper;

    @Autowired
    private SeckillItemMapper itemMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeckillOrderProducer seckillOrderProducer;

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

        return IntStream.range(0, items.size())
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

    /** 秒杀下单(同步侧):轻校验 → 取号 → 写排队中 → 扔消息。重活全在消费端——这就是削峰。
     *  userId 来自网关验签后透传的 X-User-Id,身份以网关为准,不信客户端。 */
    public SeckillOrderResponseVO order(Long sessionId, Long itemId, Long userId) {
        // 1. 场次校验:存在 + 启用 + 时间窗口
        SeckillSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime())) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_STARTED);
        }
        if (now.isAfter(session.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_ENDED);
        }
        // 2. 商品校验:item 必须属于这个场次(防跨场次下单)
        SeckillItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getSessionId().equals(sessionId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 3. 第一层防重(快速拒绝):已抢到的人直接拦,不再进 MQ。
        //    并发窗口内的漏网之鱼由第二层(Lua 原子)、第三层(DB 唯一索引)兜底。
        Boolean grabbed = redisTemplate.opsForSet()
                .isMember(userKey(itemId), String.valueOf(userId));
        if (Boolean.TRUE.equals(grabbed)) {
            throw new BusinessException(ErrorCode.SECKILL_ALREADY_ORDERED);
        }
        // 4. 取号 + 写"排队中"快照(30 分钟过期)
        String requestId = UUID.randomUUID().toString().replace("-", "");
        SeckillResultVO snapshot = new SeckillResultVO();
        snapshot.setUserId(userId);
        snapshot.setStatus(SeckillResultVO.STATUS_QUEUING);
        writeResult(requestId, snapshot);
        // 5. 扔进 MQ:带上价格快照,消费端不再读活动表
        SeckillOrderMessage msg = new SeckillOrderMessage();
        msg.setRequestId(requestId);
        msg.setSessionId(sessionId);
        msg.setItemId(itemId);
        msg.setUserId(userId);
        msg.setSkuId(item.getSkuId());
        msg.setSeckillPrice(item.getSeckillPrice());
        seckillOrderProducer.send(msg);
        return new SeckillOrderResponseVO(requestId, SeckillResultVO.STATUS_QUEUING);
    }

    /** 消费侧:一条秒杀消息的完整处理(Lua 原子扣减 → 异步建单 → 改写结果)。
     *  抛异常 = 让消费者重试(建单失败回补后重投);能走到写结果的都是终态,天然幂等。 */
    public void handleSeckillMessage(SeckillOrderMessage msg) {
        // 1. Lua 原子扣减:检查 + 扣减 + 标记一步完成,任何并发请求都插不进来
        Long r = redisTemplate.execute(SECKILL_DEDUCT_SCRIPT,
                List.of(stockKey(msg.getItemId()), userKey(msg.getItemId())),
                String.valueOf(msg.getUserId()));
        int code = (r == null) ? -1 : r.intValue();

        SeckillResultVO result = new SeckillResultVO();
        result.setUserId(msg.getUserId());

        switch (code) {
            case 1 -> {
                SeckillOrder order = new SeckillOrder();
                order.setRequestId(msg.getRequestId());
                order.setSessionId(msg.getSessionId());
                order.setItemId(msg.getItemId());
                order.setUserId(msg.getUserId());
                order.setSkuId(msg.getSkuId());
                order.setSeckillPrice(msg.getSeckillPrice());
                order.setStatus(1);
                try {
                    // id 由 MyBatis-Plus 雪花算法生成(@TableId ASSIGN_ID)
                    seckillOrderMapper.insert(order);
                    result.setStatus(SeckillResultVO.STATUS_SUCCESS);
                    result.setOrderId(order.getId());
                    log.info("秒杀抢购成功, requestId={}, userId={}, orderId={}",
                            msg.getRequestId(), msg.getUserId(), order.getId());
                } catch (DuplicateKeyException e) {
                    // 第三层防重兜底(唯一索引):requestId 的单已存在 = 消息重投,补写结果即可
                    SeckillOrder existed = findOrderByRequestId(msg.getRequestId());
                    if (existed != null) {
                        result.setStatus(SeckillResultVO.STATUS_SUCCESS);
                        result.setOrderId(existed.getId());
                    } else {
                        // uk_user_item 冲突(同人同商品,Lua 没拦住的理论极端)
                        result.setStatus(SeckillResultVO.STATUS_FAILED);
                        result.setReason("已抢过");
                    }
                } catch (Exception e) {
                    // 建单失败(数据库抖动等):把 Lua 扣掉的还回去(摘标记 + 库存加回),
                    // 再抛出去让 MQ 重投——重投时重新走一遍完整流程。
                    redisTemplate.opsForSet()
                            .remove(userKey(msg.getItemId()), String.valueOf(msg.getUserId()));
                    redisTemplate.opsForValue().increment(stockKey(msg.getItemId()));
                    throw e;
                }
            }
            case 0 -> {
                result.setStatus(SeckillResultVO.STATUS_SOLD_OUT);
                log.info("秒杀已售罄, requestId={}, itemId={}", msg.getRequestId(), msg.getItemId());
            }
            case -1 -> {
                result.setStatus(SeckillResultVO.STATUS_FAILED);
                result.setReason("活动未预热,请稍后重试");
                log.error("秒杀库存 key 不存在(可能未预热), requestId={}, itemId={}",
                        msg.getRequestId(), msg.getItemId());
            }
            case -2 -> {
                // 已在抢到标记里:查一次是"重投补写结果"还是"真重复抢购"
                SeckillOrder existed = findOrderByRequestId(msg.getRequestId());
                if (existed != null) {
                    result.setStatus(SeckillResultVO.STATUS_SUCCESS);
                    result.setOrderId(existed.getId());
                } else {
                    result.setStatus(SeckillResultVO.STATUS_FAILED);
                    result.setReason("已抢过");
                }
            }
            default -> {
                result.setStatus(SeckillResultVO.STATUS_FAILED);
                result.setReason("未知错误");
            }
        }
        writeResult(msg.getRequestId(), result);
    }

    /** 轮询结果:只认自己取的号(快照里存了 userId,防拿别人的 requestId 偷看) */
    public SeckillResultVO result(Long sessionId, String requestId, Long userId) {
        String json = redisTemplate.opsForValue().get(resultKey(requestId));
        if (json == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        try {
            SeckillResultVO vo = objectMapper.readValue(json, SeckillResultVO.class);
            if (!vo.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            return vo;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /** 按 requestId 查订单:重投幂等时确认"单其实早就建好了" */
    private SeckillOrder findOrderByRequestId(String requestId) {
        return seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getRequestId, requestId));
    }

    /** 写结果快照(下单写排队中,消费端写终态,共用这个方法) */
    private void writeResult(String requestId, SeckillResultVO snapshot) {
        try {
            redisTemplate.opsForValue().set(resultKey(requestId),
                    objectMapper.writeValueAsString(snapshot),
                    RESULT_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String stockKey(Long itemId) {
        return STOCK_KEY_PREFIX + itemId;
    }

    private String userKey(Long itemId) {
        return USER_KEY_PREFIX + itemId;
    }

    private String resultKey(String requestId) {
        return RESULT_KEY_PREFIX + requestId;
    }
}
