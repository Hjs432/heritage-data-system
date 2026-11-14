package com.heritage.service;

import com.heritage.entity.Inheritor;
import com.heritage.repository.InheritorRepository;
import com.heritage.repository.ProjectRepository;
import com.heritage.vo.InheritorVO;
import com.heritage.vo.PageVO;
import com.heritage.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 传承人业务层（独立文档，可被多个非遗项目引用）
 */
@Service
@RequiredArgsConstructor
public class InheritorService {

    private final InheritorRepository inheritorRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    /** 分页查询（全部用户可读） */
    public PageVO<InheritorVO> page(int pageNum, int pageSize) {
        var page = inheritorRepository.findAll(PageRequest.of(
                Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id")));
        List<InheritorVO> list = page.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(page.getTotalElements(), pageNum, pageSize, list);
    }

    /** 查询单条 */
    public InheritorVO detail(String id) {
        return inheritorRepository.findById(id).map(this::toVO).orElse(null);
    }

    /** 查询该传承人关联的全部非遗项目（用于传承人详情弹窗） */
    public List<ProjectVO> projects(String id) {
        if (!inheritorRepository.existsById(id)) {
            return Collections.emptyList();
        }
        return projectRepository.findByInheritorRefListContaining(id).stream()
                .map(projectService::toVO)
                .collect(Collectors.toList());
    }

    /** 新增（admin） */
    public InheritorVO save(Inheritor inheritor) {
        inheritor.setId(null);
        return toVO(inheritorRepository.save(inheritor));
    }

    /** 修改（admin） */
    public InheritorVO update(String id, Inheritor inheritor) {
        Inheritor exist = inheritorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("传承人不存在：" + id));
        inheritor.setId(exist.getId());
        return toVO(inheritorRepository.save(inheritor));
    }

    /** 删除（admin） */
    public void delete(String id) {
        if (!inheritorRepository.existsById(id)) {
            throw new IllegalArgumentException("传承人不存在：" + id);
        }
        inheritorRepository.deleteById(id);
    }

    private InheritorVO toVO(Inheritor entity) {
        InheritorVO vo = new InheritorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}