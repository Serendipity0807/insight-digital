package com.insight.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** 只有配置了 jwt.secret 的服务(mall-user / mall-gateway)才注册本 Bean;其他服务跳过 */
@Component
@ConditionalOnProperty(name = "jwt.secret")
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire:86400000}")
    private long expire;

    /** HS256 密钥:要求至少 32 字节(256 bit),否则 jjwt 会抛 WeakKeyException */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发:subject 存 userId,expire 控制有效期 */
    public String generate(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expire))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    /** 解析 + 验签:签名不对 / 已过期都会抛 JwtException(第④步网关捕获) */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
