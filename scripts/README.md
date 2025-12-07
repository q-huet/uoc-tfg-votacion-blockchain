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
| `clean-fabric.sh` | Elimina ejemplos y archivos innecesarios de la carpeta `fabric/` tras la instalación. |
| `simulate-hack.sh` | **Seguridad**. Simula un intento de voto fraudulento por parte de Org1 para verificar el rechazo de la red. |
| `generate_evidence.sh` | Recopila logs, estados y evidencias de seguridad (incluyendo intentos de hackeo). |

## ⚠️ Notas Importantes

*   **Permisos**: Asegúrate de dar permisos de ejecución: `chmod +x *.sh`.
*   **Puertos**: Los scripts asumen que los puertos 8080 (Back), 4200 (Front) y 7984/5984 (CouchDB) están libres.
