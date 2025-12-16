# Diseño de Scripts y DevOps

Este documento cubre los scripts de automatización para el ciclo de vida de la red, despliegue y pruebas de seguridad.

## 1. Ciclo de Vida de la Red

La gestión de la infraestructura blockchain se realiza mediante scripts de Bash que encapsulan los comandos nativos de Hyperledger Fabric (`peer`, `osnadmin`, `configtxgen`).

### `start-network.sh`
Es el script maestro de inicialización. Realiza las siguientes tareas secuenciales:

```mermaid
graph TD
    Start[Inicio] --> Clean[Limpieza de Contenedores y Volúmenes]
    Clean --> Up[Levantar Nodos (Docker Compose)]
    Up --> Channel[Crear Canal 'electionchannel']
    Channel --> Join[Unir Peers al Canal]
    Join --> Deploy[Desplegar Chaincode (Java)]
    Deploy --> End[Red Lista]
```

### Comandos Clave
*   **Limpieza**: `docker rm -f $(docker ps -aq ...)` asegura que no queden contenedores "zombies" que bloqueen puertos.
*   **Despliegue**: Utiliza `./network.sh deployCC` para empaquetar, instalar, aprobar y confirmar el chaincode en todos los peers de la organización.

## 2. Gestión de Identidades

El sistema utiliza la infraestructura de clave pública (PKI) generada por Fabric.

*   **Material Criptográfico**: Se encuentra en `fabric-samples/test-network/organizations/`.
*   **Estructura**:
    *   `peerOrganizations/org1.example.com/users/User1@org1.example.com/`: Contiene el certificado (`signcerts`) y la clave privada (`keystore`) que el Backend utiliza para firmar transacciones.

    *   `ordererOrganizations/`: Identidades para los nodos ordenadores.

## 3. Simulaciones de Seguridad

Se han desarrollado scripts específicos para validar la robustez de la red ante ataques comunes en entornos de consorcio.

### Escenario A: Ataque de Consenso (`simulate-hack.sh`)
Simula un intento de **Org1** de registrar un voto unilateralmente, puenteando a **Org2**.

*   **Mecanismo**: El script invoca el chaincode enviando la propuesta *solo* al peer de Org1 (`localhost:7051`).
*   **Resultado Esperado**: La transacción falla en la fase de validación (o incluso en el envío al Orderer si el cliente verifica la política) porque la política de aval `AND('Org1MSP.peer', 'Org2MSP.peer')` no se cumple. Falta la firma de Org2.

```bash
# Snippet del ataque
peer chaincode invoke \
    -o localhost:7050 \
    --peerAddresses localhost:7051 \ # Solo Org1
    # --peerAddresses localhost:9051 \ # Org2 OMITIDO INTENCIONALMENTE
    -C electionchannel -n electioncc ...
```

### Escenario B: Ataque de Identidad (`simulate-hack-company.sh`)
Simula un intento de la **Empresa (Orderer)** de influir en la votación usando sus credenciales administrativas.

*   **Mecanismo**: Se exportan las variables de entorno `CORE_PEER_LOCALMSPID="OrdererMSP"` y se intenta invocar el chaincode.
*   **Resultado Esperado**: Rechazo. Aunque el Orderer es parte de la red, su MSP no forma parte de la política de aval del chaincode (que solo incluye a los sindicatos). Por tanto, su firma no es válida para satisfacer la política `AND(Org1, Org2)`.

