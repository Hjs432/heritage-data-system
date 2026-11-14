package com.heritage.repository;

import com.heritage.entity.SysUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 系统用户 Repository（MongoDB）
 */
public interface SysUserRepository extends MongoRepository<SysUser, String> {

    /** 根据登录账号查询用户（用于登录鉴权） */
    Optional<SysUser> findByUserAccount(String userAccount);

    /** 判断账号是否已存在（用于注册校验） */
    boolean existsByUserAccount(String userAccount);
}