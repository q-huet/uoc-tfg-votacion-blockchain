#!/bin/bash
# scripts/clean-fabric.sh
#
# Este script limpia la instalación de Hyperledger Fabric en 'fabric-samples',
# eliminando los ejemplos (samples) innecesarios y manteniendo solo los binarios,
# la configuración y la red de prueba (test-network) del proyecto.

# Directorio raíz del proyecto
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FABRIC_DIR="${PROJECT_ROOT}/fabric-samples"

echo "🧹 Iniciando limpieza de componentes de Fabric en ${FABRIC_DIR}..."

if [ ! -d "${FABRIC_DIR}" ]; then
    echo "❌ El directorio ${FABRIC_DIR} no existe."
    exit 1
fi

# 1. Limpiar subdirectorios innecesarios dentro de 'fabric-samples'
# Mantenemos solo: bin, config, test-network
echo "🧹 Limpiando directorios innecesarios..."

# Lista de carpetas a MANTENER
KEEP_DIRS=("bin" "config" "test-network")

for dir in "${FABRIC_DIR}"/*; do
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

# Eliminar archivos sueltos en fabric-samples/ (como READMEs de los samples, etc)
# Mantenemos .gitignore si existe
find "${FABRIC_DIR}" -maxdepth 1 -type f -not -name ".gitignore" -delete

echo "✨ Limpieza completada. La carpeta 'fabric-samples' contiene solo lo esencial."
