package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.entity.FolkActivity;
import com.heritage.service.ActivityService;
import com.heritage.vo.FolkActivityVO;
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
 * 民俗活动 API
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /** 分页查询 */
    @GetMapping
    public Result<PageVO<FolkActivityVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(activityService.page(pageNum, pageSize));
    }

    /** 单条详情 */
    @GetMapping("/{id}")
    public Result<FolkActivityVO> detail(@PathVariable String id) {
        FolkActivityVO vo = activityService.detail(id);
        return vo == null ? Result.fail(404, "民俗活动不存在") : Result.success(vo);
    }

    /** 新增（admin） */
    @PostMapping
    public Result<FolkActivityVO> save(@Valid @RequestBody FolkActivity activity) {
        return Result.success("新增成功", activityService.save(activity));
    }

    /** 修改（admin） */
    @PutMapping("/{id}")
    public Result<FolkActivityVO> update(@PathVariable String id, @Valid @RequestBody FolkActivity activity) {
        return Result.success("修改成功", activityService.update(id, activity));
    }

    /** 删除（admin） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        activityService.delete(id);
        return Result.success("删除成功", null);
    }
}