# Proyecto: Sistema de Votación Sindical basado en Blockchain

## Descripción general

Este proyecto implementa un **prototipo de sistema de votación electrónica** para procesos sindicales dentro de un entorno industrial (por ejemplo, una planta de Ford España S.A).  
El objetivo es demostrar cómo la tecnología **blockchain** puede proporcionar **transparencia, trazabilidad e inmutabilidad** en los procesos de voto, preservando al mismo tiempo el anonimato y la verificabilidad de los resultados.

El sistema se compone de varios módulos:

- **Backend (Spring Boot, Java 21)**: gestiona la autenticación, la orquestación de votos, el almacenamiento cifrado off-chain y la comunicación con la red blockchain (Hyperledger Fabric).
- **Frontend (Angular)**: interfaz web donde los empleados pueden autenticarse y emitir su voto.
- **Chaincode (Node.js)**: contrato inteligente desplegado en la red Fabric que gestiona la emisión y registro de votos.
- **Infraestructura Blockchain (Hyperledger Fabric)**: red permissioned que simula los nodos de empresa, sindicatos y auditor externo.
- **Componente opcional de Anchoring (Hardhat + Solidity)**: para publicar hashes de integridad en una blockchain pública (opcional para PoC).

## Stack tecnológico

**Backend**

- Java 21
- Spring Boot 3.5.7
- Maven
- Hyperledger Fabric Java Gateway SDK
- JSON Web Tokens (JWT) simulados (mock EntraID)
- Cifrado AES-GCM para almacenamiento de blobs off-chain
- Docker/Podman (entorno contenedor opcional)
- VSCode + Remote WSL2 para desarrollo

**Frontend**

- Angular 17
- TypeScript
- Angular Material
- Node.js / npm

**Blockchain**

- Hyperledger Fabric 2.5 (network local con test-network extendida)
- Chaincode en Node.js (fabric-contract-api)
- Algoritmo de consenso: CFT Crash Fault-Tolerant (Raft protocol) 

**Otros**

- Scripts en Bash y Node.js para despliegue y pruebas
- Almacenamiento local cifrado (archivos en `/data/storage`)
- Arquitectura modular, basada en microservicio backend + cliente Angular

## Propósito del PoC

El prototipo no busca ser una plataforma de producción, sino una **demostración funcional** para responder a preguntas de investigación:

1. ¿Cómo mejora blockchain la trazabilidad y confianza frente a sistemas centralizados?
2. ¿Qué arquitectura resulta más eficiente en entornos industriales con requisitos de gobernanza compartida?
3. ¿Cómo puede garantizarse el anonimato del votante y la integridad del recuento en un sistema distribuido?

## Objetivos técnicos

- Implementar un backend modular en Spring Boot que pueda comunicarse con una red Fabric local.
- Gestionar el ciclo de vida del voto: autenticación, emisión, cifrado, registro y verificación.
- Simular un flujo de elección sindical con al menos dos actores validadores.
- Documentar las decisiones arquitectónicas y su justificación técnica.

---

**Autor:** Enrique Huet Adrover  
**Entorno:** Windows 10 + WSL2 (Ubuntu)  
**IDE:** VSCode (Remote WSL2)  
**Versión Java:** 21  
**Framework principal:** Spring Boot 3.5.7  
**Fecha:** 2025
