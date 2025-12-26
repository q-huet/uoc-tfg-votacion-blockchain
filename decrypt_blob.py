import json
import base64
import sys
import os
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives import hashes

def load_master_key(keystore_path):
    with open(keystore_path, 'r') as f:
        keystore = json.load(f)
    return base64.b64decode(keystore['masterKey'])

def decrypt_storage_blob(blob_path, master_key):
    with open(blob_path, 'rb') as f:
        encrypted_data = f.read()
    
    # IV is 12 bytes (from StorageProperties)
    iv = encrypted_data[:12]
    ciphertext = encrypted_data[12:]
    
    aesgcm = AESGCM(master_key)
    try:
        decrypted_data = aesgcm.decrypt(iv, ciphertext, None)
        return decrypted_data
    except Exception as e:
        print(f"Error decrypting blob with AES key: {e}")
        sys.exit(1)

def decrypt_vote_rsa(encrypted_vote_base64, private_key_path):
    with open(private_key_path, 'rb') as f:
        private_key = serialization.load_pem_private_key(
            f.read(),
            password=None
        )
    
    encrypted_vote = base64.b64decode(encrypted_vote_base64)
    
    try:
        # Java's Cipher.getInstance("RSA") defaults to RSA/ECB/PKCS1Padding
        original_message = private_key.decrypt(
            encrypted_vote,
            padding.PKCS1v15()
        )
        return original_message.decode('utf-8')
    except Exception as e:
        print(f"Error decrypting vote with RSA key: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 decrypt_blob.py <path_to_blob_file> <path_to_private_key_pem>")
        sys.exit(1)
        
    blob_path = sys.argv[1]
    private_key_path = sys.argv[2]
    
    # Path to the keystore in the workspace
    keystore_path = "backend-spring/src/main/resources/keystore/keystore.json"
    
    if not os.path.exists(keystore_path):
        print(f"Keystore not found at {keystore_path}")
        print("Please run this script from the root of the project.")
        sys.exit(1)
        
    print(f"Loading master key from {keystore_path}...")
    try:
        master_key = load_master_key(keystore_path)
    except Exception as e:
        print(f"Failed to load master key: {e}")
        sys.exit(1)
    
    print(f"Decrypting blob {blob_path}...")
    try:
        decrypted_blob_bytes = decrypt_storage_blob(blob_path, master_key)
    except Exception as e:
        print(f"Failed to decrypt blob: {e}")
        sys.exit(1)
    
    try:
        vote_data = json.loads(decrypted_blob_bytes)
        print("Blob decrypted successfully. Content:")
        print(json.dumps(vote_data, indent=2))
        
        if "encryptedVote" in vote_data:
            print("\nFound encrypted vote. Decrypting with private key...")
            encrypted_vote = vote_data["encryptedVote"]
            try:
                decrypted_vote = decrypt_vote_rsa(encrypted_vote, private_key_path)
                print("\n--- DECRYPTED VOTE ---")
                print(decrypted_vote)
                print("----------------------")
            except Exception as e:
                print(f"Failed to decrypt RSA vote: {e}")
        else:
            print("\nNo 'encryptedVote' field found in blob. It might be a plaintext vote.")
            
    except json.JSONDecodeError:
        print("Decrypted blob is not valid JSON.")
        print(decrypted_blob_bytes)
