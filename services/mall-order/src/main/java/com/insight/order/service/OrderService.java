package com.insight.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.common.result.Result;
import com.insight.order.client.InventoryClient;
import com.insight.order.client.LockRequest;
import com.insight.order.client.ProductClient;
import com.insight.order.client.ProductSkuVO;
import com.insight.order.dto.CreateOrderRequest;
import com.insight.order.dto.OrderVO;
import com.insight.order.entity.Order;
import com.insight.order.entity.OrderItem;
import com.insight.order.entity.OrderLog;
import com.insight.order.mapper.OrderItemMapper;
import com.insight.order.mapper.OrderLogMapper;
import com.insight.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    /** 状态机:10 待支付 */
    private static final int STATUS_PENDING = 10;
    /** 支付时限(分钟) */
    private static final int PAY_EXPIRE_MINUTES = 30;
    /** 状态日志触发方 */
    private static final String OPERATOR_SYSTEM = "system";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private ObjectMapper objectMapper;

    /** 编程式事务:Spring Boot 自动配置好了这个 Bean,直接注入 */
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 全局事务入口(TM)。锁库存失败 → 异常传播 → TC 全局回滚 → 订单分支被 undo。
     * 第⑤步留的脏订单,被这一行注解消灭。
     */
    @GlobalTransactional
    public OrderVO create(Long userId, CreateOrderRequest req) {
        // 1. 拉权威商品数据 + 校验(不存在的 SKU → 10501)
        Map<Long, ProductSkuVO> skuMap = loadAndCheckSkus(req);

        // 2. 组装明细快照,服务端算钱
        List<OrderItem> items = new ArrayList<>();
        int totalAmount = 0;
        for (CreateOrderRequest.Item i : req.getItems()) {
            ProductSkuVO sku = skuMap.get(i.getSkuId());
            OrderItem item = new OrderItem();
            item.setSpuId(sku.getSpuId());
            item.setSkuId(sku.getId());
            item.setSkuName(sku.getSpuName());   // 快照:拷贝此刻的商品名
            item.setSpec(sku.getSpec());
            item.setImage(sku.getImage());
            item.setPrice(sku.getPrice());
            item.setQuantity(i.getQuantity());
            item.setTotalAmount(sku.getPrice() * i.getQuantity());
            items.add(item);
            totalAmount += item.getTotalAmount();
        }

        // 3. 落订单:三张表在独立事务里原子写入并提交
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES);
        Long orderId = saveOrder(userId, req, items, totalAmount, expireTime);

        // 4. 锁库存(跨服务)。失败 → 订单已落库,不一致就此产生(演示点)
        Result<Void> lockResult = inventoryClient.lock(toLockRequest(orderId, req));
        if (lockResult.getCode() != Result.SUCCESS_CODE) {
            log.warn("锁库存失败(故意不补偿,留脏数据演示), orderId={}, code={}, msg={}",
                    orderId, lockResult.getCode(), lockResult.getMessage());
            // 原样抛给全局异常处理器;第⑥步 Seata 就是在这里触发全局回滚
            throw new BusinessException(lockResult.getCode(), lockResult.getMessage());
        }

        // 5. 响应
        OrderVO vo = new OrderVO();
        vo.setOrderId(orderId);
        vo.setPayAmount(totalAmount);
        vo.setExpireTime(expireTime);
        return vo;
    }

    /** 拉 SKU 并校验:商品服务没返回 = 不存在/下架,不许下单 */
    private Map<Long, ProductSkuVO> loadAndCheckSkus(CreateOrderRequest req) {
        List<Long> skuIds = req.getItems().stream()
                .map(CreateOrderRequest.Item::getSkuId)
                .distinct()
                .toList();
        Result<List<ProductSkuVO>> resp = productClient.listSkuByIds(skuIds);
        // Feign 不经过网关/axios,Result 自己判断 code(购物车同款坑)
        if (resp.getCode() != Result.SUCCESS_CODE) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        List<ProductSkuVO> skus = resp.getData();
        if (skus == null || skus.size() != skuIds.size()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_INVALID);
        }
        return skus.stream().collect(Collectors.toMap(ProductSkuVO::getId, s -> s));
    }

    /** 落订单 + 明细 + 状态日志,一个独立事务(编程式,为什么见"关键点拆解") */
    private Long saveOrder(Long userId, CreateOrderRequest req, List<OrderItem> items,
                           int totalAmount, LocalDateTime expireTime) {
        return transactionTemplate.execute(status -> {
            Order order = new Order();
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(0);
            order.setPayAmount(totalAmount);    // 暂无优惠:实付 = 总额
            order.setStatus(STATUS_PENDING);
            order.setAddressSnapshot(toJson(req.getAddress()));
            order.setRemark(req.getRemark());
            order.setExpireTime(expireTime);
            orderMapper.insert(order);

            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }

            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(order.getId());
            orderLog.setFromStatus(null);       // 诞生,不是流转
            orderLog.setToStatus(STATUS_PENDING);
            orderLog.setOperator(OPERATOR_SYSTEM);
            orderLog.setRemark("创建订单");
            orderLogMapper.insert(orderLog);

            return order.getId();
        });
    }

    /** 组装库存锁定请求 */
    private LockRequest toLockRequest(Long orderId, CreateOrderRequest req) {
        LockRequest lock = new LockRequest();
        lock.setOrderId(orderId);
        lock.setItems(req.getItems().stream().map(i -> {
            LockRequest.Item li = new LockRequest.Item();
            li.setSkuId(i.getSkuId());
            li.setQuantity(i.getQuantity());
            return li;
        }).toList());
        return lock;
    }

    /** 对象 → JSON 字符串(存进 address_snapshot 的 JSON 列) */
    private String toJson(Object obj) {
        try {
            return obj == null ? null : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
