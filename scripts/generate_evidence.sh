#!/bin/bash

# Directorio de salida
OUTPUT_DIR="evidence_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUTPUT_DIR"

echo "Generando evidencias técnicas de Hyperledger Fabric..."
echo "Directorio de salida: $OUTPUT_DIR"

# 1. Logs del Chaincode (Lógica de Negocio)
echo "1. Capturando logs del Chaincode (Smart Contract)..."
CC_CONTAINER=$(docker ps --format "{{.Names}}" | grep "dev-peer0.org1.example.com-electioncc")
if [ -n "$CC_CONTAINER" ]; then
    docker logs "$CC_CONTAINER" > "$OUTPUT_DIR/chaincode_logs.txt" 2>&1
    echo "   - Logs guardados en $OUTPUT_DIR/chaincode_logs.txt"
else
    echo "   - No se encontró el contenedor del chaincode."
fi

# 2. Logs del Peer (Validación y Commit de Bloques)
echo "2. Capturando logs de los Peers (Validación de Bloques)..."
# Peer Org1
docker logs peer0.org1.example.com > "$OUTPUT_DIR/peer0_org1_logs.txt" 2>&1
grep -i "committed block" "$OUTPUT_DIR/peer0_org1_logs.txt" > "$OUTPUT_DIR/peer0_org1_commits.txt"
# Peer Org2 (Importante para ver rechazos de validación si Org1 es el atacante)
docker logs peer0.org2.example.com > "$OUTPUT_DIR/peer0_org2_logs.txt" 2>&1
grep -i "committed block" "$OUTPUT_DIR/peer0_org2_logs.txt" > "$OUTPUT_DIR/peer0_org2_commits.txt"

# Buscar errores de validación (VSCC) que evidencien intentos de hackeo
echo "   - Buscando evidencias de transacciones inválidas..."
grep -i "VSCC error" "$OUTPUT_DIR/peer0_org1_logs.txt" "$OUTPUT_DIR/peer0_org2_logs.txt" > "$OUTPUT_DIR/validation_errors.txt" || true
grep -i "validation failed" "$OUTPUT_DIR/peer0_org1_logs.txt" "$OUTPUT_DIR/peer0_org2_logs.txt" >> "$OUTPUT_DIR/validation_errors.txt" || true

echo "   - Logs completos en $OUTPUT_DIR/peer0_org*_logs.txt"
echo "   - Resumen de commits en $OUTPUT_DIR/peer0_org*_commits.txt"
echo "   - Errores de validación en $OUTPUT_DIR/validation_errors.txt"

# 2.1 Capturar log del intento de hackeo si existe
if [ -f "scripts/logs/hack_attempt.log" ]; then
    echo "2.1 Capturando log de simulación de hackeo (Sindicato)..."
    cp scripts/logs/hack_attempt.log "$OUTPUT_DIR/hack_attempt_console.txt"
fi

# 2.2 Capturar log del intento de hackeo de la Empresa si existe
if [ -f "scripts/logs/hack_attempt_company.log" ]; then
    echo "2.2 Capturando log de simulación de hackeo (Empresa)..."
    cp scripts/logs/hack_attempt_company.log "$OUTPUT_DIR/hack_attempt_company_console.txt"
fi

# 3. Logs del Orderer (Creación de Bloques)
echo "3. Capturando logs del Orderer (Consenso)..."
docker logs orderer.example.com > "$OUTPUT_DIR/orderer_logs.txt" 2>&1
echo "   - Logs guardados en $OUTPUT_DIR/orderer_logs.txt"

# 4. Estado Mundial (World State) desde CouchDB
echo "4. Consultando el World State (CouchDB)..."
# Asumiendo que el canal es 'electionchannel' y el chaincode 'electioncc'
# La base de datos en CouchDB suele llamarse 'channelname_chaincodename'
# Usamos credenciales por defecto admin:adminpw
curl -s -u admin:adminpw "http://localhost:5984/electionchannel_electioncc/_all_docs?include_docs=true" | json_pp > "$OUTPUT_DIR/world_state.json"
echo "   - Estado actual guardado en $OUTPUT_DIR/world_state.json"

# 5. Inspección de Bloques (Requiere binarios de Fabric configurados, intentamos usar el contenedor cli si existe, sino saltamos)
echo "5. Intentando inspeccionar el último bloque..."
# Verificamos si tenemos acceso a 'peer' command localmente o via docker exec
if docker ps | grep -q "cli"; then
    echo "   - Usando contenedor CLI para obtener información del canal..."
    docker exec cli peer channel getinfo -c electionchannel > "$OUTPUT_DIR/channel_info.txt" 2>&1
    echo "   - Info del canal guardada en $OUTPUT_DIR/channel_info.txt"
else
    echo "   - Contenedor CLI no encontrado, omitiendo inspección profunda de bloques."
fi

echo "=================================================="
echo "Evidencias generadas exitosamente en $OUTPUT_DIR"
echo "=================================================="
