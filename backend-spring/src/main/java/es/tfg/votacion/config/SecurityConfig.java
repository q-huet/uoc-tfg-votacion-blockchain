package es.tfg.votacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad Spring Security con JWT
 * 
 * Configuraciones:
 * - CORS para desarrollo con Angular
 * - JWT authentication filter
 * - Endpoints públicos vs protegidos
 * - Control de acceso basado en roles (RBAC)
 * - Manejo de excepciones 401/403
 * - Sesiones stateless (no sessions)
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * Configuración principal de seguridad HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Spring Security with JWT authentication");

        http
                // Deshabilitar CSRF (no necesario para API REST stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar manejo de excepciones
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // Configurar sesiones como STATELESS (no sessions, solo JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Endpoints que permiten autenticación opcional
                        .requestMatchers(HttpMethod.GET, "/api/v1/elections").permitAll()

                        // Endpoints que requieren autenticación (cualquier rol)
                        .requestMatchers(
                                "/api/v1/auth/validate",
                                "/api/v1/auth/user",
                                "/api/v1/auth/logout"
                        ).authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/elections/*").authenticated()

                        // Endpoints para VOTER
                        .requestMatchers(HttpMethod.POST, "/api/v1/elections/*/vote")
                        .hasRole("VOTER")

                        // Endpoints para ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/elections/*/close")
                        .hasRole("ADMIN")

                        // Endpoints para ADMIN y AUDITOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/elections/*/results")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )

                // Añadir filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("Spring Security configured successfully");
        return http.build();
    }

    /**
     * Configuración de CORS para desarrollo
     * Permite conexiones desde el frontend Angular
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS for development environment");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);

        return source;
    }
}