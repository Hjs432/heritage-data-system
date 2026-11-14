package com.heritage.service;

import com.heritage.entity.DataSource;
import com.heritage.repository.DataSourceRepository;
import com.heritage.vo.DataSourceVO;
import com.heritage.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据来源业务层（独立文档，为非遗项目提供溯源来源）
 */
@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final DataSourceRepository dataSourceRepository;

    /** 分页查询（全部用户可读） */
    public PageVO<DataSourceVO> page(int pageNum, int pageSize) {
        var page = dataSourceRepository.findAll(PageRequest.of(
                Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id")));
        List<DataSourceVO> list = page.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(page.getTotalElements(), pageNum, pageSize, list);
    }

    /** 查询单条 */
    public DataSourceVO detail(String id) {
        return dataSourceRepository.findById(id).map(this::toVO).orElse(null);
    }

    /** 新增（admin） */
    public DataSourceVO save(DataSource dataSource) {
        dataSource.setId(null);
        return toVO(dataSourceRepository.save(dataSource));
    }

    /** 修改（admin） */
    public DataSourceVO update(String id, DataSource dataSource) {
        DataSource exist = dataSourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据来源不存在：" + id));
        dataSource.setId(exist.getId());
        return toVO(dataSourceRepository.save(dataSource));
    }

    /** 删除（admin） */
    public void delete(String id) {
        if (!dataSourceRepository.existsById(id)) {
            throw new IllegalArgumentException("数据来源不存在：" + id);
        }
        dataSourceRepository.deleteById(id);
    }

    private DataSourceVO toVO(DataSource entity) {
        DataSourceVO vo = new DataSourceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}