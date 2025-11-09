package es.tfg.votacion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de seguridad para el PoC
 * 
 * Configuraciones:
 * - CORS para desarrollo con Angular
 * - Deshabilitación de CSRF (no necesario para PoC)
 * - Configuración básica de JWT mock
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Configuración de CORS para desarrollo
     * Permite conexiones desde el frontend Angular
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS for development environment");

        registry.addMapping("/api/v1/**")
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://127.0.0.1:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // TODO: Añadir configuración de JWT filter cuando se implemente AuthService
    // TODO: Configurar endpoints públicos vs protegidos
    // TODO: Añadir manejo de excepciones de seguridad
}