package com.quizcontest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Quiz Contest API
 * Provides comprehensive API documentation and interactive Swagger UI
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Contest API")
                        .version("1.0.0")
                        .description("Comprehensive REST API for multi-user quiz hosting platform with real-time scoring, " +
                                "image support, and leaderboard management. Supports quiz creation, question management, " +
                                "participant tracking, and automated scoring.")
                        .contact(new Contact()
                                .name("Quiz Contest Team")
                                .email("support@quizcontest.com")
                                .url("https://quizcontest.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.quizcontest.com")
                                .description("Production Server")
                ));
    }
}
