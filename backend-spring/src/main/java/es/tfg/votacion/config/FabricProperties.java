package es.tfg.votacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración de Hyperledger Fabric
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "fabric")
@Validated
public class FabricProperties {

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