package es.tfg.votacion.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Recibo de voto procesado
 * 
 * Representa el comprobante que recibe un usuario después de votar
 * exitosamente.
 * Contiene información que permite verificar que el voto fue registrado
 * correctamente
 * en el blockchain sin revelar por quién se votó.
 * 
 * @param receiptId        Identificador único del recibo
 * @param electionId       ID de la elección
 * @param userId           ID del usuario que votó
 * @param transactionId    ID de la transacción en el blockchain
 * @param blockHash        Hash del bloque que contiene la transacción
 * @param processedAt      Momento en que se procesó el voto
 * @param verificationCode Código para verificar el voto sin revelar contenido
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
public record Receipt(

        @NotBlank(message = "Receipt ID is required") @Size(min = 1, max = 64, message = "Receipt ID must be between 1 and 64 characters") @JsonProperty("receiptId") String receiptId,

        @NotBlank(message = "Election ID is required") @Size(min = 1, max = 50, message = "Election ID must be between 1 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Election ID must contain only alphanumeric characters, hyphens, and underscores") @JsonProperty("electionId") String electionId,

        @NotBlank(message = "User ID is required") @Size(min = 1, max = 50, message = "User ID must be between 1 and 50 characters") @JsonProperty("userId") String userId,

        @NotBlank(message = "Transaction ID is required") @Size(min = 1, max = 128, message = "Transaction ID must be between 1 and 128 characters") @JsonProperty("transactionId") String transactionId,

        @Size(min = 1, max = 128, message = "Block hash must be between 1 and 128 characters if provided") @JsonProperty("blockHash") String blockHash,

        @NotNull(message = "Processed timestamp is required") @JsonProperty("processedAt") LocalDateTime processedAt,

        @NotBlank(message = "Verification code is required") @Size(min = 8, max = 32, message = "Verification code must be between 8 and 32 characters") @Pattern(regexp = "^[A-Z0-9]+$", message = "Verification code must contain only uppercase letters and numbers") @JsonProperty("verificationCode") String verificationCode

) {

    /**
     * Constructor con validación
     */
    public Receipt {
        // Normalización
        if (receiptId != null) {
            receiptId = receiptId.trim();
        }

        if (electionId != null) {
            electionId = electionId.trim().toLowerCase();
        }

        if (userId != null) {
            userId = userId.trim().toLowerCase();
        }

        if (transactionId != null) {
            transactionId = transactionId.trim();
        }

        if (blockHash != null) {
            blockHash = blockHash.trim();
            // Si está vacío después del trim, lo convertimos a null
            if (blockHash.isEmpty()) {
                blockHash = null;
            }
        }

        if (verificationCode != null) {
            verificationCode = verificationCode.trim().toUpperCase();
        }

        // Validaciones adicionales
        if (electionId != null && !electionId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Election ID contains invalid characters");
        }

        if (verificationCode != null && !verificationCode.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Verification code must contain only uppercase letters and numbers");
        }

        // Validar que el timestamp no sea futuro
        if (processedAt != null && processedAt.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("Processed timestamp cannot be in the future");
        }

        // Validar formato de IDs hexadecimales
        if (transactionId != null && !isValidHexString(transactionId)) {
            throw new IllegalArgumentException("Transaction ID must be a valid hexadecimal string");
        }

        if (blockHash != null && !isValidHexString(blockHash)) {
            throw new IllegalArgumentException("Block hash must be a valid hexadecimal string");
        }
    }

    /**
     * Constructor para deserialización JSON
     */
    @JsonCreator
    public static Receipt create(
            @JsonProperty("receiptId") String receiptId,
            @JsonProperty("electionId") String electionId,
            @JsonProperty("userId") String userId,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("blockHash") String blockHash,
            @JsonProperty("processedAt") LocalDateTime processedAt,
            @JsonProperty("verificationCode") String verificationCode) {
        return new Receipt(receiptId, electionId, userId, transactionId, blockHash, processedAt, verificationCode);
    }

    /**
     * Factory method para crear recibo básico
     * 
     * @param electionId       ID de la elección
     * @param userId           ID del usuario
     * @param transactionId    ID de la transacción blockchain
     * @param verificationCode Código de verificación
     * @return Nueva instancia de Receipt con timestamp actual
     */
    public static Receipt of(String electionId, String userId, String transactionId, String verificationCode) {
        String receiptId = generateReceiptId(electionId, userId, transactionId);
        return new Receipt(receiptId, electionId, userId, transactionId, null, LocalDateTime.now(), verificationCode);
    }

    /**
     * Factory method para crear recibo completo
     * 
     * @param electionId       ID de la elección
     * @param userId           ID del usuario
     * @param transactionId    ID de la transacción blockchain
     * @param blockHash        Hash del bloque
     * @param verificationCode Código de verificación
     * @return Nueva instancia de Receipt
     */
    public static Receipt withBlockHash(String electionId, String userId, String transactionId,
            String blockHash, String verificationCode) {
        String receiptId = generateReceiptId(electionId, userId, transactionId);
        return new Receipt(receiptId, electionId, userId, transactionId, blockHash, LocalDateTime.now(),
                verificationCode);
    }

    /**
     * Verifica si el recibo tiene hash de bloque
     * 
     * @return true si tiene blockHash
     */
    public boolean hasBlockHash() {
        return blockHash != null && !blockHash.isBlank();
    }

    /**
     * Verifica si el recibo está completamente procesado
     * Un recibo está completo si tiene tanto transactionId como blockHash
     * 
     * @return true si está completamente procesado
     */
    public boolean isCompletelyProcessed() {
        return transactionId != null && !transactionId.isBlank() && hasBlockHash();
    }

    /**
     * Crea una copia con hash de bloque
     * 
     * @param newBlockHash Nuevo hash del bloque
     * @return Nueva instancia con el blockHash actualizado
     */
    public Receipt withBlockHash(String newBlockHash) {
        return new Receipt(receiptId, electionId, userId, transactionId, newBlockHash, processedAt, verificationCode);
    }

    /**
     * Crea una copia con nuevo timestamp de procesamiento
     * 
     * @param newProcessedAt Nuevo timestamp
     * @return Nueva instancia con el timestamp actualizado
     */
    public Receipt withProcessedAt(LocalDateTime newProcessedAt) {
        return new Receipt(receiptId, electionId, userId, transactionId, blockHash, newProcessedAt, verificationCode);
    }

    /**
     * Obtiene una representación resumida del recibo para mostrar al usuario
     * 
     * @return String con información clave del recibo
     */
    public String getSummary() {
        return String.format("Recibo: %s | Elección: %s | Procesado: %s | Código: %s",
                receiptId.substring(0, Math.min(8, receiptId.length())) + "...",
                electionId,
                processedAt.toLocalDate(),
                verificationCode);
    }

    /**
     * Genera un ID único para el recibo
     * 
     * @param electionId    ID de la elección
     * @param userId        ID del usuario
     * @param transactionId ID de la transacción
     * @return ID único del recibo
     */
    private static String generateReceiptId(String electionId, String userId, String transactionId) {
        // Generar un ID único basado en los inputs
        String combined = electionId + ":" + userId + ":" + transactionId + ":" + System.currentTimeMillis();
        return "RCP-" + Integer.toHexString(combined.hashCode()).toUpperCase();
    }

    /**
     * Verifica si una cadena es hexadecimal válida
     * 
     * @param str Cadena a verificar
     * @return true si es hexadecimal válida
     */
    private static boolean isValidHexString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // Remover prefijo 0x si existe
        String hexStr = str.startsWith("0x") ? str.substring(2) : str;

        return hexStr.matches("^[0-9a-fA-F]+$");
    }

    /**
     * Representación para logging (sin información sensible)
     */
    @Override
    public String toString() {
        return String.format("Receipt{id='%s', electionId='%s', userId='%s', txId='%s...', processed=%s}",
                receiptId,
                electionId,
                userId,
                transactionId != null && transactionId.length() > 8 ? transactionId.substring(0, 8) : transactionId,
                processedAt);
    }
}