package es.tfg.votacion.config;

import es.tfg.votacion.model.User;
import es.tfg.votacion.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT para autenticación automática en Spring Security
 * 
 * Funcionalidades:
 * - Extracción automática del token JWT del header Authorization
 * - Validación del token con AuthService
 * - Configuración del SecurityContext con el usuario autenticado
 * - Logging de eventos de autenticación
 * 
 * El filtro se ejecuta una vez por request y valida el token JWT
 * antes de que la petición llegue a los controladores.
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public JwtAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && !jwt.isEmpty()) {
                User user = authService.validateJwtToken(jwt);

                if (user != null) {
                    // Crear autoridad basada en el rol del usuario
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.role().name())
                    );

                    // Crear token de autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Configurar el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("User '{}' authenticated successfully with role '{}'",
                            user.username(), user.role());
                } else {
                    logger.debug("Invalid JWT token for request to: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            // No lanzar excepción - permitir que el request continúe sin autenticación
            // Spring Security se encargará de denegar el acceso si es necesario
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     * 
     * @param request HTTP request
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
