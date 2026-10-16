package com.insight.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.inventory.dto.LockRequest;
import com.insight.inventory.entity.Inventory;
import com.insight.inventory.entity.InventoryLog;
import com.insight.inventory.mapper.InventoryLogMapper;
import com.insight.inventory.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InventoryService {

    /** 乐观锁冲突重试上限 */
    private static final int MAX_RETRY = 3;
    /** 流水类型:1 下单锁定 */
    private static final int CHANGE_TYPE_LOCK = 1;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    /**
     * 锁定库存:订单服务创建订单时调用,一个订单一次锁多件。
     * @Transactional:本服务的多行锁定要么全成、要么全滚(本地一致性);
     * 跨服务一致性是下一步 Seata 的事。
     */
    @Transactional
    public void lock(LockRequest req) {
        for (LockRequest.Item item : req.getItems()) {
            lockOne(req.getOrderId(), item.getSkuId(), item.getQuantity());
        }
    }

    /** 单件锁定:读版本号 → 条件更新 → 0 行重试(乐观锁 CAS) */
    private void lockOne(Long orderId, Long skuId, Integer quantity) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>().eq(Inventory::getSkuId, skuId));
            if (inv == null || inv.getSaleableStock() < quantity) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }

            // CAS:WHERE 带版本号。并发下只有第一个请求能成功,其余影响 0 行
            int rows = inventoryMapper.update(null,
                    new LambdaUpdateWrapper<Inventory>()
                            .set(Inventory::getSaleableStock, inv.getSaleableStock() - quantity)
                            .set(Inventory::getLockedStock, inv.getLockedStock() + quantity)
                            .set(Inventory::getVersion, inv.getVersion() + 1)
                            .eq(Inventory::getSkuId, skuId)
                            .eq(Inventory::getVersion, inv.getVersion()));
            if (rows == 1) {
                // 写流水:审计/对账依据
                InventoryLog logRow = new InventoryLog();
                logRow.setSkuId(skuId);
                logRow.setChangeType(CHANGE_TYPE_LOCK);
                logRow.setQuantity(-quantity);   // 可售库存减少,记负数
                logRow.setOrderId(orderId);
                inventoryLogMapper.insert(logRow);
                return;
            }
            log.warn("库存锁定版本冲突, skuId={}, 第 {} 次重试", skuId, attempt);
        }
        throw new BusinessException(ErrorCode.STOCK_LOCK_CONFLICT);
    }
}
