package com.heritage.repository;

import com.heritage.entity.Inheritor;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 传承人 Repository（MongoDB）
 */
public interface InheritorRepository extends MongoRepository<Inheritor, String> {
}