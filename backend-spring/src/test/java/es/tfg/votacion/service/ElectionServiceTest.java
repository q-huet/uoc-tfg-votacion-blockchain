package es.tfg.votacion.service;

import es.tfg.votacion.dto.ElectionCreationResult;
import es.tfg.votacion.model.Election;
import es.tfg.votacion.model.ElectionOption;
import es.tfg.votacion.model.ElectionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ElectionServiceTest {

    @InjectMocks
    private ElectionService electionService;

    @Mock
    private FabricService fabricService;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock CryptoService behavior
        KeyPair mockKeyPair = mock(KeyPair.class);
        PublicKey mockPublicKey = mock(PublicKey.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        
        when(mockKeyPair.getPublic()).thenReturn(mockPublicKey);
        when(mockKeyPair.getPrivate()).thenReturn(mockPrivateKey);
        when(cryptoService.generateKeyPair()).thenReturn(mockKeyPair);
        when(cryptoService.publicKeyToPem(any(PublicKey.class))).thenReturn("mock-public-key-pem");
        when(cryptoService.privateKeyToPem(any(PrivateKey.class))).thenReturn("mock-private-key-pem");
    }

    @Test
    void createElectionShouldGenerateKeysAndCallFabric() {
        // Arrange
        Election election = new Election(
            "test-election-1",
            "Test Election",
            "Description",
            List.of(new ElectionOption("opt1", "Option 1", "Desc 1", 1), new ElectionOption("opt2", "Option 2", "Desc 2", 2)),
            ElectionStatus.DRAFT,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            "admin",
            LocalDateTime.now(),
            0,
            1,
            false,
            true,
            null // publicKey is null initially
        );

        // Act
        ElectionCreationResult result = electionService.createElection(election);

        // Assert
        assertNotNull(result);
        assertNotNull(result.election());
        assertEquals("mock-public-key-pem", result.election().publicKey());
        assertEquals("mock-private-key-pem", result.privateKey());
        
        // Verify FabricService was called with the correct parameters including public key
        verify(fabricService).createElection(eq("test-election-1"), eq("mock-public-key-pem"));
    }
}
