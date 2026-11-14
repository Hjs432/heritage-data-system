package com.heritage.dto;

import lombok.Data;

/**
 * 模糊检索请求
 */
@Data
public class SearchRequest {

    /** 用户输入的口语化描述，如“一群人跳舞”“吃什么东西” */
    private String query;

    /** 返回条数上限 */
    private Integer topK = 10;

    /** 最低分数阈值 */
    private Double minScore = 0.15;
}
