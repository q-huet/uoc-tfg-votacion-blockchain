# Configuración de Seguridad Spring Security con JWT

## 📋 Archivos Creados

### 1. JwtAuthenticationFilter.java
**Ubicación:** `src/main/java/es/tfg/votacion/config/JwtAuthenticationFilter.java`

**Propósito:** Filtro que intercepta todas las peticiones HTTP para validar automáticamente los tokens JWT.

**Funcionalidades:**
- Extrae el token JWT del header `Authorization: Bearer <token>`
- Valida el token usando `AuthService.validateJwtToken()`
- Configura el `SecurityContext` con el usuario autenticado
- Crea authorities basadas en el rol del usuario (`ROLE_VOTER`, `ROLE_ADMIN`, `ROLE_AUDITOR`)
- Permite que las peticiones sin token continúen (Spring Security decide si denegar acceso)

**Características clave:**
- Extiende `OncePerRequestFilter` (se ejecuta una vez por request)
- No lanza excepciones - permite que Spring Security maneje la autorización
- Logging detallado de eventos de autenticación
- Thread-safe

---

### 2. JwtAuthenticationEntryPoint.java
**Ubicación:** `src/main/java/es/tfg/votacion/config/JwtAuthenticationEntryPoint.java`

**Propósito:** Manejador de errores **401 Unauthorized** cuando un usuario intenta acceder a un recurso protegido sin estar autenticado.

**Respuesta JSON estandarizada:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Acceso no autorizado. Token JWT inválido o ausente.",
  "path": "/api/v1/elections/election-001/vote",
  "timestamp": "2025-11-11T00:55:49Z"
}
```

**Características:**
- Implementa `AuthenticationEntryPoint`
- Logging de intentos de acceso no autorizados
- Respuesta consistente con DTOs del sistema

---

### 3. JwtAccessDeniedHandler.java
**Ubicación:** `src/main/java/es/tfg/votacion/config/JwtAccessDeniedHandler.java`

**Propósito:** Manejador de errores **403 Forbidden** cuando un usuario autenticado intenta acceder a un recurso para el cual no tiene permisos (rol incorrecto).

**Respuesta JSON estandarizada:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Acceso denegado. No tiene permisos para acceder a este recurso.",
  "path": "/api/v1/elections/election-001/close",
  "timestamp": "2025-11-11T00:55:49Z"
}
```

**Características:**
- Implementa `AccessDeniedHandler`
- Diferencia claramente entre 401 (no autenticado) y 403 (sin permisos)
- Logging de intentos de acceso denegados

---

### 4. SecurityConfig.java (Actualizado)
**Ubicación:** `src/main/java/es/tfg/votacion/config/SecurityConfig.java`

**Propósito:** Configuración principal de Spring Security con JWT y control de acceso basado en roles (RBAC).

**Anotaciones habilitadas:**
- `@EnableWebSecurity` - Activa Spring Security
- `@EnableMethodSecurity(prePostEnabled = true)` - Permite usar `@PreAuthorize` en métodos

**Configuración de seguridad:**

#### Endpoints Públicos (sin autenticación requerida)
```java
.requestMatchers(
    "/api/v1/auth/login",           // Login
    "/actuator/health",             // Health check
    "/actuator/info"                // Info
).permitAll()

.requestMatchers(HttpMethod.GET, "/api/v1/elections").permitAll()  // Listar elecciones
```

#### Endpoints Autenticados (cualquier rol)
```java
.requestMatchers(
    "/api/v1/auth/validate",        // Validar token
    "/api/v1/auth/user",            // Info usuario
    "/api/v1/auth/logout"           // Logout
).authenticated()

.requestMatchers(HttpMethod.GET, "/api/v1/elections/*").authenticated()  // Detalle elección
```

#### Endpoints con Control de Acceso por Rol

**VOTER (Votante):**
```java
.requestMatchers(HttpMethod.POST, "/api/v1/elections/*/vote")
    .hasRole("VOTER")
```

**ADMIN (Administrador):**
```java
.requestMatchers(HttpMethod.POST, "/api/v1/elections/*/close")
    .hasRole("ADMIN")
```

**ADMIN y AUDITOR:**
```java
.requestMatchers(HttpMethod.GET, "/api/v1/elections/*/results")
    .hasAnyRole("ADMIN", "AUDITOR")
```

#### Configuración CORS
```java
.allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
.allowedHeaders("*")
.allowCredentials(true)
.maxAge(3600L)
```

#### Otras Configuraciones
- **CSRF:** Deshabilitado (no necesario para API REST stateless con JWT)
- **Sesiones:** STATELESS (no se crean sesiones, solo JWT)
- **Filtro JWT:** Añadido antes de `UsernamePasswordAuthenticationFilter`

---

## 🔐 Flujo de Autenticación y Autorización

### 1. Usuario hace request
```
GET /api/v1/elections/election-001
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. JwtAuthenticationFilter intercepta
- Extrae token del header
- Valida con `AuthService.validateJwtToken()`
- Si válido: crea `UsernamePasswordAuthenticationToken` con authorities
- Configura `SecurityContextHolder`

### 3. Spring Security valida autorización
- Verifica que el endpoint requiere autenticación
- Verifica que el usuario tiene el rol necesario
- Si todo OK: permite el acceso
- Si no: lanza `AccessDeniedException` o `AuthenticationException`

### 4. Manejo de errores
- **Sin token o token inválido:** `JwtAuthenticationEntryPoint` → 401
- **Token válido pero rol incorrecto:** `JwtAccessDeniedHandler` → 403

### 5. Controller ejecuta
- Puede acceder al usuario autenticado con `@AuthenticationPrincipal`
- Ya no necesita validar el token manualmente

---

## 📝 Ejemplo de Uso en Controllers

### Antes (Manual)
```java
@PostMapping("/{id}/vote")
public ResponseEntity<?> emitVote(
        @RequestHeader("Authorization") String authHeader) {
    
    // Validación manual del token
    String token = extractToken(authHeader);
    User user = authService.validateJwtToken(token);
    
    if (user == null) {
        return ResponseEntity.status(401).body(new ErrorResponse(...));
    }
    
    // Validación manual del rol
    if (!user.role().equals(UserRole.VOTER)) {
        return ResponseEntity.status(403).body(new ErrorResponse(...));
    }
    
    // Lógica del endpoint...
}
```

### Después (Automático con Spring Security)
```java
@PostMapping("/{id}/vote")
public ResponseEntity<?> emitVote(
        @AuthenticationPrincipal User user) {
    
    // El usuario ya está autenticado y autorizado
    // Spring Security valida automáticamente el token y el rol
    
    // Lógica del endpoint directamente...
    String userId = user.id();
    String username = user.username();
    // ...
}
```

---

## ✅ Tests de Validación

**Ejecución:** `./mvnw test`

**Resultados:**
```
[INFO] Tests run: 55, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Tests que validan seguridad:**
- `AuthServiceTest` - 25 tests de autenticación JWT
- `ApplicationTests` - Context loading con SecurityConfig

**Logs de configuración:**
```
INFO es.tfg.votacion.config.SecurityConfig : Configuring Spring Security with JWT authentication
INFO es.tfg.votacion.config.SecurityConfig : Configuring CORS for development environment
INFO es.tfg.votacion.config.SecurityConfig : Spring Security configured successfully
```

---

## 🎯 Próximos Pasos

### Recomendado: Tests de Controllers con MockMvc
Ahora que Spring Security está configurado, crear tests de controllers que validen:

1. **AuthControllerTest**
   - Login exitoso retorna 200 y token JWT
   - Login con credenciales inválidas retorna 401
   - Validar token válido retorna 200
   - Validar token inválido retorna 401
   - Obtener usuario sin token retorna 401

2. **ElectionControllerTest**
   - Listar elecciones sin token retorna 200 (público)
   - Votar sin token retorna 401
   - Votar con token ADMIN retorna 403 (rol incorrecto)
   - Votar con token VOTER retorna 201
   - Cerrar elección sin token retorna 401
   - Cerrar elección con token VOTER retorna 403
   - Cerrar elección con token ADMIN retorna 200
   - Ver resultados sin token retorna 401
   - Ver resultados con token VOTER retorna 403
   - Ver resultados con token ADMIN retorna 200

---

## 🔒 Consideraciones de Seguridad

### Implementado ✅
- JWT firmado con HMAC-SHA256
- Tokens con expiración (1 hora)
- Control de acceso basado en roles (RBAC)
- CORS configurado para desarrollo
- Sesiones stateless (no sessions)
- Manejo estandarizado de errores 401/403

### Pendiente para Producción ⚠️
- [ ] JWT refresh tokens (renovación automática)
- [ ] Blacklist de tokens revocados (Redis)
- [ ] Rate limiting por IP/usuario
- [ ] HTTPS obligatorio
- [ ] Rotación de secret keys
- [ ] Auditoría de accesos
- [ ] Logging de seguridad a SIEM
- [ ] Headers de seguridad (Helmet)

---

## 📚 Referencias

- [Spring Security Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [OWASP API Security](https://owasp.org/www-project-api-security/)

---

**Autor:** Enrique Huet Adrover  
**Fecha:** 11 de noviembre de 2025  
**Versión:** 1.0
