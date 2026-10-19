package com.insight.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight.common.result.Result;
import com.insight.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** 白名单规则:HTTP 方法 + 路径前缀 双匹配,只放行"公开的只读操作"。
     *  写操作(POST/PUT/DELETE)一律不在白名单——哪怕路径前缀相同也要 token。
     *  例:GET /api/v1/product/1001 放行,PUT /api/v1/product/1001/status 拦截。
     *  record 是 Java 16+ 的轻量数据类:一行声明字段,自动生成构造器/equals/toString。 */
    private record WhiteRule(HttpMethod method, String pathPrefix) {}

    private static final List<WhiteRule> WHITE_LIST = List.of(
            new WhiteRule(HttpMethod.POST, "/api/v1/user/login"),
            new WhiteRule(HttpMethod.POST, "/api/v1/user/register"),
            new WhiteRule(HttpMethod.GET,  "/api/v1/user/ping"),
            new WhiteRule(HttpMethod.GET,  "/api/v1/product"),
            new WhiteRule(HttpMethod.GET,  "/api/v1/search")
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();
        // 1. 白名单直接放行(前缀匹配必须加 "/",否则 /api/v1/productXxx 会被误放行)
        if (isWhiteListed(method, path)) {
            return chain.filter(exchange);
        }

        // 2. 取 Authorization 头,必须是 "Bearer xxx" 格式
        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        // 3. 验签:篡改 / 过期 / 签名错误 → JwtException → 401
        try {
            Claims claims = jwtUtil.parse(auth.substring(7));
            Long userId = Long.valueOf(claims.getSubject());
            // 4. 透传 X-User-Id:用 set 覆盖,而不是 add 追加——防止客户端伪造身份头
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .headers(h -> h.set("X-User-Id", String.valueOf(userId)))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange);
        }
    }

    /** 方法与路径前缀同时匹配才算白名单命中 */
    private boolean isWhiteListed(HttpMethod method, String path) {
        for (WhiteRule rule : WHITE_LIST) {
            boolean methodMatch = rule.method().equals(method);
            boolean pathMatch = path.equals(rule.pathPrefix())
                    || path.startsWith(rule.pathPrefix() + "/");
            if (methodMatch && pathMatch) {
                return true;
            }
        }
        return false;
    }

    /** 401:返回统一 Result JSON(前端 axios 认 code/message 格式) */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Result.error(401, "未登录或登录已过期"));
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":401,\"message\":\"未登录或登录已过期\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 值越小越先执行,保证在任何路由/负载均衡过滤器之前
    }
}
