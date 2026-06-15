package com.ecnu.config;

import com.ecnu.interceptor.JwtTokenInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    private JwtTokenInterceptor jwtTokenInterceptor;
    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login",
                        "/doctor/login",
                        "/doctor/register",
                        // --- Swagger / Knife4j 接口文档相关路径 ---
                        "/doc.html",                  // Knife4j 页面
                        "/swagger-ui.html",           // Swagger UI 页面 (YAML中配置的)
                        "/swagger-ui/**",             // Swagger UI 静态资源
                        "/v3/api-docs/**",            // OpenAPI 3.0 数据接口 (YAML中配置的)
                        "/webjars/**",                // UI 前端依赖的静态资源
                        "/swagger-resources/**",      // Swagger 资源
                        "/v2/api-docs/**",            // 兼容旧版 Swagger 2
                        "/favicon.ico",               // 网站图标
                        "/error");                    // 框架默认错误页面);
    }
}