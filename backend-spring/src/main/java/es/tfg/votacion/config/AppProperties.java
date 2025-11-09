package es.tfg.votacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración de la aplicación
 * Mapea las propiedades del application.yaml a clases Java
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Fabric fabric = new Fabric();
    private Storage storage = new Storage();
    private Auth auth = new Auth();
    private Elections elections = new Elections();

    // Getters y Setters
    public Fabric getFabric() {
        return fabric;
    }

    public void setFabric(Fabric fabric) {
        this.fabric = fabric;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Elections getElections() {
        return elections;
    }

    public void setElections(Elections elections) {
        this.elections = elections;
    }

    /**
     * Configuración de Hyperledger Fabric
     */
    public static class Fabric {
        private String connectionProfile;
        private String walletPath;
        private String user;
        private String channel;
        private String chaincode;
        private Timeouts timeouts = new Timeouts();
        private Retry retry = new Retry();

        // Getters y Setters
        public String getConnectionProfile() {
            return connectionProfile;
        }

        public void setConnectionProfile(String connectionProfile) {
            this.connectionProfile = connectionProfile;
        }

        public String getWalletPath() {
            return walletPath;
        }

        public void setWalletPath(String walletPath) {
            this.walletPath = walletPath;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getChaincode() {
            return chaincode;
        }

        public void setChaincode(String chaincode) {
            this.chaincode = chaincode;
        }

        public Timeouts getTimeouts() {
            return timeouts;
        }

        public void setTimeouts(Timeouts timeouts) {
            this.timeouts = timeouts;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public static class Timeouts {
            private int connection = 30;
            private int transaction = 60;
            private int query = 10;

            public int getConnection() {
                return connection;
            }

            public void setConnection(int connection) {
                this.connection = connection;
            }

            public int getTransaction() {
                return transaction;
            }

            public void setTransaction(int transaction) {
                this.transaction = transaction;
            }

            public int getQuery() {
                return query;
            }

            public void setQuery(int query) {
                this.query = query;
            }
        }

        public static class Retry {
            private int maxAttempts = 3;
            private int backoffDelayMs = 1000;

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public int getBackoffDelayMs() {
                return backoffDelayMs;
            }

            public void setBackoffDelayMs(int backoffDelayMs) {
                this.backoffDelayMs = backoffDelayMs;
            }
        }
    }

    /**
     * Configuración de almacenamiento cifrado
     */
    public static class Storage {
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

    /**
     * Configuración de autenticación mock
     */
    public static class Auth {
        private String usersFile;
        private Jwt jwt = new Jwt();
        private String[] roles = { "voter", "admin", "auditor" };

        public String getUsersFile() {
            return usersFile;
        }

        public void setUsersFile(String usersFile) {
            this.usersFile = usersFile;
        }

        public Jwt getJwt() {
            return jwt;
        }

        public void setJwt(Jwt jwt) {
            this.jwt = jwt;
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }

        public static class Jwt {
            private String secret;
            private int expiration = 3600;
            private String issuer;
            private String audience;

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public int getExpiration() {
                return expiration;
            }

            public void setExpiration(int expiration) {
                this.expiration = expiration;
            }

            public String getIssuer() {
                return issuer;
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer;
            }

            public String getAudience() {
                return audience;
            }

            public void setAudience(String audience) {
                this.audience = audience;
            }
        }
    }

    /**
     * Configuración específica de elecciones
     */
    public static class Elections {
        private Default defaultConfig = new Default();
        private Validation validation = new Validation();

        public Default getDefaultConfig() {
            return defaultConfig;
        }

        public void setDefaultConfig(Default defaultConfig) {
            this.defaultConfig = defaultConfig;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }

        public static class Default {
            private int votingDurationHours = 24;
            private int maxVotesPerUser = 1;
            private boolean allowVoteModification = false;
            private boolean requireAuditTrail = true;

            public int getVotingDurationHours() {
                return votingDurationHours;
            }

            public void setVotingDurationHours(int votingDurationHours) {
                this.votingDurationHours = votingDurationHours;
            }

            public int getMaxVotesPerUser() {
                return maxVotesPerUser;
            }

            public void setMaxVotesPerUser(int maxVotesPerUser) {
                this.maxVotesPerUser = maxVotesPerUser;
            }

            public boolean isAllowVoteModification() {
                return allowVoteModification;
            }

            public void setAllowVoteModification(boolean allowVoteModification) {
                this.allowVoteModification = allowVoteModification;
            }

            public boolean isRequireAuditTrail() {
                return requireAuditTrail;
            }

            public void setRequireAuditTrail(boolean requireAuditTrail) {
                this.requireAuditTrail = requireAuditTrail;
            }
        }

        public static class Validation {
            private int minTitleLength = 5;
            private int maxTitleLength = 100;
            private int minDescriptionLength = 10;
            private int maxDescriptionLength = 500;
            private int minOptions = 2;
            private int maxOptions = 10;

            public int getMinTitleLength() {
                return minTitleLength;
            }

            public void setMinTitleLength(int minTitleLength) {
                this.minTitleLength = minTitleLength;
            }

            public int getMaxTitleLength() {
                return maxTitleLength;
            }

            public void setMaxTitleLength(int maxTitleLength) {
                this.maxTitleLength = maxTitleLength;
            }

            public int getMinDescriptionLength() {
                return minDescriptionLength;
            }

            public void setMinDescriptionLength(int minDescriptionLength) {
                this.minDescriptionLength = minDescriptionLength;
            }

            public int getMaxDescriptionLength() {
                return maxDescriptionLength;
            }

            public void setMaxDescriptionLength(int maxDescriptionLength) {
                this.maxDescriptionLength = maxDescriptionLength;
            }

            public int getMinOptions() {
                return minOptions;
            }

            public void setMinOptions(int minOptions) {
                this.minOptions = minOptions;
            }

            public int getMaxOptions() {
                return maxOptions;
            }

            public void setMaxOptions(int maxOptions) {
                this.maxOptions = maxOptions;
            }
        }
    }
}