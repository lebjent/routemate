package com.trip.routemate.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI routeMateOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RouteMate API")
                        .description("여행 일정, 여행지, 옵션상품 및 파트너사 관리 API")
                        .version("v1")
                        .contact(new Contact().name("RouteMate Team")))
                .components(new Components()
                        .addSecuritySchemes("sessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Spring Session cookie. 로그인 후 브라우저 세션이 자동으로 사용됩니다.")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public API")
                .pathsToMatch(
                        "/api/home/**",
                        "/api/destinations/**",
                        "/api/public/**",
                        "/api/lotto/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("User API")
                .pathsToMatch(
                        "/api/auth/**",
                        "/api/user/**",
                        "/api/my-travel-plans/**",
                        "/api/product-orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Admin API")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
