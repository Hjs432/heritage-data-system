package com.heritage.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

/**
 * JWT 工具类：负责 token 的生成与解析
 */
@Component
public class JwtUtil {

    /** 签名密钥（来自 application.yml） */
    @Value("${jwt.secret}")
    private String secret;

    /** token 有效期（毫秒） */
    @Value("${jwt.expire-time}")
    private Long expireTime;

    /**
     * 由配置的 secret 做 SHA-256 派生，得到 256bit 密钥，
     * 保证满足 HS256 算法对密钥长度的要求（配置明文可保持简短）
     */
    private SecretKey getSecretKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new IllegalStateException("生成 JWT 密钥失败", e);
        }
    }

    /**
     * 生成 token
     *
     * @param account 登录账号（作为 subject）
     * @param role    角色：admin / user
     */
    public String createToken(String account, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setSubject(account)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 token，返回 Claims（解析失败会抛出异常） */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}