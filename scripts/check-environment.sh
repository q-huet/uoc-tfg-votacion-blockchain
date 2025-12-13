#!/bin/bash
# Script para verificar y preparar el entorno de desarrollo
# Diseñado para Ubuntu/Debian (WSL2 recomendado)

echo "=================================================="
echo "   VERIFICACIÓN DE REQUISITOS - VOTACIÓN BLOCKCHAIN"
echo "=================================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables de estado
MISSING_TOOLS=0

check_command() {
    local cmd=$1
    local version_cmd=$2
    
    if command -v "$cmd" &> /dev/null; then
        echo -e "${GREEN}✓${NC} $cmd instalado"
        if [ ! -z "$version_cmd" ]; then
            echo -e "   Version: $(eval $version_cmd)"
        fi
        return 0
    else
        echo -e "${RED}✗${NC} $cmd NO instalado"
        MISSING_TOOLS=$((MISSING_TOOLS+1))
        return 1
    fi
}

install_suggestion() {
    echo -e "${YELLOW}→ Para instalar:${NC} $1"
}

echo -e "${BLUE}## 1. Herramientas Básicas del Sistema${NC}"
echo "-----------------------------------"

# Curl
if ! check_command "curl" "curl --version | head -n 1"; then
    install_suggestion "sudo apt-get update && sudo apt-get install -y curl"
fi

# Git
if ! check_command "git" "git --version"; then
    install_suggestion "sudo apt-get install -y git"
fi

# JQ (Procesador JSON)
if ! check_command "jq" "jq --version"; then
    install_suggestion "sudo apt-get install -y jq"
fi

# Build Essential (Make, GCC, etc.)
if ! check_command "make" "make --version | head -n 1"; then
    install_suggestion "sudo apt-get install -y build-essential"
fi

echo ""
echo -e "${BLUE}## 2. Docker (Contenedores)${NC}"
echo "-----------------------------------"

# Docker
if check_command "docker" "docker --version"; then
    # Verificar permisos de usuario
    if groups | grep -q "docker"; then
        echo -e "${GREEN}✓${NC} Usuario pertenece al grupo 'docker'"
    else
        echo -e "${RED}✗${NC} Usuario NO pertenece al grupo 'docker'"
        echo -e "${YELLOW}→ Ejecuta:${NC} sudo usermod -aG docker \$USER && newgrp docker"
        MISSING_TOOLS=$((MISSING_TOOLS+1))
    fi
    
    # Verificar si el servicio corre
    if docker info >/dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Docker Daemon está corriendo"
    else
        echo -e "${RED}✗${NC} Docker Daemon NO está corriendo"
        echo -e "${YELLOW}→ Inicia Docker Desktop o ejecuta:${NC} sudo service docker start"
        MISSING_TOOLS=$((MISSING_TOOLS+1))
    fi
else
    install_suggestion "Sigue la guía oficial: https://docs.docker.com/engine/install/ubuntu/"
fi

# Docker Compose
if ! check_command "docker-compose" "docker-compose --version"; then
    # Check for docker compose plugin (v2)
    if docker compose version >/dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Docker Compose (Plugin v2) instalado"
    else
        echo -e "${RED}✗${NC} Docker Compose NO instalado"
        install_suggestion "sudo apt-get install -y docker-compose-plugin"
    fi
fi

echo ""
echo -e "${BLUE}## 3. Java (Backend)${NC}"
echo "-----------------------------------"

# Java 21
if check_command "java" "java -version 2>&1 | head -n 1"; then
    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VER" -ge 21 ]; then
        echo -e "${GREEN}✓${NC} Versión de Java correcta ($JAVA_VER >= 21)"
    else
        echo -e "${RED}✗${NC} Versión de Java incorrecta ($JAVA_VER). Se requiere Java 21."
        install_suggestion "sudo apt-get install -y openjdk-21-jdk"
        MISSING_TOOLS=$((MISSING_TOOLS+1))
    fi
else
    install_suggestion "sudo apt-get install -y openjdk-21-jdk"
fi

echo ""
echo -e "${BLUE}## 4. Node.js (Frontend)${NC}"
echo "-----------------------------------"

# Node.js
if check_command "node" "node --version"; then
    NODE_VER=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VER" -ge 18 ]; then
        echo -e "${GREEN}✓${NC} Versión de Node correcta ($NODE_VER >= 18)"
    else
        echo -e "${RED}✗${NC} Versión de Node antigua ($NODE_VER). Se recomienda v18 o superior."
        install_suggestion "curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - && sudo apt-get install -y nodejs"
        MISSING_TOOLS=$((MISSING_TOOLS+1))
    fi
else
    install_suggestion "curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - && sudo apt-get install -y nodejs"
fi

# NPM
check_command "npm" "npm --version"

# Angular CLI
if ! check_command "ng" "ng version | grep 'Angular CLI' | head -n 1"; then
    install_suggestion "npm install -g @angular/cli"
fi

echo ""
echo "=================================================="
if [ $MISSING_TOOLS -eq 0 ]; then
    echo -e "${GREEN}¡TODO LISTO!${NC} Tu entorno cumple con los requisitos."
    echo "Puedes proceder a ejecutar: ./install-fabric.sh"
else
    echo -e "${RED}ATENCIÓN:${NC} Se detectaron $MISSING_TOOLS problemas."
    echo "Por favor, revisa las sugerencias de instalación arriba."
fi
echo "=================================================="
