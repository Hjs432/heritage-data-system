package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.SearchRequest;
import com.heritage.entity.Project;
import com.heritage.service.ProjectSearchService;
import com.heritage.service.ProjectService;
import com.heritage.service.SearchIntentService;
import com.heritage.vo.PageVO;
import com.heritage.vo.ProjectVO;
import com.heritage.vo.SearchIntentVO;
import com.heritage.vo.SearchMatchVO;
import com.heritage.vo.StatVO;
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
 * 非遗项目 API
 * <p>
 * 查询接口：admin / user 均可访问（需登录）
 * 新增 / 修改 / 删除：仅 admin（由 SecurityConfig 后端拦截，普通用户 403）
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectSearchService projectSearchService;
    private final SearchIntentService searchIntentService;

    /** 分页多条件查询列表（返回已拼装关联信息的 VO） */
    @GetMapping
    public Result<PageVO<ProjectVO>> page(
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String projectCategory,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(projectService.page(projectName, projectCategory, region, pageNum, pageSize));
    }

    /** 口语化模糊检索，如“一群人跳舞”“吃什么东西”，返回 Top-K 与权重 */
    @PostMapping("/search")
    public Result<List<SearchMatchVO>> search(@RequestBody SearchRequest request) {
        return Result.success(projectSearchService.search(request));
    }

    /** 查看意图归一化结果（便于调试与前端展示） */
    @PostMapping("/search/intent")
    public Result<SearchIntentVO> searchIntent(@RequestBody SearchRequest request) {
        return Result.success(searchIntentService.parse(request.getQuery()));
    }

    /** 统计分析（MongoDB Aggregation）：按地区统计、按级别统计 */
    @GetMapping("/stat")
    public Result<StatVO> stat() {
        return Result.success(projectService.stat());
    }

    /** 单条详情，拼装关联的传承人、民俗活动、数据源 */
    @GetMapping("/{id}")
    public Result<ProjectVO> detail(@PathVariable String id) {
        ProjectVO vo = projectService.detail(id);
        return vo == null ? Result.fail(404, "非遗项目不存在") : Result.success(vo);
    }

    /** 新增项目（admin） */
    @PostMapping
    public Result<ProjectVO> save(@Valid @RequestBody Project project) {
        return Result.success("新增成功", projectService.save(project));
    }

    /** 修改项目（admin） */
    @PutMapping("/{id}")
    public Result<ProjectVO> update(@PathVariable String id, @Valid @RequestBody Project project) {
        return Result.success("修改成功", projectService.update(id, project));
    }

    /** 删除项目（admin） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return Result.success("删除成功", null);
    }
}