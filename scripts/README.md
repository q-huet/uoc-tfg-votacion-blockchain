# Scripts de Utilidad

Este directorio contiene scripts Bash para gestionar el ciclo de vida completo del proyecto (Red, Backend, Frontend).

## 📜 Índice de Scripts

### Ciclo de Vida Principal

| Script | Descripción | Uso Típico |
|--------|-------------|------------|
| **`start-all.sh`** | **INICIO LIMPIO**. Destruye todo (contenedores, datos, wallet) y levanta el sistema desde cero. | Primera ejecución o reset total. |
| **`stop-soft.sh`** | **PAUSA**. Detiene los contenedores y procesos sin borrar datos. | Al terminar la jornada de trabajo. |
| **`resume-soft.sh`** | **REANUDAR**. Arranca los contenedores detenidos y reinicia Back/Front. | Al volver a trabajar tras un `stop-soft`. |

### Componentes Individuales

| Script | Descripción |
|--------|-------------|
| `start-network.sh` | Levanta solo la red Hyperledger Fabric y despliega el Chaincode. |
| `run-backend.sh` | Compila y ejecuta el Backend Spring Boot. |
| `run-frontend.sh` | Instala dependencias y sirve el Frontend Angular. |
| `check-environment.sh` | Verifica versiones de Java, Docker, Node, etc. |
| `clean-fabric.sh` | Elimina ejemplos y archivos innecesarios de la carpeta `fabric-samples/` tras la instalación. |

| `simulate-hack.sh` | **Seguridad**. Simula un intento de voto fraudulento por parte de Org1 (Sindicato A) sin el aval de Org2 (Sindicato B). |
| `simulate-hack-company.sh` | **Seguridad**. Simula un intento de voto ilegítimo por parte de la Empresa (Orderer Org) usando su identidad administrativa. |
| `generate_evidence.sh` | Recopila logs, estados y evidencias de seguridad (incluyendo intentos de hackeo). |
| `decrypt_blob.sh` | **Utilidad**. Permite desencriptar manualmente un archivo BLOB (`.enc`) utilizando la clave maestra del servidor (AES) y la clave privada de la elección (RSA). Útil para auditorías manuales. |

## 🔐 Gestión de Claves (Cold Storage)

Aunque no se incluyen scripts automatizados para la gestión de "Cold Storage" (por su naturaleza offline), la arquitectura asume que:
1.  Las claves de elección se generan en un entorno seguro.
2.  Solo la **Clave Pública** se carga en el sistema (`backend-spring/data/elections-db.json` o similar) al crear la elección.
3.  La **Clave Privada** permanece fuera de línea hasta el momento del recuento.

## ⚠️ Notas Importantes

*   **Permisos**: Asegúrate de dar permisos de ejecución: `chmod +x *.sh`.
*   **Puertos**: Los scripts asumen que los puertos 8080 (Back), 4200 (Front) y 7984/5984 (CouchDB) están libres.
