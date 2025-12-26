#!/bin/bash

# Script para desencriptar votos usando herramientas de consola y Python para AES-GCM
# Requisitos: jq, openssl, python3, pip install cryptography

if [ "$#" -ne 2 ]; then
    echo "Uso: $0 <archivo_blob> <clave_privada_pem>"
    exit 1
fi

BLOB_FILE="$1"
PRIVATE_KEY="$2"
KEYSTORE_FILE="backend-spring/src/main/resources/keystore/keystore.json"

# Verificar dependencias
if ! command -v jq &> /dev/null; then
    echo "Error: jq no está instalado. (sudo apt-get install jq)"
    exit 1
fi

if ! command -v openssl &> /dev/null; then
    echo "Error: openssl no está instalado."
    exit 1
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Error: No se encuentra el keystore en $KEYSTORE_FILE"
    exit 1
fi

# 1. Obtener la clave maestra del keystore
echo "Leyendo clave maestra..."
MASTER_KEY_B64=$(jq -r .masterKey "$KEYSTORE_FILE")

# 2. Desencriptar el BLOB (AES-GCM)
# Nota: OpenSSL CLI tiene soporte limitado para AES-GCM, por lo que usamos un one-liner de Python
echo "Desencriptando capa de almacenamiento (AES-GCM)..."
DECRYPTED_JSON=$(python3 -c "
import sys, base64
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

try:
    key = base64.b64decode('$MASTER_KEY_B64')
    with open('$BLOB_FILE', 'rb') as f:
        data = f.read()
    
    iv = data[:12]
    ciphertext = data[12:]
    
    aesgcm = AESGCM(key)
    plaintext = aesgcm.decrypt(iv, ciphertext, None)
    print(plaintext.decode('utf-8'))
except Exception as e:
    sys.stderr.write(str(e))
    sys.exit(1)
")

if [ $? -ne 0 ]; then
    echo "Error al desencriptar el BLOB AES-GCM."
    exit 1
fi

# 3. Extraer el voto encriptado (RSA) del JSON
ENCRYPTED_VOTE_B64=$(echo "$DECRYPTED_JSON" | jq -r .encryptedVote)

if [ "$ENCRYPTED_VOTE_B64" == "null" ]; then
    echo "El BLOB no contiene un voto encriptado. Contenido:"
    echo "$DECRYPTED_JSON" | jq .
else
    echo "Voto encriptado encontrado."
    echo "Desencriptando capa de voto (RSA) con OpenSSL..."
    
    # 4. Desencriptar RSA usando OpenSSL
    # Decodificamos Base64 y pasamos a openssl pkeyutl
    echo "$ENCRYPTED_VOTE_B64" | base64 -d | openssl pkeyutl -decrypt -inkey "$PRIVATE_KEY" -pkeyopt rsa_padding_mode:pkcs1
    
    echo "" # Nueva línea al final
fi
