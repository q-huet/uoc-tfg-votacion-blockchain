package es.tfg.votacion.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API REST
 * 
 * URLs de acceso:
 * - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/api/v1/v3/api-docs
 * 
 * @author TFG Votación Blockchain
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Votación Sindical Ford - API REST")
                        .description("API REST para el sistema de votación sindical basado en Blockchain (Hyperledger Fabric). " +
                                   "Proporciona endpoints para autenticación, gestión de elecciones, emisión de votos y auditoría.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TFG Votación Blockchain")
                                .email("admin@votacionford.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Servidor de Desarrollo Local")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido del endpoint /auth/login. " +
                                                   "Incluir en el header: Authorization: Bearer {token}")));
    }
}
