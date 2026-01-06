package es.tfg.votacion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Servicio de criptografía para gestión de claves RSA y cifrado/descifrado.
 * Soporta el modelo de "Cold Storage" donde la clave privada no se almacena en el servidor
 * de forma persistente, sino que se inyecta solo durante el recuento.
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Service
public class CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    /**
     * Genera un nuevo par de claves RSA para una elección.
     * @return KeyPair con claves pública y privada.
     */
    public KeyPair generateKeyPair() {
        try {
            logger.info("Generating new RSA key pair of size {}", KEY_SIZE);
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            logger.error("RSA algorithm not found", e);
            throw new RuntimeException("Error initializing RSA key generator", e);
        }
    }

    /**
     * Convierte una clave pública a formato PEM (String Base64).
     * @param publicKey La clave pública.
     * @return String codificado en Base64.
     */
    public String publicKeyToPem(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Convierte una clave privada a formato PEM (String Base64).
     * @param privateKey La clave privada.
     * @return String codificado en Base64.
     */
    public String privateKeyToPem(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Reconstruye una clave privada desde su formato PEM (Base64).
     * @param privateKeyPem String de la clave privada en Base64.
     * @return Objeto PrivateKey.
     */
    public PrivateKey pemToPrivateKey(String privateKeyPem) {
        try {
            // Limpiar cabeceras si vinieran incluidas (aunque aquí usamos raw base64)
            String cleanKey = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            logger.error("Failed to parse private key PEM", e);
            throw new RuntimeException("Error parsing private key", e);
        }
    }
    
    /**
     * Reconstruye una clave pública desde su formato PEM (Base64).
     * @param publicKeyPem String de la clave pública en Base64.
     * @return Objeto PublicKey.
     */
    public PublicKey pemToPublicKey(String publicKeyPem) {
        try {
            String cleanKey = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return kf.generatePublic(spec);
        } catch (Exception e) {
            logger.error("Failed to parse public key PEM", e);
            throw new RuntimeException("Error parsing public key", e);
        }
    }

    /**
     * Descifra un mensaje cifrado usando la clave privada.
     * @param encryptedMessageBase64 Mensaje cifrado en Base64.
     * @param privateKeyPem Clave privada en formato PEM.
     * @return Mensaje descifrado en texto plano.
     */
    public String decrypt(String encryptedMessageBase64, String privateKeyPem) {
        try {
            PrivateKey privateKey = pemToPrivateKey(privateKeyPem);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessageBase64);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Decryption failed for a vote", e);
            throw new RuntimeException("Error decrypting vote. Check if the private key is correct.", e);
        }
    }
    
    /**
     * Cifra un mensaje usando la clave pública (Utilidad para tests o simulaciones).
     * @param message Mensaje en texto plano.
     * @param publicKeyPem Clave pública en formato PEM.
     * @return Mensaje cifrado en Base64.
     */
    public String encrypt(String message, String publicKeyPem) {
        try {
            PublicKey publicKey = pemToPublicKey(publicKeyPem);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new RuntimeException("Error encrypting message", e);
        }
    }
}
