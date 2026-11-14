package com.heritage.repository;

import com.heritage.entity.DataSource;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 数据来源 Repository（MongoDB）
 */
public interface DataSourceRepository extends MongoRepository<DataSource, String> {
}