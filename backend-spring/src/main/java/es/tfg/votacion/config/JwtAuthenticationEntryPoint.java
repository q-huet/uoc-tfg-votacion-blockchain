package es.tfg.votacion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point para manejar errores de autenticación (401 Unauthorized)
 * 
 * Se invoca cuando un usuario intenta acceder a un recurso protegido
 * sin estar autenticado o con credenciales inválidas.
 * 
 * Retorna una respuesta JSON estandarizada con el error.
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.error("Unauthorized access attempt to: {} - {}",
                request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.put("message", "Acceso no autorizado. Token JWT inválido o ausente.");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
