#!/bin/bash
# scripts/clean-fabric.sh
#
# Este script limpia la instalación de Hyperledger Fabric, eliminando
# los ejemplos (samples) innecesarios y manteniendo solo los binarios,
# la configuración y la red de prueba (test-network) del proyecto.

# Directorio raíz del proyecto
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FABRIC_DIR="${PROJECT_ROOT}/fabric-samples"

echo "🧹 Iniciando limpieza de componentes de Fabric..."

        echo "✅ Binarios movidos a ${FABRIC_DIR}/bin"
    fi
    
    if [ ! -d "${FABRIC_DIR}/config" ] && [ -d "${FABRIC_DIR}/fabric-samples/config" ]; then
        mv "${FABRIC_DIR}/fabric-samples/config" "${FABRIC_DIR}/"
        echo "✅ Configuración movida a ${FABRIC_DIR}/config"
    fi
    
    # Eliminar el resto de fabric-samples anidado
    rm -rf "${FABRIC_DIR}/fabric-samples"
    echo "🗑️  Carpeta anidada 'fabric-samples' eliminada."
fi

# 2. Eliminar carpeta fabric-samples en la raíz si existe (sobrante de instalación)
if [ -d "${PROJECT_ROOT}/fabric-samples" ]; then
    echo "⚠️  Detectada carpeta 'fabric-samples' en raíz. Moviendo binarios si es necesario..."
    
    if [ ! -d "${FABRIC_DIR}/bin" ] && [ -d "${PROJECT_ROOT}/fabric-samples/bin" ]; then
        mkdir -p "${FABRIC_DIR}"
        mv "${PROJECT_ROOT}/fabric-samples/bin" "${FABRIC_DIR}/"
    fi
    
    if [ ! -d "${FABRIC_DIR}/config" ] && [ -d "${PROJECT_ROOT}/fabric-samples/config" ]; then
        mkdir -p "${FABRIC_DIR}"
        mv "${PROJECT_ROOT}/fabric-samples/config" "${FABRIC_DIR}/"
    fi
    
    rm -rf "${PROJECT_ROOT}/fabric-samples"
    echo "🗑️  Carpeta 'fabric-samples' de raíz eliminada."
fi

# 3. Limpiar subdirectorios innecesarios dentro de 'fabric/'
# Mantenemos solo: bin, config, test-network
echo "🧹 Limpiando directorios innecesarios en ${FABRIC_DIR}..."

# Lista de carpetas a MANTENER
KEEP_DIRS=("bin" "config" "test-network")

if [ -d "$FABRIC_DIR" ]; then
    for dir in "$FABRIC_DIR"/*; do
        if [ -d "$dir" ]; then
            dirname=$(basename "$dir")
            # Comprobar si el directorio está en la lista de mantener
            should_keep=false
            for keep in "${KEEP_DIRS[@]}"; do
                if [ "$dirname" == "$keep" ]; then
                    should_keep=true
                    break
                fi
            done
            
            if [ "$should_keep" = false ]; then
                echo "🗑️  Eliminando: $dirname"
                rm -rf "$dir"
            fi
        fi
    done
    
    # Eliminar archivos sueltos en fabric/ (como READMEs de los samples, etc)
    find "$FABRIC_DIR" -maxdepth 1 -type f -not -name ".gitignore" -delete
fi

echo "✨ Limpieza completada. La carpeta 'fabric' contiene solo lo esencial."
