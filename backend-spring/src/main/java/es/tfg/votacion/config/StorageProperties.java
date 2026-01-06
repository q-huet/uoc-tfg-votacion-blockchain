package es.tfg.votacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración de almacenamiento cifrado
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "storage")
@Validated
public class StorageProperties {

    private String basePath;
    private String keystorePath;
    private Encryption encryption = new Encryption();
    private Cleanup cleanup = new Cleanup();

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public Cleanup getCleanup() {
        return cleanup;
    }

    public void setCleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
    }

    public static class Encryption {
        private String algorithm = "AES/GCM/NoPadding";
        private int keyLength = 256;
        private int ivLength = 12;
        private int tagLength = 128;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public void setKeyLength(int keyLength) {
            this.keyLength = keyLength;
        }

        public int getIvLength() {
            return ivLength;
        }

        public void setIvLength(int ivLength) {
            this.ivLength = ivLength;
        }

        public int getTagLength() {
            return tagLength;
        }

        public void setTagLength(int tagLength) {
            this.tagLength = tagLength;
        }
    }

    public static class Cleanup {
        private boolean enabled = true;
        private int retentionDays = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }
}