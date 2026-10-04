package com.example.loginbase.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Metadados da documentação OpenAPI (Swagger UI): informações gerais e o
 * esquema de segurança "sessao" (cookie {@code JSESSIONID}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("login-base API")
                        .version("0.0.1-SNAPSHOT")
                        .description("Endpoints REST sob /api/**. Autenticação pela sessão (cookie JSESSIONID) "
                                + "obtida no formulário /login; requisições que alteram estado exigem o cabeçalho "
                                + "X-XSRF-TOKEN com o valor do cookie XSRF-TOKEN."))
                .components(new Components().addSecuritySchemes("sessao", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID")))
                .addSecurityItem(new SecurityRequirement().addList("sessao"));
    }
}
