package com.ecnu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类（适配 JJWT 0.12.6 + JDK9+）
 * 完全兼容原方法签名，业务代码无需修改
 */
public class JwtUtil {

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥【重要：必须至少32个字符（256位），否则会报错】
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return 生成的JWT令牌
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 将字符串密钥转换为JJWT要求的SecretKey对象（自动识别HS256算法）
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 生成过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 构建JWT
        return Jwts.builder()
                .claims(claims) // 旧版 setClaims() → 新版 claims()
                .signWith(key)  // 新版直接传SecretKey，无需指定算法
                .expiration(exp) // 旧版 setExpiration() → 新版 expiration()
                .compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去
     * @param token     加密后的token
     * @return 解析后的Claims载荷
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 同样转换密钥
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 解析JWT
        return Jwts.parser()
                .verifyWith(key) // 旧版 setSigningKey() → 新版 verifyWith()
                .build()
                .parseSignedClaims(token) // 旧版 parseClaimsJws() → 新版 parseSignedClaims()
                .getPayload(); // 旧版 getBody() → 新版 getPayload()
    }

}