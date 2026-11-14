package com.heritage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB 配置
 * <p>
 * 连接串由 application.yml 中 spring.data.mongodb.uri 提供
 * （默认本地：mongodb://localhost:27017/heritage_db），
 * 此处显式声明 Repository 扫描路径；复杂查询与聚合统计由 MongoTemplate 完成。
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.heritage.repository")
public class MongoConfig {
}