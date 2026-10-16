package com.insight.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.cart.dto.CartItem;
import com.insight.cart.dto.CartItemVO;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CartService {

    private static final String KEY_PREFIX = "cart:";
    /** 购物车 30 天过期;每次写入刷新——活跃的永不过期,僵尸的自动清理 */
    private static final long EXPIRE_DAYS = 30;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    /** 添加商品:已存在则数量累加 */
    public void add(Long userId, Long skuId, Integer quantity) {
        CartItem item = getItem(userId, skuId);
        if (item == null) {
            item = new CartItem();
            item.setQuantity(0);
            item.setChecked(true); // 新加的商品默认勾选
        }
        item.setQuantity(item.getQuantity() + quantity);

        redisTemplate.opsForHash().put(key(userId), String.valueOf(skuId), toJson(item));
        redisTemplate.expire(key(userId), EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /** 购物车列表(商品信息第③步用 Feign 组装) */
    public List<CartItemVO> list(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key(userId));
        List<CartItemVO> result = new ArrayList<>();
        entries.forEach((field, value) -> {
            CartItem item = fromJson((String) value);
            CartItemVO vo = new CartItemVO();
            vo.setSkuId(Long.valueOf((String) field));
            vo.setQuantity(item.getQuantity());
            vo.setChecked(item.getChecked());
            result.add(vo);
        });
        return result;
    }

    /** 修改数量 */
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        CartItem item = getOrThrow(userId, skuId);
        item.setQuantity(quantity);
        redisTemplate.opsForHash().put(key(userId), String.valueOf(skuId), toJson(item));
    }

    /** 勾选/取消勾选 */
    public void updateChecked(Long userId, Long skuId, Boolean checked) {
        CartItem item = getOrThrow(userId, skuId);
        item.setChecked(checked);
        redisTemplate.opsForHash().put(key(userId), String.valueOf(skuId), toJson(item));
    }

    /** 删除条目:不存在也返回成功(DELETE 幂等) */
    public void remove(Long userId, Long skuId) {
        String key = key(userId);
        redisTemplate.opsForHash().delete(key, String.valueOf(skuId));
        // Hash 空了顺手删 key,不留空壳
        if (redisTemplate.opsForHash().size(key) == 0) {
            redisTemplate.delete(key);
        }
    }

    // ---------- 私有工具 ----------

    private CartItem getItem(Long userId, Long skuId) {
        Object value = redisTemplate.opsForHash().get(key(userId), String.valueOf(skuId));
        return value == null ? null : fromJson((String) value);
    }

    private CartItem getOrThrow(Long userId, Long skuId) {
        CartItem item = getItem(userId, skuId);
        if (item == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return item;
    }

    private String toJson(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private CartItem fromJson(String json) {
        try {
            return objectMapper.readValue(json, CartItem.class);
        } catch (JsonProcessingException e) {
            log.error("购物车数据反序列化失败: {}", json, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
