package com.distribuidoras.inventario.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${openapi.server-description:Local Development Server}")
    private String serverDescription;

    @Value("${openapi.prod-url:https://api.storemanagement.com}")
    private String prodUrl;

    @Value("${openapi.prod-description:Production Server}")
    private String prodDescription;

    @Bean
    public OpenAPI storeManagementApi() {
        return new OpenAPI()
                .info(buildInfo())
                .addServersItem(new Server().url(serverUrl).description(serverDescription))
                .addServersItem(new Server().url(prodUrl).description(prodDescription))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication Token")
                        )
                );
    }

    private Info buildInfo() {
        return new Info()
                .title("StoreManagement API")
                .description("API REST para gesti\u00f3n de inventario, pedidos y abastecimiento.\n\n" +
                        "Sistema de gesti\u00f3n integral que incluye:\n" +
                        "- Gesti\u00f3n de productos y plantillas\n" +
                        "- Consulta de inventario y disponibilidad\n" +
                        "- Registro de pedidos y seguimiento\n" +
                        "- Operaciones de picking y despacho\n" +
                        "- Gesti\u00f3n de rutas y manifiestos\n" +
                        "- Control de excepciones de inventario")
                .version("v1.0.0")
                .license(new License()
                        .name("Apache License 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                );
    }
}
