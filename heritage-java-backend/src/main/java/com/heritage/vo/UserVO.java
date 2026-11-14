package com.heritage.vo;

import lombok.Data;

/**
 * 系统用户输出 VO（不包含密码字段）
 */
@Data
public class UserVO {

    private String id;
    private String userAccount;
    private String userRole;
    private String nickname;
}