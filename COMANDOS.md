# 🚀 Guía de Comandos - Sistema de Votación Ford

## 📋 Levantar Servicios

### Opción 1: Terminales Separadas (Recomendado para desarrollo)

**Terminal 1 - Backend:**
```bash
cd ~/TFG/VotacionBC/backend-spring
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd ~/TFG/VotacionBC/frontend-angular
ng serve
```

### Opción 2: En Segundo Plano (Background)

```bash
# Levantar Backend
cd ~/TFG/VotacionBC/backend-spring && nohup ./mvnw spring-boot:run > /tmp/backend.log 2>&1 &

# Levantar Frontend
cd ~/TFG/VotacionBC/frontend-angular && nohup ng serve > /tmp/frontend.log 2>&1 &
```

**Ver logs en tiempo real:**
```bash
tail -f /tmp/backend.log   # Backend
tail -f /tmp/frontend.log  # Frontend
```

---

## 🛑 Detener Servicios

### Si están en terminales separadas:
- Presiona **Ctrl + C** en cada terminal

### Si están en segundo plano:

```bash
# Detener ambos servicios
pkill -f spring-boot:run && pkill -f "ng serve"

# Detener individualmente
pkill -f spring-boot:run  # Solo Backend
pkill -f "ng serve"        # Solo Frontend
```

### Forzar detención (si no responden):

```bash
pkill -9 -f spring-boot:run  # Backend
pkill -9 -f "ng serve"        # Frontend
```

---

## 🌐 URLs Útiles

### Backend (Spring Boot)
- **API Base:** http://localhost:8080/api/v1
- **Health Check:** http://localhost:8080/api/v1/actuator/health
- **Swagger UI:** http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api/v1/v3/api-docs

### Frontend (Angular)
- **Aplicación:** http://localhost:4200
- **Login:** http://localhost:4200/auth/login
- **Dashboard:** http://localhost:4200/dashboard

---

## 🔍 Verificar Estado

### Verificar puertos en uso:
```bash
lsof -i :8080  # Backend
lsof -i :4200  # Frontend
```

### Verificar procesos:
```bash
ps aux | grep spring-boot   # Backend
ps aux | grep "ng serve"    # Frontend
```

### Test rápido del backend:
```bash
curl http://localhost:8080/api/v1/actuator/health
```

---

## 🔑 Credenciales de Prueba

### Usuarios disponibles:
- **Admin:** `admin` / `password123`
- **Voter:** `test.user` / `password123`
- **Auditor:** `auditor` / `password123`

### Otros usuarios de prueba:
- `juan.perez` / `password123` (VOTER)
- `maria.gonzalez` / `password123` (VOTER)
- `carlos.martin` / `password123` (VOTER)
- `pedro.lopez` / `password123` (ADMIN)

---

## 📚 Usar Swagger UI

1. Accede a: http://localhost:8080/api/v1/swagger-ui.html
2. Haz login usando el endpoint `/auth/login`:
   ```json
   {
     "username": "admin",
     "password": "password123"
   }
   ```
3. Copia el **token** de la respuesta
4. Click en el botón **"Authorize" 🔒** (arriba a la derecha)
5. Pega el token (sin el prefijo "Bearer")
6. Click en **"Authorize"**
7. Ahora puedes probar todos los endpoints protegidos

---

## 🔧 Solución de Problemas

### Backend no arranca:
```bash
# Ver errores detallados
tail -f /tmp/backend.log

# Verificar que Java 21 está instalado
java -version

# Limpiar y recompilar
cd ~/TFG/VotacionBC/backend-spring
./mvnw clean package
```

### Frontend no arranca:
```bash
# Ver errores detallados
tail -f /tmp/frontend.log

# Verificar versiones
node -v   # Debe ser 20.x
npm -v    # Debe ser 10.x
ng version

# Reinstalar dependencias
cd ~/TFG/VotacionBC/frontend-angular
rm -rf node_modules package-lock.json
npm install
```

### Puerto ocupado:
```bash
# Liberar puerto 8080 (backend)
lsof -ti:8080 | xargs kill -9

# Liberar puerto 4200 (frontend)
lsof -ti:4200 | xargs kill -9
```

---

## 📦 Build de Producción

### Backend:
```bash
cd ~/TFG/VotacionBC/backend-spring
./mvnw clean package
# JAR generado en: target/votacion-0.0.1-SNAPSHOT.jar
```

### Frontend:
```bash
cd ~/TFG/VotacionBC/frontend-angular
ng build --configuration production
# Build generado en: dist/frontend-angular
```

---

## 🎯 Flujo de Trabajo Recomendado

1. **Iniciar sesión de desarrollo:**
   ```bash
   # Terminal 1
   cd ~/TFG/VotacionBC/backend-spring && ./mvnw spring-boot:run
   
   # Terminal 2
   cd ~/TFG/VotacionBC/frontend-angular && ng serve
   ```

2. **Abrir en el navegador:**
   - Frontend: http://localhost:4200
   - Swagger: http://localhost:8080/api/v1/swagger-ui.html

3. **Durante el desarrollo:**
   - Los cambios en Angular se recargan automáticamente (Hot Reload)
   - Los cambios en Spring Boot requieren reiniciar el servidor

4. **Al terminar:**
   - `Ctrl + C` en ambas terminales para detener los servicios
