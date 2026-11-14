package com.heritage.repository;

import com.heritage.entity.FolkActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 民俗活动 Repository（MongoDB）
 */
public interface FolkActivityRepository extends MongoRepository<FolkActivity, String> {
}