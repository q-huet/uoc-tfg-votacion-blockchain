#!/bin/bash
# scripts/simulate-hack.sh
#
# SIMULACIÓN DE ATAQUE: Intento de voto fraudulento por parte de Org1
# ------------------------------------------------------------------
# Este script intenta invocar el chaincode 'EmitVote' utilizando ÚNICAMENTE
# el peer de Org1 (Sindicato A), ignorando al peer de Org2 (Sindicato B).
#
# Objetivo: Demostrar que la red rechaza la transacción por fallo en la
# política de aval (Endorsement Policy Failure).

export PATH=${PWD}/../fabric-samples/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../fabric-samples/config/

# Colores
RED='\033[0;31m'

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}=== 🕵️  INICIANDO SIMULACIÓN DE HACKEO (ORG1) ===${NC}"
echo "Escenario: El administrador de Org1 (Sindicato A) intenta insertar un voto falso sin el consenso de Org2 (Sindicato B)."

# 1. Definir variables de entorno para actuar como Org1
export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${PWD}/../fabric-samples/test-network/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
export PEER0_ORG1_CA=${PWD}/../fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/../fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051


# Datos del voto falso
FAKE_ELECTION_ID="election-hack-001"
FAKE_VOTE_HASH="HACKED_HASH_123456789"

echo -e "${BLUE}[1] Construyendo transacción fraudulenta...${NC}"
echo "Target: Solo Peer0.Org1 (Sindicato A)"
echo "Omitiendo: Peer0.Org2 (Sindicato B)"

# 2. Intentar invocar el chaincode apuntando SOLO a Org1
# Nota: En una operación normal, apuntaríamos a ambos peers.
set +e # Permitir que falle para capturar el error

peer chaincode invoke \
    -o localhost:7050 \
    --ordererTLSHostnameOverride orderer.example.com \
    --tls \
    --cafile "$ORDERER_CA" \
    -C electionchannel \
    -n electioncc \
    --peerAddresses localhost:7051 \
    --tlsRootCertFiles "$PEER0_ORG1_CA" \
    -c "{\"function\":\"EmitVote\",\"Args\":[\"$FAKE_ELECTION_ID\",\"$FAKE_VOTE_HASH\"]}" > scripts/logs/hack_attempt.log 2>&1

EXIT_CODE=$?

echo -e "${BLUE}[2] Resultado del intento:${NC}"
cat scripts/logs/hack_attempt.log

if [ $EXIT_CODE -ne 0 ]; then
    echo -e "\n${GREEN}✅ ÉXITO DE LA PRUEBA DE SEGURIDAD${NC}"
    echo "El sistema rechazó el intento de hackeo."
    echo "Razón probable: La política de aval requiere firmas de ambas organizaciones."
else
    echo -e "\n${RED}⚠️  ALERTA: La transacción fue enviada al Orderer.${NC}"
    echo "Esto no significa que sea válida. Revisa los logs del Peer o el Explorer."
    echo "Si la política es 'Mayoría' y solo hay 2 orgs, 1 firma no debería ser suficiente para validar el bloque."
fi

echo -e "\n${BLUE}=== Cómo verificar la evidencia ===${NC}"
echo "1. Revisa los logs del peer de Org2 (Sindicato):"
echo "   docker logs peer0.org2.example.com 2>&1 | grep -i 'validation'"
echo "2. Si usas Hyperledger Explorer, busca una transacción marcada como INVALID."
