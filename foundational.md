# Proyecto: Sistema de Votación Sindical basado en Hyperledger Fabric

## Descripción general

Este proyecto implementa un **prototipo funcional (Proof of Concept)** de un sistema de votación electrónica para procesos sindicales dentro de un entorno industrial (por ejemplo, una planta industrial como Ford España S.A).

El objetivo principal es demostrar cómo una **blockchain permissioned**, como **Hyperledger Fabric**, puede proporcionar:

- **Transparencia** en el proceso electoral  
- **Inmutabilidad** de las transacciones  
- **Trazabilidad** verificable por todas las partes  
- **Gobernanza distribuida** entre empresa y sindicatos  
- **Seguridad** frente a manipulación o doble voto  

La PoC está orientada a un Trabajo Final de Grado, por lo que se prioriza una arquitectura clara, demostrable y con evidencias verificables (transacciones en Fabric, bloques, chaincode, logs, etc.).

## Arquitectura general

El sistema se compone de los siguientes módulos principales:

### • Backend (Spring Boot + Java 21)
- Actúa como **cliente Fabric Gateway**, firmando y enviando transacciones a la red.
- Gestiona la autenticación (mock EntraID).
- Orquesta el ciclo de vida del voto: autenticación → cifrado off-chain → emisión → registro en blockchain.
- Expone una API REST que consumen kioscos y aplicaciones web/móviles.

### • Frontend (Angular)
- Interfaz para el votante: autenticación, selección de candidatura y confirmación.
- Se ejecuta en:
  - **Kioscos físicos** distribuidos por la factoría (modo kiosco).
  - Navegador web estándar (para PoC).

### • Chaincode (Node.js / fabric-contract-api)
- Contrato inteligente que define la lógica de la elección.
- Funciones previstas:
  - `crearEleccion()`
  - `registrarVotante()`
  - `emitirVoto()`
  - `cerrarEleccion()`
  - `tally()`
  - `consultarResultados()`

### • Red Blockchain (Hyperledger Fabric)
- Red **permissioned** compuesta por varias organizaciones:
  - **Empresa**
  - **Sindicato A**
  - **Sindicato B**
  - **Auditor** (opcional, acceso sólo lectura)
- Cada organización mantiene su **peer** en infraestructura controlada.
- Ordering Service basado en Raft (**CFT — Crash Fault Tolerant**).
- Identidades emitidas por Fabric CA.

### • Kioscos (clientes ligeros)
- Ejecutan únicamente el **frontend Angular**.
- No ejecutan peers.
- La lógica de firma/envío de transacciones la hace el backend Spring Boot.

### • Herramienta de Auditoría: Hyperledger Explorer
- Permite visualizar:
  - Bloques
  - Transacciones
  - Canales
  - Chaincodes
- Se usará para generar **evidencias** en la memoria de la PoC.

---

## Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.5.7**
- **Hyperledger Fabric Java Gateway SDK**
- **JWT** (mock de EntraID)
- **AES-GCM** para almacenamiento off-chain local
- **Maven**
- **Docker Desktop + WSL2**

### Frontend
- **Angular 17**
- Typescript
- Angular Material

### Blockchain
- **Hyperledger Fabric 2.5**
- test-network extendida (3 organizaciones)
- Chaincode en Node.js
- Orquestación con scripts Bash y Docker Compose
- Ordering Service con **Raft (CFT)**

### Herramientas de apoyo
- VSCode (Remote WSL2)
- Hyperledger Explorer
- Curl / jq / node / docker CLI

---

## Propósito del PoC

La PoC tiene como objetivo demostrar:

1. Cómo Hyperledger Fabric aporta **confianza, trazabilidad y resistencia a manipulación** en procesos electorales internos.
2. Cómo múltiples actores (empresa y sindicatos) pueden **compartir la gobernanza** de una red blockchain permissioned.
3. Qué patrones permiten equilibrar **privacidad del voto** con **verificabilidad del resultado**.
4. Cómo integrar aplicaciones corporativas modernas (**Spring Boot + Angular**) con una red Fabric usando el **Gateway SDK**.

---

## Objetivos técnicos

- Desplegar una red Hyperledger Fabric local con al menos **tres organizaciones**.
- Implementar y desplegar un **chaincode** para la gestión del voto.
- Conectar el backend Spring Boot al Fabric mediante **Java Gateway SDK**.
- Simular el proceso de votación a través de kioscos (frontend Angular).
- Registrar votos cifrados off-chain y hash o referencia inmutable on-chain.
- Demostrar la trazabilidad y auditoría mediante **Hyperledger Explorer**.
- Documentar todas las decisiones arquitectónicas con criterios técnicos sólidos.

---

## Entregables esperados (TFG)

- Código funcional: backend, chaincode, frontend y scripts de infraestructura.
- Capturas de evidencia de:
  - Transacciones en Explorer
  - Bloques generados
  - Funciones chaincode invocadas
  - Flujo del votante
- Documento técnico que justifique:
  - Endorsement policy
  - Modelo de privacidad
  - Arquitectura de gobernanza
  - Seguridad y limitaciones
- Repositorio GitHub público o privado para revisión académica.

---

**Autor:** Enrique Huet Adrover  
**Entorno:** Windows 10 + Docker Desktop + WSL2 (Ubuntu)  
**IDE:** VSCode (Remote WSL2)  
**Fecha:** 2025
