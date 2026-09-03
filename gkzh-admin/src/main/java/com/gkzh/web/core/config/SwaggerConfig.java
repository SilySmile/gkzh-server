package com.gkzh.web.core.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gkzh.common.config.GkzhConfig;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger2的接口配置
 * 
 *
 */
@Configuration
public class SwaggerConfig
{
    /** 系统基础配置 */
    @Autowired
    private GkzhConfig gkzhConfig;

    /** 是否开启swagger */
    @Value("${swagger.enabled}")
    private boolean enabled;

    /** 设置请求的统一前缀 */
    @Value("${swagger.pathMapping}")
    private String pathMapping;

    /**
     * 创建API
     */
    /**
     * 后台管理API
     */
    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("后台管理API")  // 添加分组名称
                .enable(enabled)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 后台API路径模式，根据实际情况调整
                .paths(PathSelectors.ant("/admin/**"))
                .build()
                .securitySchemes(adminSecuritySchemes())  // 后台安全方案
                .securityContexts(adminSecurityContexts()) // 后台安全上下文
                .pathMapping(pathMapping);
    }

    /**
     * 前台用户API
     */
    @Bean
    public Docket frontApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("前台用户API")  // 添加分组名称
                .enable(enabled)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 前台API路径模式，根据实际情况调整
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .securitySchemes(frontSecuritySchemes())  // 前台安全方案
                .securityContexts(frontSecurityContexts()) // 前台安全上下文
                .pathMapping(pathMapping);
    }

    /**
     * 后台安全模式，使用Authorization头
     */
    private List<SecurityScheme> adminSecuritySchemes() {
        List<SecurityScheme> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", "header"));
        return apiKeyList;
    }

    /**
     * 前台安全模式，使用Front-Token头（可根据实际调整）
     */
    private List<SecurityScheme> frontSecuritySchemes() {
        List<SecurityScheme> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey("X-Front-Token", "X-Front-Token", "header"));
        return apiKeyList;
    }

    /**
     * 后台安全上下文
     */
    private List<SecurityContext> adminSecurityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(adminAuth())
                        .operationSelector(o -> o.requestMappingPattern().matches("/admin/.*"))
                        .build());
        return securityContexts;
    }

    /**
     * 前台安全上下文
     */
    private List<SecurityContext> frontSecurityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(frontAuth())
                        .operationSelector(o -> o.requestMappingPattern().matches("/api/.*"))
                        .build());
        return securityContexts;
    }

    /**
     * 后台默认的安全引用
     */
    private List<SecurityReference> adminAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    /**
     * 前台默认的安全引用
     */
    private List<SecurityReference> frontAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("X-Front-Token", authorizationScopes));
        return securityReferences;
    }

    /**
     * 添加摘要信息
     */
    private ApiInfo apiInfo()
    {
        // 用ApiInfoBuilder进行定制
        return new ApiInfoBuilder()
                // 设置标题
                .title("标题：国科智汇管理系统_接口文档")
                // 描述
                .description("描述：用于管理集团旗下公司的人员信息,具体包括XXX,XXX模块...")
                // 作者信息
                .contact(new Contact(gkzhConfig.getName(), null, null))
                // 版本
                .version("版本号:" + gkzhConfig.getVersion())
                .build();
    }
}
