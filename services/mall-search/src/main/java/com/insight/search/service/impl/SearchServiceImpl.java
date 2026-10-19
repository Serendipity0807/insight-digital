package com.insight.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.common.result.PageResult;
import com.insight.search.dto.HotWordVO;
import com.insight.search.dto.SearchItemVO;
import com.insight.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    /** 只认别名:以后换索引只动别名指向,代码永远不改 */
    private static final String INDEX_ALIAS = "product_v1";

    private final ElasticsearchClient esClient;

    @Override
    public PageResult<SearchItemVO> search(String keyword, Long categoryId, Long brandId,
                                           Integer minPrice, Integer maxPrice,
                                           String sort, int page, int size) {
        SearchRequest request = SearchRequest.of(s -> {
            s.index(INDEX_ALIAS)
                    .from((page - 1) * size)
                    .size(size)
                    .source(src -> src.filter(f -> f.excludes("description")));   // ⑥ 省带宽

            // ② bool 开关盒:关键词进 must(打分),筛选条件进 filter(不打分)
            s.query(q -> q.bool(b -> {
                if (keyword != null && !keyword.isBlank()) {
                    b.must(m -> m.multiMatch(mm -> mm
                            .fields("name^3", "tagline^2", "description")
                            .query(keyword)));
                }
                if (categoryId != null) {                                   // ③ term 精确匹配
                    b.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                }
                if (brandId != null) {
                    b.filter(f -> f.term(t -> t.field("brandId").value(brandId)));
                }
                if (minPrice != null || maxPrice != null) {                 // ④ 价格区间
                    b.filter(f -> f.range(r -> {
                        r.field("minPrice");
                        if (minPrice != null) r.gte(JsonData.of(minPrice));
                        if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                        return r;
                    }));
                }
                return b;
            }));

            // ⑤ 排序:sort 是"联合类型",每个 sort 项必须填一种具体方式。
            //    "综合"的正确做法 = 不调用 .sort()(ES 默认按 _score 降序),
            //    而不是塞一个空的 sort(空 sort 在 build 时抛 MissingRequiredPropertyException)
            switch (sort == null ? "default" : sort) {
                case "sales"      -> s.sort(srt -> srt.field(f -> f.field("sales").order(SortOrder.Desc)));
                case "price_asc"  -> s.sort(srt -> srt.field(f -> f.field("minPrice").order(SortOrder.Asc)));
                case "price_desc" -> s.sort(srt -> srt.field(f -> f.field("minPrice").order(SortOrder.Desc)));
                case "newest"     -> s.sort(srt -> srt.field(f -> f.field("createdAt").order(SortOrder.Desc)));
                default           -> { /* 综合或未知值:不加 sort,靠 _score */ }
            }
            return s;
        });

        // ⑦ ES 挂了不让裸异常穿透:兜成业务错误码,前端才知道是搜索的问题
        SearchResponse<SearchItemVO> resp;
        try {
            resp = esClient.search(request, SearchItemVO.class);
        } catch (IOException e) {
            // 兜底之前先留证据:把真实异常记进日志,排查时看控制台第一行就知道原因
            log.error("搜索请求失败:{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SEARCH_TIMEOUT);
        }

        // 客户端把每个 hit 的 _source 自动反序列化成 SearchItemVO(驼峰直配)
        List<SearchItemVO> records = resp.hits().hits().stream()
                .map(h -> h.source())
                .toList();
        long total = resp.hits().total() != null ? resp.hits().total().value() : 0;
        return PageResult.of(total, records);
    }

    @Override
    public List<HotWordVO> hot(int size) {
        SearchResponse<SearchItemVO> resp;
        try {
            resp = esClient.search(req -> req
                            .index(INDEX_ALIAS)
                            .size(0)                                          // 只要聚合结果,不要文档
                            .aggregations("hot_tags", a -> a.terms(t -> t
                                    .field("tag")                             // tag 是 keyword,可聚合
                                    .size(size))),
                    SearchItemVO.class);
        } catch (IOException e) {
            log.error("热词聚合失败:{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SEARCH_TIMEOUT);
        }
        return resp.aggregations().get("hot_tags").sterms().buckets().array().stream()
                .map(b -> new HotWordVO(b.key().stringValue(), b.docCount()))
                .toList();
    }

    @Override
    public List<String> suggest(String prefix) {
        SearchResponse<SearchItemVO> resp;
        try {
            resp = esClient.search(req -> req
                            .index(INDEX_ALIAS)
                            .size(10)
                            .source(src -> src.filter(f -> f.includes("name")))   // 只要名字
                            .query(q -> q.prefix(p -> p.field("name").value(prefix))),
                    SearchItemVO.class);
        } catch (IOException e) {
            log.error("联想词查询失败:{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SEARCH_TIMEOUT);
        }
        return resp.hits().hits().stream()
                .map(h -> h.source() == null ? null : h.source().getName())
                .filter(Objects::nonNull)
                .toList();
    }
}
