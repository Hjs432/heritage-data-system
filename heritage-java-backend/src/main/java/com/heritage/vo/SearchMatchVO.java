package com.heritage.vo;

import lombok.Data;

import java.util.List;

/**
 * 检索命中结果
 */
@Data
public class SearchMatchVO {

    /** 项目详情 */
    private ProjectVO project;

    /** 原始匹配分 */
    private Double score;

    /** 归一化权重（多个命中项之间的相对可能性） */
    private Double weight;

    /** 命中的通俗标签 */
    private List<String> matchedTags;

    /** 匹配理由，便于结果解释 */
    private List<String> reasons;
}
