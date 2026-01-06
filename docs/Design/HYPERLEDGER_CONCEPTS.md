# Conceptos Fundamentales de Hyperledger Fabric

Este documento sirve como referencia técnica sobre los componentes y conceptos clave de Hyperledger Fabric, la tecnología DLT (Distributed Ledger Technology) sobre la que se construye este sistema de votación.

## 1. Modelo de Hyperledger Fabric

Hyperledger Fabric es una plataforma de blockchain **permisionada** (permissioned) de grado empresarial. A diferencia de las blockchains públicas (como Bitcoin o Ethereum), Fabric:

*   **Identidad Conocida:** Todos los participantes deben estar autenticados e identificados.
*   **Arquitectura Modular:** Permite componentes "plug-and-play" (consenso, base de datos de estado, gestión de identidades).
*   **Rendimiento:** Soporta miles de transacciones por segundo gracias a su arquitectura única de **Ejecutar-Ordenar-Validar**.

## 2. Estructura de la Red y Componentes

### Organizaciones (Orgs)
Son las entidades lógicas que gestionan la red (ej. "Empresa", "Sindicato A", "Sindicato B"). Cada organización gestiona sus propios nodos y usuarios.

### Peers (Nodos Pares)
Son los elementos fundamentales de la red. Mantienen el ledger y ejecutan los contratos inteligentes.
*   **Endorsing Peer (Avalador):** Ejecuta el chaincode, valida la transacción y la firma (aval).
*   **Committing Peer (Confirmador):** Mantiene el ledger. Recibe bloques del Orderer, valida las firmas y actualiza su copia local del estado.
*   **Anchor Peer:** Permite la comunicación entre diferentes organizaciones (gossip protocol).

### Orderer (Servicio de Ordenación)
Es el "director de tráfico" de la red.
*   **Función:** Recibe transacciones avaladas de los clientes, las ordena cronológicamente y las empaqueta en **bloques**.
*   **Consenso (Raft):** En Fabric 2.x, se usa Raft (EtcdRaft), un protocolo de consenso tolerante a fallos (CFT).
*   **Importante:** El Orderer **NO** valida las transacciones ni ve el contenido del estado (si se usa cifrado o canales privados). Solo garantiza el orden y la entrega.

### MSP (Membership Service Provider)
Es el componente que define las reglas de identidad y acceso.
*   **Función:** Abstrae la criptografía (certificados X.509) en identidades lógicas (Roles: Admin, Client, Peer, Member).
*   **Local MSP:** Define los permisos de un nodo o usuario (quién es el admin de este peer).
*   **Channel MSP:** Define los derechos de las organizaciones dentro de un canal.

### Identidades (CAs)
Fabric usa certificados X.509 estándar.
*   **Fabric CA:** Es una Autoridad de Certificación que emite certificados para los actores de la red.
*   Cada organización suele tener su propia CA Root para mantener la soberanía sobre sus identidades.

## 3. El Ledger (Libro Mayor)

El Ledger en Fabric consta de dos partes distintas pero relacionadas:

### A. Blockchain (Historial Inmutable)
*   Es una cadena de bloques enlazados por hash.
*   Almacena **todas** las transacciones que han ocurrido en la historia, sean válidas o inválidas.
*   Es inmutable: una vez escrito, no se puede borrar ni modificar.
*   Se almacena en el sistema de archivos del Peer.

### B. World State (Estado del Mundo)
*   Es una base de datos que mantiene el **valor actual** de los atributos.
*   Permite leer el estado actual sin tener que recorrer toda la blockchain.
*   **Opciones de Base de Datos:**
    *   **LevelDB:** Base de datos clave-valor simple (por defecto). Rápida pero con consultas limitadas.
    *   **CouchDB:** Base de datos documental JSON. Permite **consultas ricas** (Rich Queries) sobre el contenido de los datos (ej. "dame todos los votos del candidato X"). *Esta es la opción usada en este proyecto.*

## 4. Canales (Channels)

Un canal es una **sub-red privada** de comunicación entre dos o más miembros de la red.
*   **Aislamiento:** Las transacciones en un canal son invisibles para los miembros que no están en ese canal.
*   **Ledger Propio:** Cada canal tiene su propio Ledger independiente.
*   En este proyecto, usamos un único canal (`electionchannel`) donde participan todas las organizaciones para garantizar transparencia total.

## 5. Chaincode (Smart Contracts)

El Chaincode es el código que define la lógica de negocio.
*   **Empaquetado:** Se despliega en contenedores Docker independientes gestionados por los Peers.
*   **Ciclo de Vida (Fabric 2.x):**
    1.  **Package:** Empaquetar el código.
    2.  **Install:** Instalar en los peers.
    3.  **Approve:** Cada organización aprueba la definición (gobernanza descentralizada).
    4.  **Commit:** Se activa el contrato en el canal.

## 6. Flujo de Transacción (Execute-Order-Validate)

Fabric invierte el modelo tradicional de blockchain (Order-Execute) para ganar escalabilidad.

1.  **Execute (Propuesta):** El cliente envía la transacción a los Peers Avaladores. Estos ejecutan el chaincode (simulación) y devuelven el resultado firmado (RW Set).
2.  **Order (Ordenación):** El cliente envía las firmas al Orderer. El Orderer crea un bloque con muchas transacciones.
3.  **Validate (Validación):** El Orderer envía el bloque a todos los Peers.
    *   **Validación Sintáctica:** ¿El bloque está bien formado?
    *   **Validación de Aval:** ¿Cumple la transacción la Política de Aval (ej. "Org1 Y Org2")?
    *   **Validación MVCC:** ¿Ha cambiado el estado desde que se simuló la transacción? (Control de concurrencia).
4.  **Commit:** Si todo es correcto, se actualiza el World State.

## 7. Políticas (Policies)

Definen las reglas de gobernanza de la red.
*   **Endorsement Policy:** Quién debe aprobar una transacción para que sea válida (ej. `AND('Org1.member', 'Org2.member')`).
*   **Lifecycle Policy:** Quién puede aprobar actualizaciones del chaincode.
*   **ACLs (Access Control Lists):** Quién puede invocar ciertos métodos o acceder a recursos.
