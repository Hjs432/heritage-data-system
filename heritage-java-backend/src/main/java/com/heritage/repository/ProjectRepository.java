package com.heritage.repository;

import com.heritage.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 非遗项目 Repository（MongoDB）
 * 复杂多条件分页查询与聚合统计由 Service 层使用 MongoTemplate 完成
 */
public interface ProjectRepository extends MongoRepository<Project, String> {

    /** 查询引用了指定传承人的全部项目（用于传承人详情弹窗） */
    List<Project> findByInheritorRefListContaining(String inheritorId);
}