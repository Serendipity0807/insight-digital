package com.insight.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.product.dto.ProductDetailVO;
import com.insight.product.dto.SkuVO;
import com.insight.product.entity.Sku;
import com.insight.product.entity.Spu;
import com.insight.product.mapper.SkuMapper;
import com.insight.product.mapper.SpuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insight.common.result.PageResult;
import com.insight.product.dto.ProductListItemVO;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

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
    public PageResult<ProductListItemVO> page(int page, int size) {
        log.info("分页查询商品列表, page={}, size={}", page, size);
        // 1. 分页查 SPU:new Page<>(当前页, 每页条数),页码从 1 开始
        Page<Spu> pageResult = spuMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Spu>()
                        .eq(Spu::getStatus, 1)        // 只查在售
                        .orderByDesc(Spu::getSales)   // 排序规则暂用销量倒序占位
        );
        List<Spu> spus = pageResult.getRecords();
        if (spus.isEmpty()) {
            return PageResult.of(pageResult.getTotal(), List.of());
        }

        // 2. 批量查 SKU 算最低价:一次 IN 查询,而不是循环查库(N+1 问题)
        List<Long> spuIds = spus.stream().map(Spu::getId).collect(Collectors.toList());
        Map<Long, Integer> minPriceMap = new HashMap<>();
        skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .in(Sku::getSpuId, spuIds)
                        .eq(Sku::getStatus, 1)
        ).forEach(sku -> minPriceMap.merge(sku.getSpuId(), sku.getPrice(), Math::min));

        // 3. Entity → VO
        List<ProductListItemVO> records = spus.stream()
                .map(s -> new ProductListItemVO(
                        s.getId(), s.getName(), s.getMainImage(),
                        s.getSales(), minPriceMap.getOrDefault(s.getId(), 0),
                        s.getTag(), s.getTagline()))
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

}
