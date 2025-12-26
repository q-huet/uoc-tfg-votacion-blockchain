package es.tfg.votacion.dto;

import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionOption;
import es.tfg.votacion.model.ElectionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ElectionResponseTest {

    @Test
    public void shouldMapPublicKeyFromElectionToResponse() {
        // Arrange
        String expectedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        LocalDateTime later = now.plusDays(1);
        
        Election election = new Election(
            "test-election",
            "Test Election",
            "Description",
            Collections.emptyList(), // options
            ElectionStatus.ACTIVE,
            now,
            later,
            "admin",
            LocalDateTime.now(),
            0,
            1,
            false,
            true,
            expectedPublicKey
        );

        // Act
        ElectionResponse response = ElectionResponse.fromElection(election, false, 0);

        // Assert
        assertNotNull(response.publicKey());
        assertEquals(expectedPublicKey, response.publicKey());
    }
}
