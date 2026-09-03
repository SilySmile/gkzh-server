package com.gkzh.app.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private StudentAuthInterceptor studentAuthInterceptor;
    @Autowired
    private StaffAuthInterceptor staffAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentAuthInterceptor)
                .addPathPatterns("/api/**") // 前台接口路径
                .excludePathPatterns("/api/student/checkin", "/api/common/**", "/api/student/login", "/api/student/register", "/api/staff/**"); // 登录接口不需要拦截
        registry.addInterceptor(staffAuthInterceptor)
                .addPathPatterns("/api/staff/**")
                .excludePathPatterns("/api/staff/login");
    }
}
