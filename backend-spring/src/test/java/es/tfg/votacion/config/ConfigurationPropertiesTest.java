package es.tfg.votacion.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que las propiedades de configuración se cargan correctamente
 * 
 * @author Enrique Huet Adrover
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest
class ConfigurationPropertiesTest {

    @Autowired
    private FabricProperties fabricProperties;

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private ElectionProperties electionProperties;

    @Test
    void fabricPropertiesShouldBeLoaded() {
        assertNotNull(fabricProperties);
        assertNotNull(fabricProperties.getConnectionProfile());
        assertNotNull(fabricProperties.getWalletPath());
        assertEquals("appUser", fabricProperties.getUser());
        assertEquals("electionchannel", fabricProperties.getChannel());
        assertEquals("electioncc", fabricProperties.getChaincode());
        
        // Test timeouts
        assertNotNull(fabricProperties.getTimeouts());
        assertEquals(30, fabricProperties.getTimeouts().getConnection());
        assertEquals(60, fabricProperties.getTimeouts().getTransaction());
        assertEquals(10, fabricProperties.getTimeouts().getQuery());
        
        // Test retry
        assertNotNull(fabricProperties.getRetry());
        assertEquals(3, fabricProperties.getRetry().getMaxAttempts());
        assertEquals(1000, fabricProperties.getRetry().getBackoffDelayMs());
    }

    @Test
    void storagePropertiesShouldBeLoaded() {
        assertNotNull(storageProperties);
        assertNotNull(storageProperties.getBasePath());
        assertNotNull(storageProperties.getKeystorePath());
        
        // Test encryption settings
        assertNotNull(storageProperties.getEncryption());
        assertEquals("AES/GCM/NoPadding", storageProperties.getEncryption().getAlgorithm());
        assertEquals(256, storageProperties.getEncryption().getKeyLength());
        assertEquals(12, storageProperties.getEncryption().getIvLength());
        assertEquals(128, storageProperties.getEncryption().getTagLength());
        
        // Test cleanup settings
        assertNotNull(storageProperties.getCleanup());
        assertTrue(storageProperties.getCleanup().isEnabled());
        assertEquals(30, storageProperties.getCleanup().getRetentionDays());
    }

    @Test
    void authPropertiesShouldBeLoaded() {
        assertNotNull(authProperties);
        assertNotNull(authProperties.getUsersFile());
        
        // Test JWT settings
        assertNotNull(authProperties.getJwt());
        assertNotNull(authProperties.getJwt().getSecret());
        assertEquals(3600, authProperties.getJwt().getExpiration());
        assertEquals("votacion-blockchain-poc", authProperties.getJwt().getIssuer());
        assertEquals("voting-system", authProperties.getJwt().getAudience());
        
        // Test roles
        assertNotNull(authProperties.getRoles());
        assertEquals(3, authProperties.getRoles().length);
    }

    @Test
    void electionPropertiesShouldBeLoaded() {
        assertNotNull(electionProperties);
        
        // Test default config
        assertNotNull(electionProperties.getDefaultConfig());
        assertEquals(24, electionProperties.getDefaultConfig().getVotingDurationHours());
        assertEquals(1, electionProperties.getDefaultConfig().getMaxVotesPerUser());
        assertFalse(electionProperties.getDefaultConfig().isAllowVoteModification());
        assertTrue(electionProperties.getDefaultConfig().isRequireAuditTrail());
        
        // Test validation
        assertNotNull(electionProperties.getValidation());
        assertEquals(5, electionProperties.getValidation().getMinTitleLength());
        assertEquals(100, electionProperties.getValidation().getMaxTitleLength());
        assertEquals(10, electionProperties.getValidation().getMinDescriptionLength());
        assertEquals(500, electionProperties.getValidation().getMaxDescriptionLength());
        assertEquals(2, electionProperties.getValidation().getMinOptions());
        assertEquals(10, electionProperties.getValidation().getMaxOptions());
        
        // Test notifications
        assertNotNull(electionProperties.getNotifications());
        assertFalse(electionProperties.getNotifications().isEmailEnabled());
        assertFalse(electionProperties.getNotifications().isSmsEnabled());
    }
}