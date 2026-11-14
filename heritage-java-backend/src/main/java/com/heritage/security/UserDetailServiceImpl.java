package com.heritage.security;

import com.heritage.entity.SysUser;
import com.heritage.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 用户加载服务：按账号从 sys_user 集合读取用户
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final SysUserRepository sysUserRepository;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        SysUser sysUser = sysUserRepository.findByUserAccount(account)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + account));
        return User.builder()
                .username(sysUser.getUserAccount())
                .password(sysUser.getUserPwd())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + sysUser.getUserRole().toUpperCase())))
                .build();
    }
}