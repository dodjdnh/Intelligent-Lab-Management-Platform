package com.lab.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径的跨域请求
        registry.addMapping("/**")
                // 允许所有来源（使用 allowedOriginPatterns 配合 allowCredentials）
                .allowedOriginPatterns("*")
                // 允许的提交方式
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 【核心修改点 1】允许的请求头：必须允许前端 request.js 中设置的 "satoken"
                .allowedHeaders("*")
                // 【核心修改点 2】暴露给前端的响应头：允许前端 JS 获取该响应头
                .exposedHeaders("satoken")
                // 是否允许发送 Cookie 或 认证信息
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒（1小时内不再发送 OPTIONS 请求）
                .maxAge(3600);
    }
}