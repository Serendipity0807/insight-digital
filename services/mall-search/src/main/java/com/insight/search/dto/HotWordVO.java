package com.insight.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 热门搜索词:词 + 出现次数 */
@Data
@AllArgsConstructor
public class HotWordVO {
    private String word;
    private long count;
}
