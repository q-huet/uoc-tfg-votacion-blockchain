# Configuración del Entorno de Desarrollo - TFG Votación Blockchain

Este documento describe cómo configurar el entorno de desarrollo del proyecto en cualquier PC.

## 📋 Requisitos Previos

### Software Necesario

| Componente | Versión Requerida | Verificación |
|-----------|------------------|--------------|
| Git | 2.x | `git --version` |
| Java (JDK) | 21.x | `java --version` |
| Maven | 3.9+ o 4.0+ | `mvn --version` |
| Node.js | 20.x LTS | `node --version` |
| npm | 10.x | `npm --version` |
| Angular CLI | 17.3.x | `ng version` |

### Clave SSH para GitHub

Necesitas una clave SSH configurada para acceder al repositorio.

## 🚀 Configuración Inicial

### 1. Verificar Entorno Actual

Ejecuta el script de verificación:

```bash
cd ~/TFG/VotacionBC
./check-environment.sh
```

Este script te mostrará qué componentes están instalados y cuáles faltan.

### 2. PC Corporativo (con Proxy Ford)

Si estás en un PC corporativo con proxy, necesitas configuraciones adicionales:

#### SSH Config
Archivo: `~/.ssh/config`
```ssh
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519
    ProxyCommand nc -X connect -x internet.ford.com:83 %h %p
```

#### Maven Settings
Archivo: `~/.m2/settings.xml`
```xml
<settings>
  <proxies>
    <proxy>
      <id>ford-http</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>internet.ford.com</host>
      <port>83</port>
    </proxy>
    <proxy>
      <id>ford-https</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>internet.ford.com</host>
      <port>83</port>
    </proxy>
  </proxies>
</settings>
```

#### NPM Config
```bash
npm config set proxy http://internet.ford.com:83
npm config set https-proxy http://internet.ford.com:83
```

#### APT Proxy (WSL/Ubuntu)
Archivo: `/etc/apt/apt.conf.d/95proxies`
```
Acquire::http::Proxy "http://internet.ford.com:83/";
Acquire::https::Proxy "http://internet.ford.com:83/";
```

### 3. PC Personal (sin Proxy)

En tu PC personal NO necesitas configuraciones de proxy. Simplemente:

1. Genera una clave SSH:
```bash
ssh-keygen -t ed25519 -C "tu_email@example.com"
```

2. Añádela a GitHub:
```bash
cat ~/.ssh/id_ed25519.pub
# Copia el contenido y añádelo en GitHub → Settings → SSH Keys
```

3. Clona el repositorio:
```bash
git clone git@github.com:q-huet/uoc-tfg-votacion-blockchain.git ~/TFG/VotacionBC
```

## 🔧 Instalación de Herramientas

### SDKMAN (Java + Maven)

```bash
# Instalar SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Instalar Java 21
sdk install java 21.0.5-tem

# Instalar Maven
sdk install maven 4.0.0-rc-5
```

### NVM (Node.js)

```bash
# Instalar NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc

# Instalar Node.js 20 LTS
nvm install 20
nvm use 20
nvm alias default 20
```

### Angular CLI

```bash
npm install -g @angular/cli@17.3.17
```

## 📦 Configuración del Proyecto

### 1. Clonar el Repositorio

```bash
git clone git@github.com:q-huet/uoc-tfg-votacion-blockchain.git ~/TFG/VotacionBC
cd ~/TFG/VotacionBC
```

### 2. Backend (Spring Boot)

```bash
cd backend-spring

# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Arrancar el servidor
./mvnw spring-boot:run
```

El backend estará disponible en: http://localhost:8080/api/v1

### 3. Frontend (Angular)

```bash
cd frontend-angular

# Instalar dependencias
npm install

# Arrancar servidor de desarrollo
ng serve
```

El frontend estará disponible en: http://localhost:4200

## 🔐 Credenciales de Prueba

El sistema incluye usuarios mock para desarrollo:

| Usuario | Password | Rol |
|---------|----------|-----|
| admin | password123 | ADMIN |
| test.user | password123 | VOTER |
| auditor | password123 | AUDITOR |
| juan.perez | password123 | VOTER |
| maria.gonzalez | password123 | VOTER |

## 🧪 Verificar que Todo Funciona

### Test Backend
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

Deberías recibir un JWT token.

### Test Frontend
Abre http://localhost:4200 y haz login con cualquier usuario de prueba.

## 📊 Estructura del Proyecto

```
VotacionBC/
├── backend-spring/          # API REST Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend-angular/        # Aplicación Angular
│   ├── src/
│   │   ├── app/
│   │   └── environments/
│   ├── package.json
│   └── angular.json
├── docs/                    # Documentación
├── chaincode/              # Chaincode Hyperledger Fabric
└── check-environment.sh    # Script de verificación
```

## 🐛 Troubleshooting

### Error de conexión a GitHub
```bash
# Verificar conexión SSH
ssh -T git@github.com

# Si falla, verifica tu clave SSH
ssh-add -l
```

### Backend no compila
```bash
# Limpiar y recompilar
cd backend-spring
./mvnw clean install -DskipTests
```

### Frontend no compila
```bash
# Limpiar caché npm y reinstalar
cd frontend-angular
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### Puerto 8080 o 4200 en uso
```bash
# Encontrar proceso usando el puerto
lsof -i :8080
lsof -i :4200

# Matar el proceso
kill -9 <PID>
```

## 📝 Notas Importantes

1. **Proxy**: Las configuraciones de proxy son SOLO para PC corporativo Ford. En tu PC personal, NO configures proxy.

2. **SSH Keys**: Cada PC necesita su propia clave SSH. No copies claves privadas entre PCs por seguridad.

3. **Node Modules**: NO subas `node_modules/` a Git. Siempre ejecuta `npm install` después de clonar.

4. **Maven Target**: NO subas `target/` a Git. Se genera automáticamente con Maven.

5. **Repositorio Privado**: Si haces el repo privado, asegúrate de que tu clave SSH esté añadida en GitHub.

## 🔄 Sincronización entre PCs

### Antes de cambiar de PC:
```bash
git add .
git commit -m "Descripción de cambios"
git push origin main
```

### Al empezar en otro PC:
```bash
cd ~/TFG/VotacionBC
git pull origin main
cd frontend-angular && npm install  # Si package.json cambió
cd ../backend-spring && ./mvnw clean compile  # Si pom.xml cambió
```

## 📚 Recursos

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Angular Docs](https://angular.io/docs)
- [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io/)
- [PrimeNG Components](https://primeng.org/)

---

**Última actualización**: 17 de noviembre de 2025
