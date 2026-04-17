package com.distribuidoras.inventario.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI storeManagementApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StoreManagement API")
                        .description("API REST para gestión de inventario y abastecimiento.")
                        .version("v1.0.0"));
                        
    }
}
