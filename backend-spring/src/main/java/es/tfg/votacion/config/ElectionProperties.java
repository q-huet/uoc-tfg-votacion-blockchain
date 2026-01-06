package es.tfg.votacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración específicas de elecciones
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "elections")
@Validated
public class ElectionProperties {

    private Default defaultConfig = new Default();
    private Validation validation = new Validation();
    private Notifications notifications = new Notifications();

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

    public Notifications getNotifications() {
        return notifications;
    }

    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
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

    public static class Notifications {
        private boolean emailEnabled = false;
        private boolean smsEnabled = false;

        public boolean isEmailEnabled() {
            return emailEnabled;
        }

        public void setEmailEnabled(boolean emailEnabled) {
            this.emailEnabled = emailEnabled;
        }

        public boolean isSmsEnabled() {
            return smsEnabled;
        }

        public void setSmsEnabled(boolean smsEnabled) {
            this.smsEnabled = smsEnabled;
        }
    }
}