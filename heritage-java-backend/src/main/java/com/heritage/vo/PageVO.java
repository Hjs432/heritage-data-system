package com.heritage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private long pageNum;
    /** 每页条数 */
    private long pageSize;
    /** 当前页数据 */
    private List<T> list;
}