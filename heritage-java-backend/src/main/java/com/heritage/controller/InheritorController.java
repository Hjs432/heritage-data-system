package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.entity.Inheritor;
import com.heritage.service.InheritorService;
import com.heritage.vo.InheritorVO;
import com.heritage.vo.PageVO;
import com.heritage.vo.ProjectVO;
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

import java.util.List;

/**
 * 传承人 API
 */
@RestController
@RequestMapping("/api/inheritor")
@RequiredArgsConstructor
public class InheritorController {

    private final InheritorService inheritorService;

    /** 分页查询 */
    @GetMapping
    public Result<PageVO<InheritorVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inheritorService.page(pageNum, pageSize));
    }

    /** 单条详情 */
    @GetMapping("/{id}")
    public Result<InheritorVO> detail(@PathVariable String id) {
        InheritorVO vo = inheritorService.detail(id);
        return vo == null ? Result.fail(404, "传承人不存在") : Result.success(vo);
    }

    /** 该传承人关联的非遗项目列表 */
    @GetMapping("/{id}/projects")
    public Result<List<ProjectVO>> projects(@PathVariable String id) {
        return Result.success(inheritorService.projects(id));
    }

    /** 新增（admin） */
    @PostMapping
    public Result<InheritorVO> save(@Valid @RequestBody Inheritor inheritor) {
        return Result.success("新增成功", inheritorService.save(inheritor));
    }

    /** 修改（admin） */
    @PutMapping("/{id}")
    public Result<InheritorVO> update(@PathVariable String id, @Valid @RequestBody Inheritor inheritor) {
        return Result.success("修改成功", inheritorService.update(id, inheritor));
    }

    /** 删除（admin） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        inheritorService.delete(id);
        return Result.success("删除成功", null);
    }
}