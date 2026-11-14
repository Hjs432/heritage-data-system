package com.heritage.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 系统用户集合（集合名：sys_user）
 * 角色枚举：admin 管理员 / user 普通浏览用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sys_user")
public class SysUser {

    /** 用户ID */
    @Id
    private String id;

    /** 登录账号（唯一索引） */
    @NotBlank(message = "登录账号不能为空")
    @Indexed(unique = true)
    @Field("user_account")
    private String userAccount;

    /** BCrypt 加密密码，禁止明文存储 */
    @NotBlank(message = "密码不能为空")
    @Field("user_pwd")
    private String userPwd;

    /** 非空枚举：admin / user */
    @NotBlank(message = "用户角色不能为空")
    @Field("user_role")
    private String userRole;

    /** 用户昵称 */
    @Field("nickname")
    private String nickname;
}