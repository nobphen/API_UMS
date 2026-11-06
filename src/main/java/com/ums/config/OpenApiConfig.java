package com.ums.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger / OpenAPI settings for the API docs page (/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    /** The API docs info (title, version) and a "bearer" JWT login, so Swagger can send the access token. */
    @Bean
    public OpenAPI umsOpenApi() {
        return new OpenAPI()
                .info(new Info().title("University Management System API").version("v1"))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
