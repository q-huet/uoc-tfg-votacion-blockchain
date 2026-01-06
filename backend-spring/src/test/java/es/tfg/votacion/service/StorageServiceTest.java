package es.tfg.votacion.service;

import es.tfg.votacion.config.StorageProperties;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para StorageService
 * 
 * Verifica:
 * - Cifrado y descifrado AES-GCM
 * - Almacenamiento en sistema de archivos
 * - Carga de claves desde keystore
 * - Operaciones CRUD de blobs
 * - Integridad de datos
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "storage.base-path=/tmp/votacion-test-storage",
    "storage.keystore-path=src/main/resources/keystore/keystore.json",
    "storage.encryption.algorithm=AES/GCM/NoPadding",
    "storage.encryption.key-length=256",
    "storage.encryption.iv-length=12",
    "storage.encryption.tag-length=128",
    "storage.cleanup.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageServiceTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageProperties storageProperties;

    private static final String TEST_ELECTION_ID = "test-election-001";
    private static final String TEST_DATA = "This is a secret vote for candidate A";
    private static Path testStoragePath;

    @BeforeAll
    static void setupAll() {
        testStoragePath = Paths.get("/tmp/votacion-test-storage");
    }

    @AfterEach
    void cleanup() throws IOException {
        // Limpiar el directorio de prueba después de cada test
        if (Files.exists(testStoragePath)) {
            try (Stream<Path> paths = Files.walk(testStoragePath)) {
                paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignorar errores de limpieza
                        }
                    });
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Storage properties should be injected correctly")
    void storagePropertiesShouldBeInjected() {
        assertNotNull(storageProperties, "Storage properties should be injected");
        assertNotNull(storageProperties.getBasePath(), "Base path should be configured");
        assertNotNull(storageProperties.getKeystorePath(), "Keystore path should be configured");
        
        assertEquals("AES/GCM/NoPadding", storageProperties.getEncryption().getAlgorithm());
        assertEquals(256, storageProperties.getEncryption().getKeyLength());
        assertEquals(12, storageProperties.getEncryption().getIvLength());
        assertEquals(128, storageProperties.getEncryption().getTagLength());
    }

    @Test
    @Order(2)
    @DisplayName("Storage service should initialize successfully")
    void storageServiceShouldInitialize() {
        assertNotNull(storageService, "Storage service should be initialized");
        // El directorio se crea bajo demanda o ya existe, pero no lo verificamos aquí
        // porque el @AfterEach lo limpia. Lo verificaremos en tests que lo usan.
    }

    @Test
    @Order(3)
    @DisplayName("Should encrypt and store data successfully")
    void shouldEncryptAndStoreData() {
        // Arrange
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        
        // Act
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        
        // Assert
        assertNotNull(blobId, "Blob ID should not be null");
        assertTrue(blobId.startsWith("BLOB-"), "Blob ID should have correct prefix");
        assertTrue(blobId.contains(TEST_ELECTION_ID), "Blob ID should contain election ID");
        assertTrue(storageService.exists(blobId), "Blob should exist in storage");
    }

    @Test
    @Order(4)
    @DisplayName("Should load and decrypt data successfully")
    void shouldLoadAndDecryptData() {
        // Arrange
        byte[] originalData = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, originalData);
        
        // Act
        byte[] decryptedData = storageService.loadDecrypted(blobId);
        
        // Assert
        assertNotNull(decryptedData, "Decrypted data should not be null");
        assertArrayEquals(originalData, decryptedData, "Decrypted data should match original");
        
        String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);
        assertEquals(TEST_DATA, decryptedString, "Decrypted string should match original");
    }

    @Test
    @Order(5)
    @DisplayName("Should handle encryption and decryption of binary data")
    void shouldHandleBinaryData() {
        // Arrange - crear datos binarios aleatorios
        byte[] binaryData = new byte[1024];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i % 256);
        }
        
        // Act
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, binaryData);
        byte[] decryptedData = storageService.loadDecrypted(blobId);
        
        // Assert
        assertArrayEquals(binaryData, decryptedData, "Binary data should be preserved");
    }

    @Test
    @Order(6)
    @DisplayName("Should handle encryption of large data")
    void shouldHandleLargeData() {
        // Arrange - crear un string grande (10KB)
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("This is line ").append(i).append(" of test data.\n");
        }
        byte[] data = largeData.toString().getBytes(StandardCharsets.UTF_8);
        
        // Act
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        byte[] decryptedData = storageService.loadDecrypted(blobId);
        
        // Assert
        assertArrayEquals(data, decryptedData, "Large data should be preserved");
        assertTrue(data.length > 10000, "Test data should be large");
    }

    @Test
    @Order(7)
    @DisplayName("Should delete blob successfully")
    void shouldDeleteBlob() {
        // Arrange
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        
        assertTrue(storageService.exists(blobId), "Blob should exist before deletion");
        
        // Act
        boolean deleted = storageService.delete(blobId);
        
        // Assert
        assertTrue(deleted, "Delete operation should return true");
        assertFalse(storageService.exists(blobId), "Blob should not exist after deletion");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle deletion of non-existent blob")
    void shouldHandleNonExistentBlobDeletion() {
        // Act
        boolean deleted = storageService.delete("non-existent-blob-id");
        
        // Assert
        assertFalse(deleted, "Delete operation should return false for non-existent blob");
    }

    @Test
    @Order(9)
    @DisplayName("Should check blob existence correctly")
    void shouldCheckBlobExistence() {
        // Arrange
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        
        // Assert
        assertTrue(storageService.exists(blobId), "Existing blob should return true");
        assertFalse(storageService.exists("non-existent-blob"), "Non-existent blob should return false");
        assertFalse(storageService.exists(null), "Null blob ID should return false");
        assertFalse(storageService.exists(""), "Empty blob ID should return false");
    }

    @Test
    @Order(10)
    @DisplayName("Should store multiple blobs for same election")
    void shouldStoreMultipleBlobsForSameElection() {
        // Arrange
        String data1 = "Vote for candidate A";
        String data2 = "Vote for candidate B";
        String data3 = "Vote for candidate C";
        
        // Act
        String blobId1 = storageService.storeEncrypted(TEST_ELECTION_ID, data1.getBytes(StandardCharsets.UTF_8));
        String blobId2 = storageService.storeEncrypted(TEST_ELECTION_ID, data2.getBytes(StandardCharsets.UTF_8));
        String blobId3 = storageService.storeEncrypted(TEST_ELECTION_ID, data3.getBytes(StandardCharsets.UTF_8));
        
        // Assert
        assertNotEquals(blobId1, blobId2, "Blob IDs should be unique");
        assertNotEquals(blobId2, blobId3, "Blob IDs should be unique");
        assertNotEquals(blobId1, blobId3, "Blob IDs should be unique");
        
        assertTrue(storageService.exists(blobId1));
        assertTrue(storageService.exists(blobId2));
        assertTrue(storageService.exists(blobId3));
        
        // Verificar que se pueden recuperar correctamente
        assertEquals(data1, new String(storageService.loadDecrypted(blobId1), StandardCharsets.UTF_8));
        assertEquals(data2, new String(storageService.loadDecrypted(blobId2), StandardCharsets.UTF_8));
        assertEquals(data3, new String(storageService.loadDecrypted(blobId3), StandardCharsets.UTF_8));
    }

    @Test
    @Order(11)
    @DisplayName("Should throw exception for null data")
    void shouldThrowExceptionForNullData() {
        assertThrows(IllegalArgumentException.class, () -> {
            storageService.storeEncrypted(TEST_ELECTION_ID, null);
        }, "Should throw exception for null data");
    }

    @Test
    @Order(12)
    @DisplayName("Should throw exception for empty data")
    void shouldThrowExceptionForEmptyData() {
        assertThrows(IllegalArgumentException.class, () -> {
            storageService.storeEncrypted(TEST_ELECTION_ID, new byte[0]);
        }, "Should throw exception for empty data");
    }

    @Test
    @Order(13)
    @DisplayName("Should throw exception for null blob ID on load")
    void shouldThrowExceptionForNullBlobIdOnLoad() {
        assertThrows(IllegalArgumentException.class, () -> {
            storageService.loadDecrypted(null);
        }, "Should throw exception for null blob ID");
    }

    @Test
    @Order(14)
    @DisplayName("Should throw exception when loading non-existent blob")
    void shouldThrowExceptionForNonExistentBlob() {
        assertThrows(RuntimeException.class, () -> {
            storageService.loadDecrypted("non-existent-blob-id");
        }, "Should throw exception when loading non-existent blob");
    }

    @Test
    @Order(15)
    @DisplayName("Encrypted data should be different from original")
    void encryptedDataShouldBeDifferent() throws IOException {
        // Arrange
        byte[] originalData = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        
        // Act
        String blobId = storageService.storeEncrypted(TEST_ELECTION_ID, originalData);
        
        // Leer el archivo cifrado directamente del disco
        Path blobPath = findBlobPath(blobId);
        assertNotNull(blobPath, "Blob file should exist");
        
        byte[] encryptedData = Files.readAllBytes(blobPath);
        
        // Assert
        assertNotEquals(originalData.length, encryptedData.length, 
            "Encrypted data length should be different (includes IV and tag)");
        
        // Verificar que el contenido cifrado no contiene el texto original
        String encryptedString = new String(encryptedData, StandardCharsets.UTF_8);
        assertFalse(encryptedString.contains(TEST_DATA), 
            "Encrypted data should not contain original plaintext");
    }

    @Test
    @Order(16)
    @DisplayName("Each encryption should produce different ciphertext (different IV)")
    void eachEncryptionShouldProduceDifferentCiphertext() throws IOException {
        // Arrange
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        
        // Act - cifrar el mismo dato dos veces
        String blobId1 = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        String blobId2 = storageService.storeEncrypted(TEST_ELECTION_ID, data);
        
        Path blobPath1 = findBlobPath(blobId1);
        Path blobPath2 = findBlobPath(blobId2);
        
        byte[] encrypted1 = Files.readAllBytes(blobPath1);
        byte[] encrypted2 = Files.readAllBytes(blobPath2);
        
        // Assert
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2), 
            "Same data encrypted twice should produce different ciphertext due to different IVs");
        
        // Pero ambos deben descifrar al mismo valor original
        byte[] decrypted1 = storageService.loadDecrypted(blobId1);
        byte[] decrypted2 = storageService.loadDecrypted(blobId2);
        
        assertArrayEquals(data, decrypted1);
        assertArrayEquals(data, decrypted2);
    }

    /**
     * Método auxiliar para encontrar la ruta de un blob en el sistema de archivos
     */
    private Path findBlobPath(String blobId) throws IOException {
        try (Stream<Path> paths = Files.walk(testStoragePath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().equals(blobId + ".enc"))
                .findFirst()
                .orElse(null);
        }
    }
}
