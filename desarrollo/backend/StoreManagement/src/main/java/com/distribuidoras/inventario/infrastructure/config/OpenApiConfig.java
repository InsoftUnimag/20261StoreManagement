package com.distribuidoras.inventario.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI storeManagementApi() {
        return new OpenAPI()
                .info(buildInfo())
                .addServersItem(buildLocalServer())
                .addServersItem(buildProductionServer())
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
                .description("API REST para gestión de inventario, pedidos y abastecimiento.\n\n" +
                        "Sistema de gestión integral que incluye:\n" +
                        "- Gestión de productos y plantillas\n" +
                        "- Consulta de inventario y disponibilidad\n" +
                        "- Registro de pedidos y seguimiento\n" +
                        "- Operaciones de picking y despacho\n" +
                        "- Gestión de rutas y manifiestos\n" +
                        "- Control de excepciones de inventario")
                .version("v1.0.0")
                .license(new License()
                        .name("Apache License 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                );
    }

    private Server buildLocalServer() {
        return new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");
    }

    private Server buildProductionServer() {
        return new Server()
                .url("https://api.storemanagement.com")
                .description("Production Server");
    }
}
