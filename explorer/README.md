# Hyperledger Explorer

Este directorio contiene la configuración y despliegue de **Hyperledger Explorer**, una herramienta de visualización para la red Hyperledger Fabric.

## Descripción

Hyperledger Explorer permite visualizar:
- Bloques y transacciones en tiempo real.
- Nodos (Peers) y Orderers.
- Canales (`electionchannel`).
- Chaincodes instalados (`electioncc`).

## Requisitos

- La red Hyperledger Fabric debe estar en ejecución (`test-network`).
- Los certificados criptográficos deben haberse generado en `../fabric/test-network/organizations`.

## Configuración

- **`docker-compose.yaml`**: Define los servicios `explorer` y `explorer-db` (PostgreSQL).
- **`config.json`**: Configuración principal del Explorer. Define el perfil de conexión y las credenciales del administrador.
- **`connection-profile.json`**: Define la topología de la red Fabric (Peers, Orderers, CAs) para que el Explorer pueda conectarse.

## Uso

El ciclo de vida de Explorer está integrado en los scripts principales del proyecto, pero también puede gestionarse manualmente.

### Inicio Automático (Recomendado)

El script principal del proyecto inicia Explorer automáticamente:

```bash
./scripts/start-all.sh
```

### Gestión Manual

Si necesitas iniciar o reiniciar solo el Explorer:

```bash
cd explorer
docker-compose up -d
```

Para detenerlo y limpiar volúmenes (útil si hay errores de wallet):

```bash
cd explorer
docker-compose down -v
```

## Acceso

Una vez iniciado, accede a la interfaz web en:

👉 **http://localhost:8090**

### Credenciales por defecto
El Explorer está configurado con autenticación desactivada o usando las credenciales definidas en `config.json` para la conexión a Fabric, pero el acceso web es directo en esta configuración de desarrollo.

## Solución de Problemas

Si el Explorer se cierra inmediatamente después de iniciar:
1. Revisa los logs: `docker logs explorer`.
2. Asegúrate de que la red Fabric está arriba.
3. Verifica que los certificados en `config.json` y `connection-profile.json` apuntan a las rutas correctas dentro del contenedor (`/tmp/crypto/...`).
4. Si ves errores de "Wallet", borra el volumen y reinicia: `docker-compose down -v && docker-compose up -d`.
