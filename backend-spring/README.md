# Backend - Sistema de Votación (Spring Boot)

Este directorio contiene el código fuente del servidor backend, desarrollado en Java 21 con Spring Boot.

## 🛠️ Tecnologías

*   **Java 21**: Lenguaje de programación.
*   **Spring Boot 3.x**: Framework de aplicación.
*   **Hyperledger Fabric Gateway SDK**: Para conectar con la blockchain.
*   **Spring Security + JWT**: Para autenticación y autorización.

## 📂 Estructura Clave

*   `src/main/java`: Código fuente Java.
*   `src/main/resources/application.yaml`: Configuración principal (puertos, rutas, credenciales).
*   `data/`: Almacenamiento local para la base de datos ligera (`elections-db.json`) y votos cifrados (`storage/`).
*   `wallet/`: Almacena las identidades digitales (certificados) para firmar transacciones en Fabric. Se genera automáticamente al arrancar.

## � Seguridad y Cifrado

El backend actúa como un intermediario de confianza cero ("Zero Trust") respecto al contenido del voto:

1.  **Recepción de Votos**: Recibe los votos ya cifrados desde el frontend (RSA). No tiene capacidad para descifrarlos durante la fase de votación.
2.  **Almacenamiento Híbrido**:
    *   **Off-chain**: Persiste el **BLOB cifrado** en el sistema de archivos local (`data/storage/`), aplicando una segunda capa de cifrado (AES-GCM) con la clave maestra del servidor.
    *   **On-chain**: Envía el **Hash (Commitment)** del voto a la red Hyperledger Fabric para garantizar su inmutabilidad.
3.  **Escrutinio y Verificación**: 
    *   Solo cuando la Junta Electoral proporciona la clave privada RSA (al cerrar la elección), el backend puede descifrar los BLOBs.
    *   **Integridad**: Antes de contar cada voto, el sistema verifica que el hash del BLOB local coincida con el commitment almacenado en la Blockchain.

## �🚀 Ejecución

El backend suele ser orquestado por los scripts en la raíz del proyecto, pero puede ejecutarse individualmente si la red Fabric ya está activa.

### Requisitos
*   Red Hyperledger Fabric activa.
*   Certificados generados en `../fabric-samples/test-network`.


### Comando
```bash
./mvnw spring-boot:run
```

O utilizando el script de utilidad:
```bash
../scripts/run-backend.sh
```
