#!/bin/bash
# scripts/simulate-hack-company.sh
#
# SIMULACIÓN DE ATAQUE: La Empresa (Orderer Org) intenta votar
# ------------------------------------------------------------------
# Escenario: La Empresa, que controla la infraestructura (Orderer),
# intenta usar su identidad administrativa para emitir un voto.
#
# Dado que la Empresa no tiene Peers en esta topología, debe enviar
# la propuesta a los Peers de los Sindicatos (Org1 y Org2).
#
# Objetivo: Demostrar que el Chaincode o la Red rechazan la transacción
# porque la identidad 'OrdererMSP' no tiene permisos para votar.

export PATH=${PWD}/../fabric/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../fabric/config/

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}=== 🕵️  INICIANDO SIMULACIÓN DE HACKEO (EMPRESA/ORDERER) ===${NC}"
echo "Escenario: El Administrador de la Empresa (OrdererMSP) intenta votar."

# 1. Definir variables de entorno para actuar como Orderer Admin
# Nota: Usamos la identidad del Admin del Orderer, pero necesitamos hablar con los Peers
export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${PWD}/../fabric/test-network/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

# Usamos la identidad MSP del Orderer
export CORE_PEER_LOCALMSPID="OrdererMSP"
export CORE_PEER_MSPCONFIGPATH=${PWD}/../fabric/test-network/organizations/ordererOrganizations/example.com/users/Admin@example.com/msp

# Necesitamos confiar en los certificados TLS de los Peers para hablar con ellos
export PEER0_ORG1_CA=${PWD}/../fabric/test-network/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export PEER0_ORG2_CA=${PWD}/../fabric/test-network/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt

# Datos del voto falso de la empresa
FAKE_ELECTION_ID="election-hack-company"
FAKE_VOTE_HASH="CORPORATE_INJECTION_HASH_999"

echo -e "${BLUE}[1] Construyendo transacción fraudulenta...${NC}"
echo "Identidad: Admin@orderer.example.com (OrdererMSP)"
echo "Target: Peer0.Org1 (Sindicato A) y Peer0.Org2 (Sindicato B)"

# 2. Intentar invocar el chaincode
# Enviamos la petición a ambos sindicatos para ver si "cuelan" el voto de la empresa
set +e

peer chaincode invoke \
    -o localhost:7050 \
    --ordererTLSHostnameOverride orderer.example.com \
    --tls \
    --cafile "$ORDERER_CA" \
    -C electionchannel \
    -n electioncc \
    --peerAddresses localhost:7051 --tlsRootCertFiles "$PEER0_ORG1_CA" \
    --peerAddresses localhost:9051 --tlsRootCertFiles "$PEER0_ORG2_CA" \
    -c "{\"function\":\"EmitVote\",\"Args\":[\"$FAKE_ELECTION_ID\",\"$FAKE_VOTE_HASH\"]}" > scripts/logs/hack_attempt_company.log 2>&1

EXIT_CODE=$?

echo -e "${BLUE}[2] Resultado del intento:${NC}"
cat scripts/logs/hack_attempt_company.log

if [ $EXIT_CODE -ne 0 ]; then
    echo -e "\n${GREEN}✅ ÉXITO DE LA PRUEBA DE SEGURIDAD${NC}"
    echo "El sistema rechazó el intento de voto de la Empresa."
    echo "Posible causa: El Chaincode detectó que el MSPID 'OrdererMSP' no está autorizado."
else
    # Si esto ocurre, significa que tu Chaincode NO está validando quién llama.
    # ¡Sería un hallazgo de seguridad importante para reportar en el TFG!
    echo -e "\n${RED}⚠️  ALERTA: La transacción fue procesada con éxito.${NC}"
    echo "Esto indica que el Chaincode permite votar a la identidad 'OrdererMSP'."
    echo "Recomendación: Añadir validación de MSPID en el Smart Contract."
fi
