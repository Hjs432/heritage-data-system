package com.heritage.vo;

import lombok.Data;

/**
 * 数据来源输出 VO
 */
@Data
public class DataSourceVO {

    private String id;
    private String sourceName;
    private String originalUrl;
    private String sourceNote;
}