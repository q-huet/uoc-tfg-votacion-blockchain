package es.tfg.votacion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler para manejar errores de autorización (403 Forbidden)
 * 
 * Se invoca cuando un usuario autenticado intenta acceder a un recurso
 * para el cual no tiene los permisos necesarios (rol incorrecto).
 * 
 * Retorna una respuesta JSON estandarizada con el error.
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        logger.error("Access denied to: {} - {}",
                request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.put("message", "Acceso denegado. No tiene permisos para acceder a este recurso.");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
