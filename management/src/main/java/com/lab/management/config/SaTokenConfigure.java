package com.lab.management.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限配置类
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 路由拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {

                    // ---------- 【核心修改点】 ----------
                    // 如果是浏览器的 OPTIONS 预检请求，直接放行，不进行登录校验
                    // 否则跨域请求会因为无法通过预检而导致 Token 丢失
                    if (SaHolder.getRequest().getMethod().equals("OPTIONS")) {
                        return;
                    }
                    // ----------------------------------

                    // 检查登录状态
                    StpUtil.checkLogin();

                }))
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/api/hardware/upload",    // 硬件数据上传接口
                        "/auth/login",             // 登录接口
                        "/error",                  // 系统错误页面
                        "/favicon.ico",            // 浏览器图标
                        "/res/**",                 // 静态资源
                        "/static/**"               // 其他静态资源
                );
    }
}