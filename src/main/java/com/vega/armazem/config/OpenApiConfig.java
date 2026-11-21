package com.vega.armazem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI armazemOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Armazém de Ingredientes")
                .version("1.0.0")
                .description("Estoque, movimentação e consulta de ingredientes nos compartimentos C1..C5."));
    }
}
