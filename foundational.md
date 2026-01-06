# Proyecto: Sistema de Votación Sindical basado en Hyperledger Fabric

## Descripción general

Este proyecto implementa un **prototipo funcional (Proof of Concept)** de un sistema de votación electrónica para procesos sindicales dentro de un entorno industrial.

El objetivo principal es demostrar cómo una **blockchain permissioned**, como **Hyperledger Fabric**, puede proporcionar:

- **Transparencia** en el proceso electoral.
- **Inmutabilidad** de las transacciones.
- **Trazabilidad** verificable por todas las partes.
- **Seguridad** frente a manipulación o doble voto.

La PoC está orientada a un Trabajo Final de Grado, priorizando una arquitectura clara, demostrable y con evidencias verificables.

## Arquitectura Actual

El sistema se compone de los siguientes módulos principales:

### • Backend (Spring Boot + Java 21)
- **Cliente Fabric Gateway**: Firma y envía transacciones a la red usando identidades X.509 (Patrón Gateway).
- **Gestión de Elecciones**: Orquesta la creación, votación y cierre de elecciones.
- **Persistencia Híbrida (Modelo Notario Digital)**:
  - **On-Chain (Blockchain)**: Commitments (SHA-256) de los votos y metadatos de auditoría. **No almacena datos cifrados**.
  - **Off-Chain (Local)**: Base de datos ligera (`elections-db.json`) y almacenamiento de BLOBs cifrados (`storage/`) para privacidad.
- **Seguridad**: 
  - Autenticación JWT para usuarios.
  - Cifrado AES-GCM para almacenamiento en disco.
  - **Verificación de Integridad**: Comprobación automática de `Hash(BLOB) == Commitment` durante el recuento.

### • Frontend (Angular 17)
- Interfaz de usuario moderna y responsiva (Angular Material).
- Funcionalidades:
  - Panel de votación.
  - Visualización de resultados en tiempo real (post-cierre).
  - Verificación de recibos de voto.

### • Chaincode (Java)
- **Smart Contract** desarrollado en Java.
- Define la lógica de negocio en la blockchain:
  - `InitLedger`: Inicialización.
  - `CreateElection`: Registro de nueva elección.
  - `EmitVote`: Registro inmutable del hash del voto.
  - `CloseElection`: Cierre oficial.

### • Red Blockchain (Hyperledger Fabric 2.5)
- Basada en la **Test Network** oficial (`fabric-samples`).
- **Nota de Implementación**: Se usa `test-network` para agilidad en la PoC. Un entorno de producción requeriría un despliegue personalizado de Fabric (Kubernetes/Cloud) con gestión de infraestructura separada por organización.
- **Topología**: 2 Organizaciones (Org1, Org2) + 1 Orderer (Raft).
- **Simulación de Roles**:
  - **Orderer Org**: Representa a la **Empresa** (Proveedor de Infraestructura).
  - **Org1**: Representa al **Sindicato A** (Validador).
  - **Org2**: Representa al **Sindicato B** (Validador).
- **Estado**: CouchDB como base de datos de estado (World State).

### • Scripts de Ciclo de Vida
- Automatización completa mediante Bash scripts:
  - `start-all.sh`: Despliegue desde cero (Hard Reset).
  - `stop-soft.sh` / `resume-soft.sh`: Persistencia del entorno de desarrollo.
  - `install-fabric.sh`: Gestión de binarios.

---

## Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.5.7**
- **Hyperledger Fabric Java Gateway SDK**
- **Spring Security + JWT**
- **AES-GCM** (Cifrado de votos)
- **Maven**

### Frontend
- **Angular 17**
- **TypeScript**
- **Angular Material**

### Blockchain
- **Hyperledger Fabric 2.5**
- **Chaincode en Java**
- **Docker & Docker Compose**
- **CouchDB**

### Entorno de Desarrollo
- **OS**: Linux (Ubuntu/WSL2)
- **IDE**: VS Code
- **Herramientas**: Docker, Git, Curl, JQ

---

## Propósito del PoC

La PoC tiene como objetivo demostrar:

1.  **Integridad**: Cómo garantizar que un voto no ha sido modificado mediante hashes en Blockchain.
2.  **Privacidad**: Cómo separar la identidad del votante del contenido del voto (Cifrado + Almacenamiento Off-chain).
3.  **Verificabilidad**: Capacidad del votante de comprobar que su voto fue contabilizado usando su recibo.
4.  **Integración**: Conexión fluida entre aplicaciones empresariales (Spring Boot) y DLTs (Fabric).

---

## Estado del Proyecto

- [x] **Red Fabric**: Operativa con Chaincode Java desplegado y políticas de aval configuradas.
- [x] **Backend**: API REST completa, seguridad JWT, conexión Gateway estable.
- [x] **Frontend**: Interfaz de votación y resultados implementada.
- [x] **Seguridad**: Implementada verificación de integridad criptográfica (Hash Check) en el recuento.
- [x] **Persistencia**: Sistema de parada y reanudación (Soft Stop/Resume) y limpieza de procesos zombie.
- [x] **Documentación**: Diseño de arquitectura, seguridad y backend actualizados.
- [x] **Limpieza**: Scripts de mantenimiento y estructura de proyecto optimizada.

---

**Autor:** Enrique Huet Adrover
**TFG - UOC**
