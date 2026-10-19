package com.insight.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.insight.common.result.PageResult;
import com.insight.common.result.Result;
import com.insight.search.dto.HotWordVO;
import com.insight.search.dto.SearchItemVO;
import com.insight.search.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final ElasticsearchClient esClient;

    private final SearchService searchService;

    @GetMapping
    public Result<PageResult<SearchItemVO>> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) @Positive Long categoryId,
            @RequestParam(value = "brandId", required = false) @Positive Long brandId,
            @RequestParam(value = "minPrice", required = false) @PositiveOrZero Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) @PositiveOrZero Integer maxPrice,
            @RequestParam(value = "sort", defaultValue = "default") String sort,
            @RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "页码必须从1开始") int page,
            @RequestParam(value = "size", defaultValue = "12")
            @Min(value = 1, message = "每页条数至少为1")
            @Max(value = 100, message = "每页条数不能超过100") int size) {
        return Result.success(searchService.search(keyword, categoryId, brandId,
                minPrice, maxPrice, sort, page, size));
    }

    /** 热门搜索词:对 tag 做词项聚合,按文档数取前 N */
    @GetMapping("/hot")
    public Result<List<HotWordVO>> hot(
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "至少返回1个")
            @Max(value = 50, message = "最多返回50个") int size) {
        return Result.success(searchService.hot(size));
    }

    /** 输入联想:对商品名做前缀查询,返回匹配的商品名列表 */
    @GetMapping("/suggest")
    public Result<List<String>> suggest(
            @RequestParam("prefix") @NotBlank(message = "联想词不能为空") String prefix) {
        return Result.success(searchService.suggest(prefix));
    }

    /** 探活:等价于 curl http://localhost:9200 —— 用最轻的调用验证整条链路 */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() throws IOException {
        String version = esClient.info().version().number();
        return Result.success(Map.of("esVersion", version));
    }
}
