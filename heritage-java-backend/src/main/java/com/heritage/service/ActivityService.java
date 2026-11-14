package com.heritage.service;

import com.heritage.entity.FolkActivity;
import com.heritage.repository.FolkActivityRepository;
import com.heritage.vo.FolkActivityVO;
import com.heritage.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 民俗活动业务层（独立文档，多个非遗项目可引用同一活动）
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final FolkActivityRepository folkActivityRepository;

    /** 分页查询（全部用户可读） */
    public PageVO<FolkActivityVO> page(int pageNum, int pageSize) {
        var page = folkActivityRepository.findAll(PageRequest.of(
                Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id")));
        List<FolkActivityVO> list = page.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(page.getTotalElements(), pageNum, pageSize, list);
    }

    /** 查询单条 */
    public FolkActivityVO detail(String id) {
        return folkActivityRepository.findById(id).map(this::toVO).orElse(null);
    }

    /** 新增（admin） */
    public FolkActivityVO save(FolkActivity activity) {
        activity.setId(null);
        return toVO(folkActivityRepository.save(activity));
    }

    /** 修改（admin） */
    public FolkActivityVO update(String id, FolkActivity activity) {
        FolkActivity exist = folkActivityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("民俗活动不存在：" + id));
        activity.setId(exist.getId());
        return toVO(folkActivityRepository.save(activity));
    }

    /** 删除（admin） */
    public void delete(String id) {
        if (!folkActivityRepository.existsById(id)) {
            throw new IllegalArgumentException("民俗活动不存在：" + id);
        }
        folkActivityRepository.deleteById(id);
    }

    private FolkActivityVO toVO(FolkActivity entity) {
        FolkActivityVO vo = new FolkActivityVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}