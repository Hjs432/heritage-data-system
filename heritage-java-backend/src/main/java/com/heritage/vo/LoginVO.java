package com.heritage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回 VO：token + 用户信息（账号、昵称、角色）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** JWT token */
    private String token;
    /** 登录账号 */
    private String userAccount;
    /** 用户昵称 */
    private String nickname;
    /** 用户角色：admin / user */
    private String userRole;
}