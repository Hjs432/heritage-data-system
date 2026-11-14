package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.entity.DataSource;
import com.heritage.service.DataSourceService;
import com.heritage.vo.DataSourceVO;
import com.heritage.vo.PageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据来源 API
 */
@RestController
@RequestMapping("/api/source")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    /** 分页查询 */
    @GetMapping
    public Result<PageVO<DataSourceVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(dataSourceService.page(pageNum, pageSize));
    }

    /** 单条详情 */
    @GetMapping("/{id}")
    public Result<DataSourceVO> detail(@PathVariable String id) {
        DataSourceVO vo = dataSourceService.detail(id);
        return vo == null ? Result.fail(404, "数据来源不存在") : Result.success(vo);
    }

    /** 新增（admin） */
    @PostMapping
    public Result<DataSourceVO> save(@Valid @RequestBody DataSource dataSource) {
        return Result.success("新增成功", dataSourceService.save(dataSource));
    }

    /** 修改（admin） */
    @PutMapping("/{id}")
    public Result<DataSourceVO> update(@PathVariable String id, @Valid @RequestBody DataSource dataSource) {
        return Result.success("修改成功", dataSourceService.update(id, dataSource));
    }

    /** 删除（admin） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        dataSourceService.delete(id);
        return Result.success("删除成功", null);
    }
}