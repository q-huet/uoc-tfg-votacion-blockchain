#!/bin/bash
# Script para verificar que el entorno de desarrollo está correctamente configurado
# Compatible con PC corporativo (con proxy) y PC personal (sin proxy)

echo "======================================"
echo "   VERIFICACIÓN DE ENTORNO - TFG"
echo "======================================"
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_command() {
    if command -v "$1" &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 instalado: $(command -v $1)"
        if [ ! -z "$2" ]; then
            echo "  Versión: $($2)"
        fi
    else
        echo -e "${RED}✗${NC} $1 NO instalado"
        return 1
    fi
}

check_version() {
    local name=$1
    local version=$2
    local expected=$3
    echo -e "${YELLOW}→${NC} $name: $version (esperado: $expected)"
}

echo "## 1. SISTEMA OPERATIVO"
echo "-----------------------------------"
echo "OS: $(uname -s)"
echo "Kernel: $(uname -r)"
echo "Distribución: $(lsb_release -ds 2>/dev/null || cat /etc/os-release | grep PRETTY_NAME | cut -d'"' -f2)"
echo ""

echo "## 2. GIT y SSH"
echo "-----------------------------------"
check_command "git" "git --version"
check_command "ssh" "ssh -V"

# Verificar clave SSH
if [ -f ~/.ssh/id_ed25519.pub ]; then
    echo -e "${GREEN}✓${NC} Clave SSH encontrada: ~/.ssh/id_ed25519.pub"
    echo "  Fingerprint: $(ssh-keygen -lf ~/.ssh/id_ed25519.pub | awk '{print $2}')"
else
    echo -e "${RED}✗${NC} No se encontró clave SSH en ~/.ssh/id_ed25519.pub"
fi

# Verificar configuración SSH para GitHub
if [ -f ~/.ssh/config ]; then
    echo -e "${GREEN}✓${NC} Archivo ~/.ssh/config existe"
    if grep -q "github.com" ~/.ssh/config; then
        echo -e "${GREEN}✓${NC} Configuración de GitHub encontrada en SSH config"
        if grep -q "ProxyCommand" ~/.ssh/config; then
            echo -e "${YELLOW}→${NC} Proxy configurado (PC corporativo)"
        else
            echo -e "${YELLOW}→${NC} Sin proxy (PC personal)"
        fi
    fi
else
    echo -e "${YELLOW}→${NC} Archivo ~/.ssh/config no existe"
fi

# Test conectividad GitHub
echo -e "\nProbando conexión SSH a GitHub..."
if ssh -T git@github.com 2>&1 | grep -q "successfully authenticated"; then
    echo -e "${GREEN}✓${NC} Conexión SSH a GitHub exitosa"
else
    echo -e "${RED}✗${NC} Error conectando a GitHub via SSH"
fi
echo ""

echo "## 3. JAVA (JDK)"
echo "-----------------------------------"
check_command "java" "java --version | head -1"

if [ -d ~/.sdkman ]; then
    echo -e "${GREEN}✓${NC} SDKMAN instalado"
    source ~/.sdkman/bin/sdkman-init.sh 2>/dev/null
    if command -v sdk &> /dev/null; then
        echo "  Java actual: $(sdk current java 2>/dev/null | grep -oP '(?<=: ).*' || echo 'No detectado')"
    fi
else
    echo -e "${YELLOW}→${NC} SDKMAN no instalado"
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
check_version "Java" "$JAVA_VERSION" "21.x"
echo ""

echo "## 4. MAVEN"
echo "-----------------------------------"
check_command "mvn" "mvn --version | head -1"

if [ -f ~/.m2/settings.xml ]; then
    echo -e "${GREEN}✓${NC} Maven settings.xml existe"
    if grep -q "proxy" ~/.m2/settings.xml; then
        echo -e "${YELLOW}→${NC} Proxy configurado en Maven (PC corporativo)"
    else
        echo -e "${YELLOW}→${NC} Sin proxy en Maven (PC personal)"
    fi
else
    echo -e "${YELLOW}→${NC} Maven settings.xml no existe"
fi

MVN_VERSION=$(mvn --version 2>&1 | head -1 | awk '{print $3}')
check_version "Maven" "$MVN_VERSION" "3.9+ o 4.0+"
echo ""

echo "## 5. NODE.JS y NPM"
echo "-----------------------------------"
check_command "node" "node --version"
check_command "npm" "npm --version"

if [ -d ~/.nvm ]; then
    echo -e "${GREEN}✓${NC} NVM instalado en ~/.nvm"
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    echo "  Versión Node actual: $(nvm current 2>/dev/null || echo 'No detectado')"
else
    echo -e "${YELLOW}→${NC} NVM no instalado"
fi

NODE_VERSION=$(node --version 2>&1)
NPM_VERSION=$(npm --version 2>&1)
check_version "Node.js" "$NODE_VERSION" "20.x LTS"
check_version "npm" "$NPM_VERSION" "10.x"

# Verificar configuración proxy npm
NPM_PROXY=$(npm config get proxy 2>/dev/null)
if [ "$NPM_PROXY" != "null" ] && [ ! -z "$NPM_PROXY" ]; then
    echo -e "${YELLOW}→${NC} Proxy configurado en npm: $NPM_PROXY"
else
    echo -e "${YELLOW}→${NC} Sin proxy en npm"
fi
echo ""

echo "## 6. ANGULAR CLI"
echo "-----------------------------------"
check_command "ng" "ng version --version 2>&1 | head -1"

NG_VERSION=$(ng version 2>&1 | grep "Angular CLI" | awk '{print $3}')
check_version "Angular CLI" "$NG_VERSION" "17.3.x"
echo ""

echo "## 7. PROYECTO TFG"
echo "-----------------------------------"
PROJECT_DIR=$(pwd)
if [ -f "$PROJECT_DIR/pom.xml" ] || [ -f "$PROJECT_DIR/backend-spring/pom.xml" ]; then
    echo -e "${GREEN}✓${NC} Ejecutando desde el directorio del proyecto: $PROJECT_DIR"
    
    # Git status
    BRANCH=$(git branch --show-current 2>/dev/null)
    echo "  Branch actual: $BRANCH"
    echo "  Último commit: $(git log -1 --pretty=format:'%h - %s' 2>/dev/null)"
    
    # Backend
    if [ -f backend-spring/pom.xml ]; then
        echo -e "${GREEN}✓${NC} Backend Spring Boot encontrado"
        SPRING_VERSION=$(grep -oP '<parent>.*?<version>\K[^<]+' backend-spring/pom.xml | head -1)
        echo "  Spring Boot: $SPRING_VERSION"
    else
        echo -e "${RED}✗${NC} Backend Spring Boot NO encontrado"
    fi
    
    # Frontend
    if [ -f frontend-angular/package.json ]; then
        echo -e "${GREEN}✓${NC} Frontend Angular encontrado"
        if [ -d frontend-angular/node_modules ]; then
            echo -e "${GREEN}✓${NC} node_modules instalado"
        else
            echo -e "${YELLOW}→${NC} node_modules NO instalado (ejecutar: npm install)"
        fi
    else
        echo -e "${RED}✗${NC} Frontend Angular NO encontrado"
    fi
else
    echo -e "${RED}✗${NC} No parece estar en la raíz del proyecto (no se encontró pom.xml o backend-spring)"
fi
echo ""

echo "## 8. PUERTOS y SERVICIOS"
echo "-----------------------------------"
check_port() {
    if lsof -i :$1 &>/dev/null || netstat -tuln 2>/dev/null | grep -q ":$1 "; then
        echo -e "${GREEN}✓${NC} Puerto $1 ($2) en uso"
    else
        echo -e "${YELLOW}→${NC} Puerto $1 ($2) libre"
    fi
}

check_port 8080 "Backend Spring Boot"
check_port 4200 "Frontend Angular"
echo ""

echo "======================================"
echo "   RESUMEN"
echo "======================================"
echo ""

# Verificar componentes críticos
CRITICAL_OK=true

command -v git &>/dev/null || CRITICAL_OK=false
command -v java &>/dev/null || CRITICAL_OK=false
command -v mvn &>/dev/null || CRITICAL_OK=false
command -v node &>/dev/null || CRITICAL_OK=false
command -v npm &>/dev/null || CRITICAL_OK=false
command -v ng &>/dev/null || CRITICAL_OK=false
[ -f ~/.ssh/id_ed25519.pub ] || CRITICAL_OK=false

if [ "$CRITICAL_OK" = true ]; then
    echo -e "${GREEN}✓ TODOS LOS COMPONENTES CRÍTICOS ESTÁN INSTALADOS${NC}"
    echo ""
    echo "Puedes trabajar en este proyecto desde este PC."
else
    echo -e "${RED}✗ FALTAN COMPONENTES CRÍTICOS${NC}"
    echo ""
    echo "Instala los componentes faltantes antes de trabajar en el proyecto."
fi

echo ""
echo "Para comparar con otro PC, ejecuta este script en ambos y compara los resultados."
echo "Script ejecutado desde: $0"
echo ""
