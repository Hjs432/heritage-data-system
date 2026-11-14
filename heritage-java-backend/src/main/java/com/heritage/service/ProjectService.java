package com.heritage.service;

import com.heritage.entity.DataSource;
import com.heritage.entity.FolkActivity;
import com.heritage.entity.Inheritor;
import com.heritage.entity.Project;
import com.heritage.repository.DataSourceRepository;
import com.heritage.repository.FolkActivityRepository;
import com.heritage.repository.InheritorRepository;
import com.heritage.repository.ProjectRepository;
import com.heritage.vo.DataSourceVO;
import com.heritage.vo.FolkActivityVO;
import com.heritage.vo.InheritorVO;
import com.heritage.vo.PageVO;
import com.heritage.vo.ProjectVO;
import com.heritage.vo.StatVO;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 非遗项目业务层
 * <p>
 * 1. 多条件分页查询使用 MongoTemplate
 * 2. 关联信息（传承人 / 民俗活动 / 数据源）通过 id 手动查询后拼装 VO，不做关系型 join
 * 3. 统计使用 MongoDB Aggregation 聚合管道
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final InheritorRepository inheritorRepository;
    private final FolkActivityRepository folkActivityRepository;
    private final DataSourceRepository dataSourceRepository;
    private final MongoTemplate mongoTemplate;
    private final SearchTagGenerator searchTagGenerator;

    /**
     * 多条件分页查询：项目名称（模糊）、非遗类别、地区
     */
    public PageVO<ProjectVO> page(String projectName, String projectCategory, String region,
                                  int pageNum, int pageSize) {
        Query query = new Query();
        List<Criteria> conditions = new ArrayList<>();
        if (StringUtils.hasText(projectName)) {
            // Pattern.quote 避免用户输入被当作正则元字符
            conditions.add(Criteria.where("project_name").regex(Pattern.quote(projectName.trim()), "i"));
        }
        if (StringUtils.hasText(projectCategory)) {
            conditions.add(Criteria.where("project_category").is(projectCategory));
        }
        if (StringUtils.hasText(region)) {
            conditions.add(Criteria.where("region").is(region));
        }
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Project.class);
        query.with(PageRequest.of(Math.max(pageNum - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "create_time")));
        List<Project> projects = mongoTemplate.find(query, Project.class);

        List<ProjectVO> list = projects.stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(total, pageNum, pageSize, list);
    }

    /** 查询单条详情（拼装关联信息） */
    public ProjectVO detail(String id) {
        return projectRepository.findById(id).map(this::toVO).orElse(null);
    }

    /** 新增非遗项目（admin）：自动生成通俗检索标签 */
    public ProjectVO save(Project project) {
        project.setId(null);
        if (project.getCreateTime() == null) {
            project.setCreateTime(LocalDateTime.now());
        }
        project.setSearchTags(searchTagGenerator.generate(project));
        return toVO(projectRepository.save(project));
    }

    /** 修改非遗项目（admin）：名称 / 类别 / 简介变化时重新生成标签，否则保留原标签 */
    public ProjectVO update(String id, Project project) {
        Project exist = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("非遗项目不存在：" + id));
        project.setId(exist.getId());
        // 前端未传录入时间时保留原值
        project.setCreateTime(project.getCreateTime() == null ? exist.getCreateTime() : project.getCreateTime());

        boolean contentChanged = !Objects.equals(exist.getProjectName(), project.getProjectName())
                || !Objects.equals(exist.getProjectCategory(), project.getProjectCategory())
                || !Objects.equals(exist.getProjectIntro(), project.getProjectIntro());
        boolean tagsMissing = exist.getSearchTags() == null || exist.getSearchTags().isEmpty();
        project.setSearchTags(contentChanged || tagsMissing
                ? searchTagGenerator.generate(project)
                : exist.getSearchTags());

        return toVO(projectRepository.save(project));
    }

    /** 删除非遗项目（admin） */
    public void delete(String id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("非遗项目不存在：" + id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * 统计分析：使用 MongoDB Aggregation 聚合
     * - 各地区非遗数量
     * - 国家级 / 省级分布
     */
    public StatVO stat() {
        return new StatVO(groupCount("region"), groupCount("project_level"), projectRepository.count());
    }

    /** 通用分组计数聚合 */
    private List<Map<String, Object>> groupCount(String field) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group(field).count().as("value"),
                Aggregation.sort(Sort.Direction.DESC, "value"));
        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, "project", Document.class);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Map<String, Object> item = new HashMap<>(4);
            Object id = doc.get("_id");
            item.put("name", id == null ? "未知" : id.toString());
            item.put("value", doc.get("value"));
            list.add(item);
        }
        return list;
    }

    // ==================== 实体 -> VO 转换 ====================

    /** Project -> ProjectVO，并手动拼装关联的传承人、民俗活动、数据源 */
    public ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);

        vo.setInheritorList(toInheritorVOList(project.getInheritorRefList()));
        vo.setActivityList(toActivityVOList(project.getActivityRefList()));

        if (StringUtils.hasText(project.getSourceRef())) {
            dataSourceRepository.findById(project.getSourceRef())
                    .ifPresent(source -> vo.setSource(toDataSourceVO(source)));
        }
        return vo;
    }

    private List<InheritorVO> toInheritorVOList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Inheritor> inheritors = inheritorRepository.findAllById(ids);
        return inheritors.stream().map(entity -> {
            InheritorVO vo = new InheritorVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<FolkActivityVO> toActivityVOList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<FolkActivity> activities = folkActivityRepository.findAllById(ids);
        return activities.stream().map(entity -> {
            FolkActivityVO vo = new FolkActivityVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private DataSourceVO toDataSourceVO(DataSource source) {
        DataSourceVO vo = new DataSourceVO();
        BeanUtils.copyProperties(source, vo);
        return vo;
    }
}