package com.heritage.vo;

import lombok.Data;

import java.util.List;

/**
 * 意图归一化结果：把口语描述拆成关键词、类别、通俗标签
 */
@Data
public class SearchIntentVO {

    /** 原始查询 */
    private String query;

    /** 命中的关键词 */
    private List<String> keywords;

    /** 推断出的非遗类别 */
    private List<String> categories;

    /** 扩展出的通俗标签 */
    private List<String> tags;

    /** 识别出的场景修饰词（如“双人”“铠甲”），用于区分同类项目的细分形态 */
    private List<String> modifiers;

    /** 便于前端展示的解释 */
    private String summary;
}
