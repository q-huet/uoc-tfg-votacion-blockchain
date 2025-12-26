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
*   `EmitVote`: Registra un hash de voto (transacción inmutable) y lo vincula al usuario para evitar doble voto.
*   `CloseElection`: Finaliza el periodo de votación.
*   `GetVote`: Permite recuperar el commitment (hash) de un voto específico dado su ID de transacción, utilizado para verificar la integridad de los datos off-chain.

## 🔐 Privacidad en Blockchain

Para cumplir con los requisitos de privacidad y escalabilidad:

*   **No se almacenan datos personales**: El chaincode no guarda información que vincule directamente un voto con una identidad real de forma pública.
*   **Integridad del Voto**: Se almacena el **Hash (Commitment)** del voto cifrado. Esto permite verificar matemáticamente que el voto contado (almacenado off-chain) es exactamente el mismo que se emitió, sin revelar su contenido en el ledger público.
*   **Prevención de Doble Voto**: El chaincode mantiene un registro de los IDs de usuario que ya han participado en una elección específica.
