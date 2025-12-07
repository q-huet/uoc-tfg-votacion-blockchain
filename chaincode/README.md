# Chaincode - Smart Contract (Java)

Este directorio contiene la lógica de negocio (Smart Contract) que se ejecuta dentro de la red Hyperledger Fabric.

## 🛠️ Tecnologías

*   **Java**: Lenguaje del contrato inteligente.
*   **Fabric Chaincode Shim**: Librería para interactuar con el ledger.

## 📍 Ubicación

El código fuente se encuentra en `java/src`.

## 🚀 Despliegue

El chaincode no se ejecuta "manualmente" como una aplicación normal. Es empaquetado, instalado y aprobado por los peers de la red Hyperledger Fabric.

Este proceso está automatizado en el script `scripts/start-network.sh` (llamado por `start-all.sh`), que utiliza el script `deployCC` de la test-network de Fabric.

## Funciones Principales

*   `InitLedger`: Inicializa el ledger con datos de prueba.
*   `CreateElection`: Registra una nueva elección en la blockchain.
*   `EmitVote`: Registra un hash de voto (transacción inmutable).
*   `CloseElection`: Finaliza el periodo de votación.
