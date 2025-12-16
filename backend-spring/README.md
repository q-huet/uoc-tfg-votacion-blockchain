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

## 🚀 Ejecución

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
