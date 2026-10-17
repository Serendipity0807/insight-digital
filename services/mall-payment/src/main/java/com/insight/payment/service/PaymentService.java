package com.insight.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.common.result.Result;
import com.insight.payment.client.InternalOrderVO;
import com.insight.payment.client.MarkPaidRequest;
import com.insight.payment.client.OrderClient;
import com.insight.payment.dto.CallbackRequest;
import com.insight.payment.dto.PayRequest;
import com.insight.payment.dto.PayVO;
import com.insight.payment.entity.Payment;
import com.insight.payment.mapper.PaymentMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;
    private static final int ORDER_STATUS_PENDING = 10;
    private static final String PAY_NO_PREFIX = "PAY";

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderClient orderClient;

    /** 发起支付:校验订单 → 生成支付单。重复发起返回同一支付单(幂等) */
    public PayVO createPayment(Long userId, PayRequest req) {
        // 1. 查订单(内部接口):不存在/不是本人的/不是待支付 → 10701(不泄露他人订单信息)
        Result<InternalOrderVO> resp = orderClient.getOrder(req.getOrderId());
        if (resp.getCode() != Result.SUCCESS_CODE || resp.getData() == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
        }
        InternalOrderVO order = resp.getData();
        if (!order.getUserId().equals(userId) || order.getStatus() != ORDER_STATUS_PENDING) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
        }

        // 2. 幂等:已有支付单直接返回(并发双点的最终防线是第 3 步的唯一索引)
        Payment exist = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, req.getOrderId()));
        if (exist != null) {
            return toPayVO(exist);
        }

        // 3. 生成支付单。payment_no = PAY+orderId:一单一支付单,单号天然唯一
        Payment p = new Payment();
        p.setPaymentNo(PAY_NO_PREFIX + order.getOrderId());
        p.setOrderId(order.getOrderId());
        p.setUserId(userId);
        p.setChannel(req.getChannel());
        p.setAmount(order.getPayAmount());   // 金额来自订单服务,不信任请求体
        p.setStatus(STATUS_PENDING);
        try {
            paymentMapper.insert(p);
        } catch (DuplicateKeyException e) {
            // 并发重复发起:唯一索引兜底,重查返回(注册接口同款)
            return toPayVO(paymentMapper.selectOne(
                    new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, req.getOrderId())));
        }
        log.info("创建支付单, paymentNo={}, orderId={}, amount={}", p.getPaymentNo(), p.getOrderId(), p.getAmount());
        return toPayVO(p);
    }

    /**
     * 渠道支付成功回调(mock)。全局事务入口(TM):支付单 → 订单 → 库存三库协同。
     * 幂等三件套:查重返回 + 状态机闸门(CAS 0→1)+ 唯一索引。
     */
    @GlobalTransactional
    public void handleCallback(CallbackRequest req) {
        // 1. 查支付单
        Payment p = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getPaymentNo, req.getPaymentNo()));
        if (p == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        // 2. 幂等闸门①:已成功 → 直接返回,绝不重复扣库存
        if (p.getStatus() == STATUS_SUCCESS) {
            log.info("重复回调,幂等返回, paymentNo={}", req.getPaymentNo());
            return;
        }
        // 3. 幂等闸门②:CAS 0→1。0 行 = 并发下另一个回调抢先了,幂等返回
        int rows = paymentMapper.update(null,
                new LambdaUpdateWrapper<Payment>()
                        .set(Payment::getStatus, STATUS_SUCCESS)
                        .set(Payment::getTradeNo, req.getTradeNo())
                        .set(Payment::getNotifyTime, LocalDateTime.now())
                        .eq(Payment::getId, p.getId())
                        .eq(Payment::getStatus, STATUS_PENDING));
        if (rows == 0) {
            log.info("并发回调,幂等返回, paymentNo={}", req.getPaymentNo());
            return;
        }

        // 4. 跨服务:订单 10→20(order 内部会连带扣库存)。
        //    失败 → 异常传播 → Seata 全局回滚,第 3 步的支付单更新被 undo
        Result<Void> resp = orderClient.markPaid(new MarkPaidRequest(p.getOrderId()));
        if (resp.getCode() != Result.SUCCESS_CODE) {
            throw new BusinessException(resp.getCode(), resp.getMessage());
        }
        log.info("支付回调处理完成, paymentNo={}, orderId={}", req.getPaymentNo(), p.getOrderId());
    }

    private PayVO toPayVO(Payment p) {
        PayVO vo = new PayVO();
        vo.setPaymentNo(p.getPaymentNo());
        vo.setAmount(p.getAmount());
        vo.setChannel(p.getChannel());
        return vo;
    }

    /**
     * 内部接口:关单时关闭支付单(由订单服务调用)。
     * 条件更新 WHERE status=0(待支付)→ 0 行 = 无支付单/已支付/已关闭,全部静默幂等。
     * 尤其"已支付":用户钱都付了,关单只是晚一步,支付单必须保持成功。
     */
    public void closeByOrderId(Long orderId) {
        paymentMapper.update(null,
                new LambdaUpdateWrapper<Payment>()
                        .set(Payment::getStatus, STATUS_FAILED)
                        .eq(Payment::getOrderId, orderId)
                        .eq(Payment::getStatus, STATUS_PENDING));
    }
}
