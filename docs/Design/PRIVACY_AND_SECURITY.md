# Gestión de Privacidad, Seguridad y Secreto del Voto

Este documento detalla la arquitectura lógica diseñada para garantizar el secreto del voto, la integridad del proceso y la separación entre la identidad del votante y su elección en el sistema **VotacionBC**.

## 1. Separación de Identidad y Sentido del Voto

El principio fundamental para garantizar el voto secreto en una blockchain permisionada (como Hyperledger Fabric) es desacoplar **quién envía la transacción** de **qué contiene la transacción**.

### El Desafío en Blockchain
En Hyperledger Fabric, cada transacción está firmada criptográficamente por el cliente (el votante). Esto significa que, por defecto, cualquier nodo que inspeccione el ledger podría ver:
`Transacción TX123` -> `Creada por: Usuario1` -> `Payload: Voto por Opción A`.

### La Solución Implementada
Para romper este vínculo, el sistema utiliza un esquema de **Cifrado de Payload**:

1.  **Identidad (Autenticación e Infraestructura):**
    *   **Emisor:** La **Fabric CA (Certificate Authority)** de la organización.
    *   **Funcionamiento:** El Backend valida la identidad del usuario mediante su certificado MSP (X.509). Este certificado es emitido por la CA de la organización (ej. Org1) cuando el usuario se registra.
    *   **Uso:** Se utiliza **exclusivamente** para firmar la transacción, verificar el derecho al voto (Censo) y para impedir el doble voto. Es la capa de "transporte y permisos".

2.  **Sentido del Voto (Cifrado de Aplicación):**
    *   **Emisor:** La **Autoridad Electoral** (a través del Backend) al crear la elección.
    *   **Funcionamiento:** Al iniciarse una elección, el sistema genera un par de claves RSA (Pública/Privada) específicas para ese evento. La **Clave Pública** se envía al Frontend (navegador/terminal).
    *   **Uso:** El contenido del voto (la opción elegida) se cifra con esta Clave Pública **en el dispositivo del cliente (Frontend)** antes de salir hacia el servidor.
    *   **Importancia:** Esto garantiza que ni siquiera el Backend (administrado por la empresa) vea el voto en texto plano. El Backend recibe un "blob" cifrado que simplemente reenvía a la red Blockchain.

De esta forma, el registro inmutable queda así:
*   **Metadatos:** `Usuario1` ha votado en la `Elección 1`. (Público para auditores, garantiza "un usuario, un voto").
*   **Datos:** `Cifrado(Opción A)`. (Ilegible para los nodos y administradores sin la clave de la elección).

---

## 2. Cifrado On-Chain vs. Off-Chain

La arquitectura híbrida distribuye la información para maximizar la seguridad:

### On-Chain (En la Blockchain)
Se almacena la información que requiere **inmutabilidad y trazabilidad**:
*   **Commitment (Hash del Voto):** Un identificador único (SHA-256) del voto cifrado. Sirve como "huella digital" para verificar la integridad.
*   **Metadatos de Auditoría:** ID de transacción, Timestamp, ID de Elección.
*   **Estado del Votante:** Un registro que marca al usuario como `HAS_VOTED` para evitar el doble voto.
*   **NOTA:** NO se almacena el voto cifrado (BLOB) en la Blockchain para optimizar el almacenamiento y cumplir con normativas de privacidad (derecho al olvido).

### Off-Chain (Fuera de la Blockchain)
Se almacena la información sensible o demasiado pesada:
*   **El Voto Cifrado (BLOB):** El archivo binario cifrado con la clave pública de la elección. Se guarda en el sistema de archivos del Backend (`data/storage`).
*   **Claves Privadas de la Elección:**
    *   **¿Quién la tiene?** La **Junta Electoral** (Autoridad de la Elección).
    *   **¿Dónde está?** Custodiada en frío hasta el momento del recuento.
*   **Recibos (Receipts):** Los comprobantes de votación.

---

## 3. Reconstrucción del Voto y Auditoría

### ¿Se puede reconstruir un voto?
Técnicamente, **sí**, pero solo bajo condiciones controladas y restringidas.
Dado que la blockchain guarda quién firmó la transacción y el contenido cifrado, si alguien posee la **Clave Privada de la Elección**, podría descifrar una transacción específica y ver qué votó el usuario que la firmó.

### ¿Va esto en contra del voto secreto?
No, siempre que se cumplan los protocolos de la **Autoridad Electoral**:
1.  **Separación de Roles:** La clave privada no la tiene el administrador del sistema, sino la Junta Electoral.
2.  **Recuento Anónimo:** El software de recuento está diseñado para descifrar **todos** los votos en bloque y sumar los resultados, sin mostrar la relación `Transacción ID <-> Voto Descifrado`.
3.  **Destrucción de Claves:** Una vez finalizado el recuento y validada la elección, la clave privada puede ser destruida, haciendo matemáticamente imposible reconstruir el sentido del voto a posteriori.

**Excepción de Auditoría Judicial:** En caso de impugnación legal grave, la capacidad de reconstruir el voto (trazabilidad) es una característica de seguridad deseada en sistemas electrónicos frente a los tradicionales, permitiendo detectar fraudes masivos, aunque comprometa la privacidad individual en ese escenario extremo.

---

## 4. El Proceso de Recuento

El recuento es el único momento donde se revela la información oculta.

1.  **Cierre de la Elección:** El sistema (Chaincode) cambia el estado de la elección a `CLOSED`. Ya no se aceptan más transacciones.
2.  **Inyección de la Clave:** La Autoridad Electoral introduce la clave privada (o las partes de ella) en el sistema de recuento (Backend).
3.  **Descifrado y Verificación de Integridad:**
    *   El sistema recupera todos los votos cifrados (BLOBs) del almacenamiento local.
    *   **Verificación Cruzada:** Para cada BLOB, se calcula su hash SHA-256 y se compara con el `commitment` almacenado inmutablemente en la Blockchain (recuperado mediante el ID de transacción).
    *   Si los hashes coinciden, se procede al descifrado RSA en memoria.
    *   Si no coinciden, el voto se marca como **manipulado** y se excluye del recuento.
4.  **Agregación:** Se suman los totales por opción.
5.  **Publicación:** Se publica el resultado final.

### ¿Se ve la identidad durante el recuento?
**No.** El algoritmo de recuento itera sobre los valores cifrados. Aunque la identidad del firmante existe en los metadatos de la transacción de Fabric, el proceso de recuento ignora el campo `Creator` y solo procesa el campo `Args` (el voto).

---

## Resumen del Flujo de Seguridad

| Paso | Acción | ¿Quién ve qué? | Ubicación |
| :--- | :--- | :--- | :--- |
| **Voto** | Usuario envía voto | Usuario ve su opción. Backend ve datos cifrados. | Tránsito (TLS) |
| **Registro** | Chaincode graba | Nodos ven `UsuarioX` y `DatosCifrados`. | Blockchain (On-chain) |
| **Custodia** | Espera al cierre | Nadie puede leer los votos. | Blockchain (On-chain) |
| **Recuento** | Autoridad descifra | Sistema suma totales. No asocia ID a Voto. | Memoria del Servidor (Off-chain) |
