package es.tfg.votacion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.tfg.votacion.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Servicio para almacenamiento cifrado off-chain
 * 
 * Gestiona:
 * - Cifrado/descifrado AES-GCM de blobs de voto
 * - Almacenamiento en sistema de archivos
 * - Gestión de claves desde keystore
 * - Limpieza automática de archivos antiguos
 * 
 * Formato de archivo cifrado:
 * [IV (12 bytes)][Datos cifrados con tag de autenticación (16 bytes)]
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;
    
    private SecretKey masterKey;
    private Path storageBasePath;

    @Autowired
    public StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.objectMapper = new ObjectMapper();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Inicialización del servicio
     * Carga la clave de cifrado y verifica el directorio de almacenamiento
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Storage Service with AES-GCM encryption");
        
        try {
            // Cargar clave de cifrado desde keystore
            this.masterKey = loadEncryptionKey();
            logger.info("Encryption key loaded successfully from keystore");
            
            // Configurar directorio de almacenamiento
            this.storageBasePath = Paths.get(storageProperties.getBasePath());
            Files.createDirectories(storageBasePath);
            logger.info("Storage directory initialized: {}", storageBasePath.toAbsolutePath());
            
            // Verificar configuración de cifrado
            logger.debug("Encryption algorithm: {}", storageProperties.getEncryption().getAlgorithm());
            logger.debug("Key length: {} bits", storageProperties.getEncryption().getKeyLength());
            logger.debug("IV length: {} bytes", storageProperties.getEncryption().getIvLength());
            logger.debug("Tag length: {} bits", storageProperties.getEncryption().getTagLength());
            
            // Verificar si la limpieza automática está habilitada
            if (storageProperties.getCleanup().isEnabled()) {
                logger.info("Automatic cleanup enabled - retention: {} days", 
                    storageProperties.getCleanup().getRetentionDays());
            } else {
                logger.warn("Automatic cleanup is disabled");
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize Storage Service: {}", e.getMessage(), e);
            throw new RuntimeException("Storage Service initialization failed", e);
        }
    }

    /**
     * Almacena datos cifrados en el sistema de archivos
     * 
     * @param electionId ID de la elección
     * @param data       Datos a cifrar y almacenar
     * @return ID único del blob almacenado
     * @throws RuntimeException si falla el cifrado o almacenamiento
     */
    public String storeEncrypted(String electionId, byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data to encrypt cannot be null or empty");
        }
        
        logger.info("Storing encrypted data for election: {} ({} bytes)", electionId, data.length);
        
        try {
            // Generar ID único para el blob
            String blobId = generateBlobId(electionId);
            
            // Cifrar datos
            byte[] encryptedData = encryptData(data, masterKey);
            logger.debug("Data encrypted successfully - size: {} bytes", encryptedData.length);
            
            // Crear directorio de elección si no existe
            Path electionDir = storageBasePath.resolve(sanitizeElectionId(electionId));
            Files.createDirectories(electionDir);
            
            // Guardar en archivo
            Path blobPath = electionDir.resolve(blobId + ".enc");
            Files.write(blobPath, encryptedData);
            logger.info("Encrypted blob stored successfully: {}", blobPath.getFileName());
            
            // Calcular y loggear el hash del blob cifrado (para auditoría)
            String blobHash = calculateSHA256(encryptedData);
            logger.debug("Blob SHA-256 hash: {}", blobHash);
            
            return blobId;
            
        } catch (Exception e) {
            logger.error("Failed to store encrypted data for election {}: {}", electionId, e.getMessage(), e);
            throw new RuntimeException("Failed to store encrypted data", e);
        }
    }

    /**
     * Recupera y descifra datos del almacenamiento
     * 
     * @param blobId ID del blob a recuperar
     * @return Datos descifrados originales
     * @throws RuntimeException si falla la carga o descifrado
     */
    public byte[] loadDecrypted(String blobId) {
        if (blobId == null || blobId.isBlank()) {
            throw new IllegalArgumentException("Blob ID cannot be null or empty");
        }
        
        logger.info("Loading and decrypting blob: {}", blobId);
        
        try {
            // Buscar el archivo en los directorios de elecciones
            Path blobPath = findBlobPath(blobId);
            
            if (blobPath == null || !Files.exists(blobPath)) {
                logger.error("Blob not found: {}", blobId);
                throw new RuntimeException("Blob not found: " + blobId);
            }
            
            // Leer datos cifrados
            byte[] encryptedData = Files.readAllBytes(blobPath);
            logger.debug("Encrypted blob loaded - size: {} bytes", encryptedData.length);
            
            // Descifrar datos
            byte[] decryptedData = decryptData(encryptedData, masterKey);
            logger.info("Blob decrypted successfully: {} ({} bytes)", blobId, decryptedData.length);
            
            return decryptedData;
            
        } catch (Exception e) {
            logger.error("Failed to load and decrypt blob {}: {}", blobId, e.getMessage(), e);
            throw new RuntimeException("Failed to load decrypted data", e);
        }
    }

    /**
     * Elimina un blob del almacenamiento
     * 
     * @param blobId ID del blob a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean delete(String blobId) {
        if (blobId == null || blobId.isBlank()) {
            logger.warn("Attempted to delete blob with null or empty ID");
            return false;
        }
        
        logger.info("Deleting blob: {}", blobId);
        
        try {
            Path blobPath = findBlobPath(blobId);
            
            if (blobPath == null || !Files.exists(blobPath)) {
                logger.warn("Blob not found for deletion: {}", blobId);
                return false;
            }
            
            // Sobrescribir con datos aleatorios antes de eliminar (borrado seguro)
            long fileSize = Files.size(blobPath);
            byte[] randomData = new byte[(int) fileSize];
            secureRandom.nextBytes(randomData);
            Files.write(blobPath, randomData);
            
            // Eliminar el archivo
            Files.delete(blobPath);
            logger.info("Blob deleted securely: {}", blobId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to delete blob {}: {}", blobId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica si un blob existe en el almacenamiento
     * 
     * @param blobId ID del blob a verificar
     * @return true si existe
     */
    public boolean exists(String blobId) {
        if (blobId == null || blobId.isBlank()) {
            return false;
        }
        
        Path blobPath = findBlobPath(blobId);
        return blobPath != null && Files.exists(blobPath);
    }

    /**
     * Cifra datos usando AES-GCM
     * 
     * Formato del resultado:
     * [IV (12 bytes)][Datos cifrados + tag de autenticación (16 bytes)]
     * 
     * @param data Datos a cifrar
     * @param key  Clave de cifrado
     * @return Datos cifrados con IV y tag de autenticación
     * @throws Exception si falla el cifrado
     */
    private byte[] encryptData(byte[] data, SecretKey key) throws Exception {
        logger.debug("Encrypting data with AES-GCM - input size: {} bytes", data.length);
        
        // Generar IV aleatorio
        byte[] iv = new byte[storageProperties.getEncryption().getIvLength()];
        secureRandom.nextBytes(iv);
        
        // Configurar cifrador AES-GCM
        Cipher cipher = Cipher.getInstance(storageProperties.getEncryption().getAlgorithm());
        GCMParameterSpec parameterSpec = new GCMParameterSpec(
            storageProperties.getEncryption().getTagLength(), 
            iv
        );
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        
        // Cifrar datos
        byte[] encryptedData = cipher.doFinal(data);
        
        // Combinar IV + datos cifrados + tag
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        
        byte[] result = byteBuffer.array();
        logger.debug("Encryption complete - output size: {} bytes (IV: {}, encrypted: {})", 
            result.length, iv.length, encryptedData.length);
        
        return result;
    }

    /**
     * Descifra datos usando AES-GCM
     * 
     * Espera formato:
     * [IV (12 bytes)][Datos cifrados + tag de autenticación (16 bytes)]
     * 
     * @param encryptedData Datos cifrados con IV y tag
     * @param key           Clave de descifrado
     * @return Datos originales descifrados
     * @throws Exception si falla el descifrado o la autenticación
     */
    private byte[] decryptData(byte[] encryptedData, SecretKey key) throws Exception {
        logger.debug("Decrypting data with AES-GCM - input size: {} bytes", encryptedData.length);
        
        int ivLength = storageProperties.getEncryption().getIvLength();
        
        if (encryptedData.length < ivLength) {
            throw new IllegalArgumentException("Encrypted data is too short to contain IV");
        }
        
        // Extraer IV
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        
        // Extraer datos cifrados
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);
        
        // Configurar cifrador AES-GCM
        Cipher cipher = Cipher.getInstance(storageProperties.getEncryption().getAlgorithm());
        GCMParameterSpec parameterSpec = new GCMParameterSpec(
            storageProperties.getEncryption().getTagLength(), 
            iv
        );
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        
        // Descifrar datos (el tag se verifica automáticamente)
        byte[] decryptedData = cipher.doFinal(cipherText);
        logger.debug("Decryption complete - output size: {} bytes", decryptedData.length);
        
        return decryptedData;
    }

    /**
     * Carga la clave de cifrado desde el keystore
     * 
     * @return Clave de cifrado AES
     * @throws Exception si falla la carga del keystore
     */
    private SecretKey loadEncryptionKey() throws Exception {
        logger.debug("Loading encryption key from keystore: {}", storageProperties.getKeystorePath());
        
        Path keystorePath = Paths.get(storageProperties.getKeystorePath());
        
        if (!Files.exists(keystorePath)) {
            throw new IOException("Keystore file not found: " + keystorePath);
        }
        
        // Leer y parsear keystore JSON
        JsonNode keystoreJson = objectMapper.readTree(keystorePath.toFile());
        
        String masterKeyBase64 = keystoreJson.get("masterKey").asText();
        String algorithm = keystoreJson.get("algorithm").asText();
        int keyLength = keystoreJson.get("keyLength").asInt();
        
        logger.debug("Keystore loaded - algorithm: {}, key length: {} bits", algorithm, keyLength);
        
        // Decodificar clave desde Base64
        byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
        
        if (keyBytes.length != keyLength / 8) {
            throw new IllegalArgumentException(
                String.format("Key length mismatch: expected %d bytes, got %d bytes", 
                    keyLength / 8, keyBytes.length)
            );
        }
        
        // Crear SecretKey
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        logger.info("Encryption key loaded and validated successfully");
        
        return key;
    }

    /**
     * Genera un ID único para un blob
     * 
     * @param electionId ID de la elección
     * @return ID único del blob
     */
    private String generateBlobId(String electionId) {
        String uuid = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        return String.format("BLOB-%s-%d-%s", 
            sanitizeElectionId(electionId), 
            timestamp, 
            uuid.substring(0, 8));
    }

    /**
     * Sanitiza el ID de elección para usar en nombres de archivo
     * 
     * @param electionId ID de elección
     * @return ID sanitizado
     */
    private String sanitizeElectionId(String electionId) {
        return electionId.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Busca la ruta de un blob en el sistema de archivos
     * 
     * @param blobId ID del blob
     * @return Ruta del archivo o null si no se encuentra
     */
    private Path findBlobPath(String blobId) {
        try {
            // Buscar en todos los subdirectorios
            try (Stream<Path> paths = Files.walk(storageBasePath)) {
                return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(blobId + ".enc"))
                    .findFirst()
                    .orElse(null);
            }
        } catch (IOException e) {
            logger.error("Error searching for blob {}: {}", blobId, e.getMessage());
            return null;
        }
    }

    /**
     * Calcula el hash SHA-256 de los datos
     * 
     * @param data Datos a hashear
     * @return Hash en formato hexadecimal
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Failed to calculate SHA-256 hash: {}", e.getMessage());
            return "HASH-ERROR";
        }
    }

    /**
     * Ejecuta limpieza automática de archivos antiguos
     * Basado en la configuración retention_days
     * 
     * Se ejecuta diariamente a las 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void performCleanup() {
        if (!storageProperties.getCleanup().isEnabled()) {
            logger.debug("Automatic cleanup is disabled, skipping");
            return;
        }
        
        logger.info("Starting automatic cleanup of old storage files");
        
        try {
            int retentionDays = storageProperties.getCleanup().getRetentionDays();
            long cutoffTime = Instant.now()
                .minusSeconds(retentionDays * 24L * 60 * 60)
                .toEpochMilli();
            
            logger.info("Cleaning up files older than {} days (cutoff: {})", 
                retentionDays, LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(cutoffTime), 
                    ZoneOffset.UTC));
            
            int deletedCount = 0;
            
            try (Stream<Path> paths = Files.walk(storageBasePath)) {
                var oldFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".enc"))
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
                
                for (Path oldFile : oldFiles) {
                    try {
                        Files.delete(oldFile);
                        deletedCount++;
                        logger.debug("Deleted old file: {}", oldFile.getFileName());
                    } catch (IOException e) {
                        logger.warn("Failed to delete old file {}: {}", 
                            oldFile.getFileName(), e.getMessage());
                    }
                }
            }
            
            logger.info("Cleanup completed - deleted {} old files", deletedCount);
            
        } catch (Exception e) {
            logger.error("Failed to perform cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup al destruir el servicio
     */
    @PreDestroy
    public void destroy() {
        logger.info("Shutting down Storage Service");
        
        // Limpiar la clave de memoria por seguridad
        if (masterKey != null) {
            try {
                // Java no permite destruir la clave directamente, pero podemos hacer null
                masterKey = null;
                logger.debug("Master key cleared from memory");
            } catch (Exception e) {
                logger.error("Error clearing master key: {}", e.getMessage());
            }
        }
    }
}