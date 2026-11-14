package com.heritage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heritage.common.Result;
import com.heritage.common.ResultCode;
import com.heritage.security.JwtTokenFilter;
import com.heritage.security.UserDetailServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;

/**
 * Spring Security + JWT 安全配置
 * <p>
 * 权限规则（后端强制拦截，不能只依赖前端隐藏按钮）：
 * 1. /api/user/login、/api/user/register 无需 token；
 * 2. GET /api/user/list 仅 admin；
 * 3. 所有 POST / PUT / DELETE 接口必须 admin，普通 user 返回 403；
 * 4. 其余查询接口登录后即可访问（admin / user 均可）。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final UserDetailServiceImpl userDetailService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;

    /** 密码加密器：BCrypt */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 认证提供者：使用 sys_user 数据 + BCrypt 校验 */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** 认证管理器（登录时使用） */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离使用 JWT，关闭 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 启用 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录、注册放行
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        // 用户列表仅 admin
                        .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                        // 写操作（新增/修改/删除）必须 admin
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        // 其余查询接口登录即可
                        .anyRequest().authenticated())
                // 401 / 403 统一返回 Result JSON
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, ex) ->
                                writeJson(response, ResultCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) ->
                                writeJson(response, ResultCode.FORBIDDEN)))
                .authenticationProvider(authenticationProvider())
                // JWT 过滤器置于用户名密码过滤器之前
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 禁止 Spring Boot 把 JwtTokenFilter 额外注册为全局 Servlet 过滤器，避免重复执行 */
    @Bean
    public FilterRegistrationBean<JwtTokenFilter> jwtFilterRegistration(JwtTokenFilter filter) {
        FilterRegistrationBean<JwtTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /** 输出统一 JSON 响应 */
    private void writeJson(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(resultCode.getCode());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(resultCode.getCode(), resultCode.getMsg())));
    }
}