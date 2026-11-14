package com.heritage.service;

import com.heritage.entity.SysUser;
import com.heritage.repository.SysUserRepository;
import com.heritage.security.JwtUtil;
import com.heritage.vo.LoginVO;
import com.heritage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务层：注册、登录（JWT）、用户列表
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /** 注册用户：密码 BCrypt 加密存储，默认角色 user */
    public UserVO register(String account, String pwd, String nickname) {
        if (!StringUtils.hasText(account) || !StringUtils.hasText(pwd)) {
            throw new IllegalArgumentException("账号和密码不能为空");
        }
        if (sysUserRepository.existsByUserAccount(account)) {
            throw new IllegalArgumentException("账号已存在：" + account);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(account);
        // BCrypt 加密，禁止明文存储
        sysUser.setUserPwd(passwordEncoder.encode(pwd));
        sysUser.setUserRole("user");
        sysUser.setNickname(StringUtils.hasText(nickname) ? nickname : account);
        return toVO(sysUserRepository.save(sysUser));
    }

    /** 登录：交给 Spring Security 校验密码，成功后签发 JWT */
    public LoginVO login(String account, String pwd) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account, pwd));
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        SysUser sysUser = sysUserRepository.findByUserAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));
        String token = jwtUtil.createToken(sysUser.getUserAccount(), sysUser.getUserRole());
        return new LoginVO(token, sysUser.getUserAccount(), sysUser.getNickname(), sysUser.getUserRole());
    }

    /** 全部用户（仅 admin 可访问，权限由 Security 拦截） */
    public List<UserVO> listAll() {
        return sysUserRepository.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    private UserVO toVO(SysUser entity) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}