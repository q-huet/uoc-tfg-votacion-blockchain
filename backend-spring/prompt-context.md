# Contexto de desarrollo asistido (Copilot)

Este documento describe la estructura esperada del **backend Spring Boot** para el sistema de votación sindical basado en blockchain.  
Debe servir como guía contextual para que GitHub Copilot genere código consistente, modular y alineado con los objetivos del proyecto.

---

## 📦 Estructura de paquetes

```java
es.ford.tfg.votacion
├─ Application.java → Clase principal
├─ controller/
│ ├─ AuthController.java → Simula autenticación (mock EntraID)
│ ├─ ElectionController.java → Gestión de elecciones y votos
├─ service/
│ ├─ FabricService.java → Interfaz con Hyperledger Fabric
│ ├─ StorageService.java → Gestión de blobs cifrados (off-chain)
│ └─ AuthService.java → Validación básica de usuarios mock
├─ model/
│ ├─ VoteRequest.java → Representa la petición de voto
│ ├─ Receipt.java → Recibo de voto (commitment + txId)
│ ├─ User.java → Mock de identidad (id, nombre, rol)
│ └─ Election.java → Representa una elección
├─ config/
│ ├─ FabricConfig.java → Carga parámetros de conexión a Fabric
│ ├─ SecurityConfig.java → Configura CORS y mock JWT
│ └─ AppProperties.java → Enlaza propiedades YAML
```

---

## 🌐 Endpoints REST esperados

### 1. Autenticación (`AuthController`)

- `POST /auth/login`  
  → Entrada: `{ "username": "juan", "password": "1234" }`  
  → Salida: `{ "token": "MOCK-juan" }`

### 2. Elecciones (`ElectionController`)

- `GET /elections`  
  → Lista elecciones activas (mock).
- `POST /elections/{id}/vote`  
  → Entrada: `VoteRequest { electionId, voterId, commitment, voteBlobBase64 }`  
  → Salida: `Receipt { commitment, txId }`
- `POST /elections/{id}/close`  
  → Cierra la votación y devuelve resumen.
- `GET /elections/{id}/results`  
  → Devuelve resultados de recuento.

---

## 🧩 Servicios principales

### `FabricService`

- Gestiona conexión con **Hyperledger Fabric Gateway**.
- Métodos sugeridos:
  - `initGateway()` — inicializa conexión (lee wallet y perfil).
  - `emitVote(String commitment)` — invoca chaincode para registrar voto.
  - `closeElection()` — ejecuta método del chaincode.
  - `countVotes()` — devuelve resultados agregados.

> Copilot debe generar código con bloques `try-with-resources` para cerrar `Gateway` y `Network`.

### `StorageService`

- Gestiona almacenamiento de blobs cifrados fuera de blockchain.
- Métodos sugeridos:
  - `storeEncrypted(String electionId, byte[] data)` → guarda y devuelve ID.
  - `loadDecrypted(String id)` → descifra y devuelve bytes originales.
  - `delete(String id)` → elimina blob si es necesario.
- Usa AES-GCM con clave cargada desde `/resources/keystore/keystore.json`.

### `AuthService`

- Valida usuarios a partir de un JSON local (mock de EntraID).
- Genera tokens JWT simulados (sin firma real).
- Permite filtrar roles (`voter`, `admin`, `auditor`).

---

## 🔐 Configuración y seguridad

- CORS abierto para `http://localhost:4200`
- Deshabilitar CSRF (PoC)
- Autenticación simulada (JWT con prefijo `MOCK-`)
- En producción se integraría con Azure EntraID, pero aquí se usa mock JSON.

---

## ⚙️ Propiedades y entorno

Archivo `application.yml` esperado:

```yaml
server:
  port: 8080

fabric:
  connection_profile: /home/ehuetadr/fabric/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.json
  wallet_path: /home/ehuetadr/TFG/VotacionBC/backend-spring/wallet
  user: appUser
  channel: electionchannel
  chaincode: electioncc

storage:
  base_path: /home/ehuetadr/TFG/VotacionBC/backend-spring/data/storage
  keystore_path: /home/ehuetadr/TFG/VotacionBC/backend-spring/src/main/resources/keystore/keystore.json
```

---

## 🔧 Buenas prácticas sugeridas

- Usar **Java 21** (moderna, compatible con **Spring Boot 3.5.7**).
- Clases anotadas con:
  - `@RestController`
  - `@Service`
  - `@Configuration`
- Validar DTOs con:
  - `@Validated`
  - `@NotNull` _(JSR-380)_
- Retornar respuestas en formato **JSON** mediante `ResponseEntity<...>`.
- Centralizar las rutas REST bajo el prefijo:
  Centralizar rutas REST bajo /api/v1/....

- Registrar logs con `LoggerFactory` (**org.slf4j**).
- Añadir comentarios **Javadoc** breves en cada clase.

---

## 💡 Instrucciones para Copilot

- Generar código en **Java 21** con **estilo limpio**, clases bien separadas y nombres explícitos.
- El backend **no incluye base de datos relacional**:
- Los datos se simulan o almacenan temporalmente en **ficheros JSON** o **en memoria**.
- Toda la lógica relacionada con la **blockchain** debe encapsularse en `FabricService`.
- **Evitar dependencias innecesarias** — priorizar claridad y demostración funcional.
- **No generar configuraciones complejas**:
- Mantener un enfoque de **PoC ligera**.
- Priorizar:
- **Modularidad**
- **Claridad**
- **Trazabilidad del código**

---

📘 _Documento de apoyo para la generación de código automatizado y la organización del backend del proyecto TFG._
