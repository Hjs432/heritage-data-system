package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.entity.SysUser;
import com.heritage.service.UserService;
import com.heritage.vo.LoginVO;
import com.heritage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 API
 * 注册 / 登录 无需 token；用户列表仅 admin（由 SecurityConfig 后端拦截）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 注册用户（默认角色 user，密码 BCrypt 加密） */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody SysUser body) {
        return Result.success("注册成功",
                userService.register(body.getUserAccount(), body.getUserPwd(), body.getNickname()));
    }

    /** 登录：返回 JWT token 与用户信息 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody SysUser body) {
        return Result.success("登录成功", userService.login(body.getUserAccount(), body.getUserPwd()));
    }

    /** 获取全部用户（仅 admin） */
    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        return Result.success(userService.listAll());
    }
}