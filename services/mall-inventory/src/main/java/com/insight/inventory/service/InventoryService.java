package com.insight.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.inventory.dto.DeductRequest;
import com.insight.inventory.dto.LockRequest;
import com.insight.inventory.dto.ReleaseRequest;
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
    /** 流水类型:2 支付扣减 */
    private static final int CHANGE_TYPE_DEDUCT = 2;
    /** 流水类型:3 取消释放 */
    private static final int CHANGE_TYPE_RELEASE = 3;

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

    /** 支付扣减:locked 减。乐观锁 CAS + 重试,和锁定一个套路 */
    @Transactional
    public void deduct(DeductRequest req) {
        for (DeductRequest.Item item : req.getItems()) {
            deductOne(req.getOrderId(), item.getSkuId(), item.getQuantity());
        }
    }

    private void deductOne(Long orderId, Long skuId, Integer quantity) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>().eq(Inventory::getSkuId, skuId));
            if (inv == null || inv.getLockedStock() < quantity) {
                throw new BusinessException(ErrorCode.STOCK_DEDUCT_CONFLICT);
            }
            int rows = inventoryMapper.update(null,
                    new LambdaUpdateWrapper<Inventory>()
                            .set(Inventory::getLockedStock, inv.getLockedStock() - quantity)
                            .set(Inventory::getVersion, inv.getVersion() + 1)
                            .eq(Inventory::getSkuId, skuId)
                            .eq(Inventory::getVersion, inv.getVersion()));
            if (rows == 1) {
                InventoryLog logRow = new InventoryLog();
                logRow.setSkuId(skuId);
                logRow.setChangeType(CHANGE_TYPE_DEDUCT);
                logRow.setQuantity(-quantity);   // 锁定库存减少,记负数
                logRow.setOrderId(orderId);
                inventoryLogMapper.insert(logRow);
                return;
            }
            log.warn("库存扣减版本冲突, skuId={}, 第 {} 次重试", skuId, attempt);
        }
        throw new BusinessException(ErrorCode.STOCK_DEDUCT_CONFLICT);
    }

    /** 取消释放:locked 回补 saleable。和锁定相反,乐观锁 CAS 同款 */
    @Transactional
    public void release(ReleaseRequest req) {
        for (ReleaseRequest.Item item : req.getItems()) {
            releaseOne(req.getOrderId(), item.getSkuId(), item.getQuantity());
        }
    }

    private void releaseOne(Long orderId, Long skuId, Integer quantity) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            Inventory inv = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>().eq(Inventory::getSkuId, skuId));
            if (inv == null || inv.getLockedStock() < quantity) {
                throw new BusinessException(ErrorCode.STOCK_RELEASE_CONFLICT);
            }
            int rows = inventoryMapper.update(null,
                    new LambdaUpdateWrapper<Inventory>()
                            .set(Inventory::getLockedStock, inv.getLockedStock() - quantity)
                            .set(Inventory::getSaleableStock, inv.getSaleableStock() + quantity)
                            .set(Inventory::getVersion, inv.getVersion() + 1)
                            .eq(Inventory::getSkuId, skuId)
                            .eq(Inventory::getVersion, inv.getVersion()));
            if (rows == 1) {
                InventoryLog logRow = new InventoryLog();
                logRow.setSkuId(skuId);
                logRow.setChangeType(CHANGE_TYPE_RELEASE);
                logRow.setQuantity(quantity);   // 可售库存回补,记正数——两层库存的最后一环闭环
                logRow.setOrderId(orderId);
                inventoryLogMapper.insert(logRow);
                return;
            }
            log.warn("库存释放版本冲突, skuId={}, 第 {} 次重试", skuId, attempt);
        }
        throw new BusinessException(ErrorCode.STOCK_RELEASE_CONFLICT);
    }
}
