package com.heritage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统计结果 VO（供前端 ECharts 直接渲染）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatVO {

    /** 各地区非遗数量：[{name: "西藏拉萨", value: 3}, ...] */
    private List<Map<String, Object>> regionStat;

    /** 国家级 / 省级分布：[{name: "国家级", value: 4}, ...] */
    private List<Map<String, Object>> levelStat;

    /** 非遗总数 */
    private long total;
}