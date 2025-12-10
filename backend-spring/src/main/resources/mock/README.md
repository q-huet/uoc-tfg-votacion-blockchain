# Mock Users Configuration

Este archivo contiene los usuarios mock para desarrollo y testing del sistema de votación.

## 📋 Usuarios Disponibles

| Username | Password | Rol | Departamento | Estado |
|----------|----------|-----|--------------|--------|
| juan.perez | password123 | VOTER | Producción | Activo |
| maria.gonzalez | password123 | VOTER | Administración | Activo |
| admin | password123 | ADMIN | RRHH | Activo |
| auditor | password123 | AUDITOR | Auditoría | Activo |
| carlos.martin | password123 | VOTER | Logística | Activo |
| ana.rodriguez | password123 | VOTER | Calidad | Activo |
| pedro.lopez | password123 | ADMIN | Dirección | Activo |
| lucia.garcia | password123 | VOTER | Producción | **Inactivo** |
| test.user | password123 | VOTER | Testing | Activo |
| voter10 | password123 | VOTER | Operaciones | Activo |
| voter11 | password123 | VOTER | Operaciones | Activo |
| voter12 | password123 | VOTER | Operaciones | Activo |
| voter13 | password123 | VOTER | Operaciones | Activo |
| voter14 | password123 | VOTER | Operaciones | Activo |
| voter15 | password123 | VOTER | Operaciones | Activo |
| voter16 | password123 | VOTER | Operaciones | Activo |
| voter17 | password123 | VOTER | Operaciones | Activo |
| voter18 | password123 | VOTER | Operaciones | Activo |
| voter19 | password123 | VOTER | Operaciones | Activo |

## 🔐 Información de Seguridad

**Contraseña por defecto:** `password123`

**Hash BCrypt:** `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

> ⚠️ **IMPORTANTE**: Estos usuarios son únicamente para desarrollo y testing. 
> En producción, se debe integrar con Azure EntraID o un sistema de autenticación real.

## 👥 Roles del Sistema

### VOTER (Votante)
- Puede participar en elecciones
- Puede emitir votos
- Puede verificar sus votos
- Acceso limitado solo a votación

### ADMIN (Administrador)
- Todos los permisos de VOTER
- Puede crear y gestionar elecciones
- Puede cerrar elecciones
- Puede ver resultados
- Acceso completo al sistema

### AUDITOR (Auditor)
- Puede ver todas las elecciones
- Puede ver resultados y auditorías
- Puede verificar transacciones blockchain
- **No puede votar** (para mantener imparcialidad)
- Acceso de solo lectura

## 📝 Estructura del JSON

```json
{
  "id": "Identificador único del usuario",
  "username": "Nombre de usuario (login)",
  "password": "Hash BCrypt de la contraseña",
  "email": "Correo electrónico corporativo",
  "fullName": "Nombre completo del empleado",
  "role": "VOTER | ADMIN | AUDITOR",
  "department": "Departamento de trabajo",
  "active": true/false,
  "lastLogin": "Último acceso (puede ser null)",
  "createdAt": "Fecha de creación del usuario"
}
```

## 🧪 Testing

Para tests automatizados, usar:
- **Usuario de prueba:** `test.user`
- **Usuario inactivo:** `lucia.garcia` (para testear validación de estado)

## 🔄 Generación de Hash BCrypt

Si necesitas generar un nuevo hash BCrypt para otra contraseña:

```bash
# Usando htpasswd (Apache)
htpasswd -bnBC 10 "" password123 | tr -d ':\n'

# Usando Python
python -c "import bcrypt; print(bcrypt.hashpw(b'password123', bcrypt.gensalt(rounds=10)).decode())"

# Online
https://bcrypt-generator.com/ (rounds: 10)
```

## 📚 Integración con Azure EntraID

En producción, este archivo no se usará. La integración con Azure EntraID incluirá:

1. OAuth 2.0 / OpenID Connect
2. Tokens JWT reales desde Azure
3. Sincronización de usuarios desde Active Directory
4. Multi-Factor Authentication (MFA)
5. Políticas de acceso condicional

---

**Versión:** 1.0  
**Última actualización:** 2025-11-10  
**Mantenedor:** Enrique Huet Adrover
