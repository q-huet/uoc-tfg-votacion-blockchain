# Sistema de Votación Sindical basado en Blockchain (TFG)

Este proyecto implementa un sistema de votación electrónica seguro y transparente utilizando **Hyperledger Fabric** y **Spring Boot**.

## 📚 Documentación de Componentes

Cada parte del sistema tiene su propia documentación detallada:

*   [**Backend (Spring Boot)**](./backend-spring/README.md): Lógica de negocio, API REST y conexión con Fabric.
*   [**Frontend (Angular)**](./frontend-angular/README.md): Interfaz de usuario web.
*   [**Chaincode (Smart Contract)**](./chaincode/README.md): Lógica inmutable en la Blockchain.
*   [**Scripts**](./scripts/README.md): Herramientas de automatización y gestión.
*   [**Usuarios de Prueba**](./backend-spring/src/main/resources/mock/README.md): Lista de usuarios mock para pruebas.

---

## 🚀 Guía de Inicio Rápido

### 1. Requisitos Previos
Asegúrate de tener instalado en tu entorno Linux:
*   **Docker** y **Docker Compose**.
*   **Java 21** (JDK).
*   **Node.js** (v18+) y **NPM**.
*   **Curl** y **Git**.

### 2. Clonar y Preparar
```bash
git clone https://github.com/q-huet/uoc-tfg-votacion-blockchain.git
cd uoc-tfg-votacion-blockchain
chmod +x scripts/*.sh install-fabric.sh
```

### 3. Instalación de Binarios Fabric
Este proyecto requiere los binarios de Hyperledger Fabric que no se incluyen en el repositorio por su tamaño. Ejecuta el siguiente script para descargarlos:

```bash
./install-fabric.sh
```
> Este script descargará las imágenes Docker y los binarios necesarios (`peer`, `orderer`, etc.) y los colocará en la carpeta `fabric/`.

**(Opcional) Limpieza de ejemplos**:
El script de instalación descarga también ejemplos de Fabric que no son necesarios para este proyecto. Puedes eliminarlos automáticamente ejecutando:
```bash
./scripts/clean-fabric.sh
```

### 4. Primera Ejecución (Instalación Limpia)
Para levantar todo el entorno desde cero (Red Fabric + Chaincode + Backend + Frontend):

```bash
./scripts/start-all.sh
```
> ⚠️ **Atención**: Este comando **BORRA** cualquier dato previo en la red blockchain y en la base de datos local. Úsalo para la primera vez o cuando quieras resetear el entorno.

El sistema estará disponible en:
*   **Frontend**: http://localhost:4200
*   **Backend API**: http://localhost:8080/api/v1
*   **CouchDB (World State)**: http://localhost:5984/_utils

---

## 🔄 Flujo de Trabajo Diario (Persistencia)

Para evitar perder datos (usuarios, votos, elecciones) entre sesiones de desarrollo, utiliza los scripts de parada y reanudación "suave".

### 🛑 Detener el sistema (Pausa)
Cuando termines de trabajar, **NO** uses `start-all.sh` ni bajes la red manualmente. Usa:

```bash
./scripts/stop-soft.sh
```
Esto detendrá los contenedores Docker y matará los procesos de Java/Node, pero **mantendrá los datos** en los volúmenes de Docker y en la carpeta `backend-spring/data`.

### ▶️ Reanudar el sistema
Para continuar donde lo dejaste:

```bash
./scripts/resume-soft.sh
```
Esto reiniciará los contenedores existentes y volverá a levantar el Backend y el Frontend.

---

## 🛠️ Solución de Problemas Comunes

### Error: "Port 7984 already in use" al reanudar
Si `resume-soft.sh` falla porque el puerto de CouchDB está ocupado:
1.  Verifica qué proceso lo usa: `sudo lsof -i :7984`
2.  Si es un proceso "zombie" o residual, mátalo: `kill -9 <PID>`
3.  Vuelve a ejecutar `./scripts/resume-soft.sh`.

### Error: Wallet vacía o credenciales inválidas
Si el backend falla al conectar con Fabric:
1.  Asegúrate de que la red está corriendo (`docker ps`).
2.  Si la red se reinició con `start-all.sh`, la carpeta `wallet` se regenerará sola.
3.  Si persiste, considera hacer un reset completo con `./scripts/start-all.sh`.

---

## 👤 Autor
**Enrique Huet Adrover** - Trabajo Fin de Grado (UOC)
