package com.insight.product.service;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.common.mq.ProductChangeMessage;
import com.insight.common.result.PageResult;
import com.insight.common.search.EsProductDoc;
import com.insight.product.dto.ProductDetailVO;
import com.insight.product.dto.ProductListItemVO;
import com.insight.product.dto.SkuVO;
import com.insight.product.entity.Sku;
import com.insight.product.entity.Spu;
import com.insight.product.mapper.SkuMapper;
import com.insight.product.mapper.SpuMapper;
import com.insight.product.mq.ProductChangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProductChangeProducer productChangeProducer;

    /** 按 id 查询商品详情(SPU + 该 SPU 下所有 SKU) */
    public ProductDetailVO detail(Long id) {
        log.info("查询商品详情, id={}", id);
        // 1. 查 SPU
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        // 2. 查该 SPU 下的所有 SKU(类型安全的条件构造器)
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id)
        );
        // 3. Entity 组装成 VO
        List<ProductDetailVO.SkuVO> skuVOs = skus.stream()
                .map(s -> new ProductDetailVO.SkuVO(
                        s.getId(), s.getSpec(), s.getPrice(), s.getOriginalPrice()))
                .collect(Collectors.toList());

        return new ProductDetailVO(
                spu.getId(), spu.getName(), spu.getMainImage(),
                spu.getDescription(), spu.getSales(),
                spu.getTag(), spu.getTagline(), spu.getSpecParams(), skuVOs);
    }

    /** 分页查询商品列表(默认按销量倒序,只查在售) */
    public PageResult<ProductListItemVO> page(int page, int size, String kw) {
        log.info("分页查询商品列表, page={}, size={}, kw={}", page, size, kw);
        // 1. 分页查 SPU:new Page<>(当前页, 每页条数),页码从 1 开始
        Page<Spu> pageResult = spuMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Spu>()
                        .eq(Spu::getStatus, 1)        // 只查在售
                        .like(StringUtils.hasText(kw), Spu::getName, kw)  // kw 非空才生效
                        .orderByDesc(Spu::getSales)   // 排序规则暂用销量倒序占位
        );
        List<Spu> spus = pageResult.getRecords();
        if (spus.isEmpty()) {
            return PageResult.of(pageResult.getTotal(), List.of());
        }

        // 2. 批量查 SKU 算最低价:一次 IN 查询,而不是循环查库(N+1 问题)
        List<Long> spuIds = spus.stream().map(Spu::getId).collect(Collectors.toList());
        Map<Long, Integer> minPriceMap = new HashMap<>();
        Map<Long, Long> minSkuIdMap = new HashMap<>();   // 新增:记住最低价是哪个 SKU
        skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .in(Sku::getSpuId, spuIds)
                        .eq(Sku::getStatus, 1)
        ).forEach(sku -> {
            Integer old = minPriceMap.get(sku.getSpuId());
            if (old == null || sku.getPrice() < old) {   // 更便宜 → 刷新最低价并记下它的 id
                minPriceMap.put(sku.getSpuId(), sku.getPrice());
                minSkuIdMap.put(sku.getSpuId(), sku.getId());
            }
        });

        // 3. Entity → VO
        List<ProductListItemVO> records = spus.stream()
                .map(s -> new ProductListItemVO(
                        s.getId(), s.getName(), s.getMainImage(),
                        s.getSales(), minPriceMap.getOrDefault(s.getId(), 0),
                        s.getTag(), s.getTagline(),
                        minSkuIdMap.get(s.getId())))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), records);
    }

    /** 批量查 SKU 摘要:供内部服务(购物车)组装展示用,两次 IN 查询避免 N+1 */
    public List<SkuVO> listSkuByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // 1. 一次查回所有 SKU(逻辑删除的自动被 @TableLogic 过滤)
        List<Sku> skus = skuMapper.selectBatchIds(ids);
        if (skus.isEmpty()) {
            return List.of();
        }
        // 2. 批量查 SPU 拿商品名——和分页列表算最低价一个手法,绝不循环查库
        Map<Long, String> spuNameMap = spuMapper.selectBatchIds(
                        skus.stream().map(Sku::getSpuId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Spu::getId, Spu::getName));
        // 3. 组装
        return skus.stream().map(s -> {
            SkuVO vo = new SkuVO();
            vo.setId(s.getId());
            vo.setSpuId(s.getSpuId());
            vo.setSpec(s.getSpec());
            vo.setPrice(s.getPrice());
            vo.setOriginalPrice(s.getOriginalPrice());
            vo.setImage(s.getImage());
            vo.setSpuName(spuNameMap.getOrDefault(s.getSpuId(), ""));
            return vo;
        }).toList();
    }

    /** 上下架:status=1 上架 / 0 下架。
     *  事务提交后才发 MQ——execute 返回时事务已提交,消费者拿到的消息
     *  一定对应"库里已生效的新值"(消息绝不会比数据快一步)。 */
    public void updateStatus(Long id, Integer status) {
        // 1. 事务内:校验存在 + 更新。返回更新后的 SPU;状态没变返回 null(幂等:不发无意义消息)
        Spu updated = transactionTemplate.execute(tx -> {
            Spu spu = spuMapper.selectById(id);
            if (spu == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (spu.getStatus().equals(status)) {
                return null;
            }
            Spu patch = new Spu();
            patch.setId(id);
            patch.setStatus(status);
            spuMapper.updateById(patch);
            spu.setStatus(status);
            return spu;
        });
        if (updated == null) {
            return;
        }
        // 2. 事务已提交,发消息:上架 → UPSERT 整条文档;下架 → DELETE 只带 id
        ProductChangeMessage msg = new ProductChangeMessage();
        msg.setSpuId(id);
        if (status == 1) {
            msg.setType(ProductChangeMessage.TYPE_UPSERT);
            msg.setDoc(buildDoc(updated));
        } else {
            msg.setType(ProductChangeMessage.TYPE_DELETE);
        }
        productChangeProducer.send(msg);
    }

    /** 组装 ES 文档:12 个字段与全量同步脚本一一对应;
     *  minPrice 口径 = MIN(在售 SKU 价格),无在售 SKU 时为 0(与全量脚本 LEFT JOIN 语义一致) */
    private EsProductDoc buildDoc(Spu spu) {
        Integer minPrice = skuMapper.selectList(
                        new LambdaQueryWrapper<Sku>()
                                .eq(Sku::getSpuId, spu.getId())
                                .eq(Sku::getStatus, 1))
                .stream()
                .map(Sku::getPrice)
                .min(Integer::compareTo)
                .orElse(0);
        EsProductDoc doc = new EsProductDoc();
        doc.setId(spu.getId());
        doc.setName(spu.getName());
        doc.setTagline(spu.getTagline());
        doc.setDescription(spu.getDescription());
        doc.setBrandId(spu.getBrandId());
        doc.setCategoryId(spu.getCategoryId());
        doc.setSales(spu.getSales());
        doc.setStatus(spu.getStatus());
        doc.setTag(spu.getTag());
        doc.setMainImage(spu.getMainImage());
        // ES mapping 里 createdAt 是 date 类型;格式与全量脚本写入的一致,避免两种格式混存
        doc.setCreatedAt(spu.getCreatedAt() == null ? null
                : spu.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        doc.setMinPrice(minPrice);
        return doc;
    }
}
