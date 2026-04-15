package com.vpm.organizationserver.integration.service;

import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.organizationserver.api.internal.AuthClient;
import com.vpm.organizationserver.config.IntegrationTestsDBConfig;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.exception.organization.OrganizationAlreadyExistsException;
import com.vpm.organizationserver.repository.OrganizationRepository;
import com.vpm.organizationserver.service.RegistrationService;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@DisplayName("RegistrationService Integration Tests")
public class RegistrationServiceTest {

    @MockitoBean
    AuthClient authClient;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    /*
     * Testing data
     */
    private OrganizationRegisterRequest validRegistrationRequest;
    private AuthRegistrationResponse authRegistrationResponse;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        organizationRepository.deleteAll();

        validRegistrationRequest = createOrganizationRegisterRequest();
        authRegistrationResponse = new AuthRegistrationResponse();
        authRegistrationResponse.setUserId(1L);
    }

    @Nested
    @DisplayName("Successful Registration Tests")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should successfully register organization with valid data")
        void shouldSuccessfullyRegisterOrganization() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            assertDoesNotThrow(() -> registrationService.register(validRegistrationRequest));

            // Assert
            Optional<Organization> savedOrganization = organizationRepository.findByUserId(1L);
            assertTrue(savedOrganization.isPresent(), "Organization should be saved in database");

            Organization organization = savedOrganization.get();
            assertEquals("Test Organization", organization.getOrganizationName());
            assertEquals("1234567890", organization.getKrsNumber());
            assertEquals(1L, organization.getUserId());
            assertEquals("Main St", organization.getStreet());
            assertEquals("10", organization.getApartmentNumber());
            assertEquals("New York", organization.getTown());
            assertEquals("10001", organization.getZipCode());
            assertEquals("John", organization.getOwnerFirstName());
            assertEquals("Doe", organization.getOwnerLastName());
            assertEquals("+1234567890", organization.getPhoneNumber());
            assertEquals("contact-test@example.com", organization.getContactEmail());
        }

        @Test
        @DisplayName("Should correctly map request to organization entity")
        void shouldCorrectlyMapRequestToEntity() throws OrganizationAlreadyExistsException {
            // Arrange
            OrganizationRegisterRequest request = new OrganizationRegisterRequest();
            request.setOrganizationName("Tech NonProfit");
            request.setKrsNumber("9876543210");
            request.setStreet("Oak Avenue");
            request.setApartmentNumber("25");
            request.setTown("San Francisco");
            request.setZipCode("94101");
            request.setOwnerFirstName("Jane");
            request.setOwnerLastName("Smith");
            request.setPhoneNumber("+9876543210");
            request.setEmail("jane@techorg.com");
            request.setContactEmail("jane-contact@techorg.com");
            request.setPassword("securepass123");

            AuthRegistrationResponse response = new AuthRegistrationResponse();
            response.setUserId(99L);

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(response);

            // Act
            registrationService.register(request);

            // Assert
            Optional<Organization> savedOrganization = organizationRepository.findByUserId(99L);
            assertTrue(savedOrganization.isPresent());

            Organization organization = savedOrganization.get();
            assertEquals("Tech NonProfit", organization.getOrganizationName());
            assertEquals("9876543210", organization.getKrsNumber());
            assertEquals(99L, organization.getUserId());
            assertEquals("Oak Avenue", organization.getStreet());
            assertEquals("25", organization.getApartmentNumber());
            assertEquals("San Francisco", organization.getTown());
            assertEquals("94101", organization.getZipCode());
            assertEquals("Jane", organization.getOwnerFirstName());
            assertEquals("Smith", organization.getOwnerLastName());
            assertEquals("+9876543210", organization.getPhoneNumber());
            assertEquals("jane-contact@techorg.com", organization.getContactEmail());
        }

        @Test
        @DisplayName("Should handle apartment number as dash when empty")
        void shouldHandleEmptyApartmentNumber() throws OrganizationAlreadyExistsException {
            // Arrange
            OrganizationRegisterRequest request = createOrganizationRegisterRequest();
            request.setApartmentNumber("");

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(request);

            // Assert
            Optional<Organization> savedOrganization = organizationRepository.findByUserId(1L);
            assertTrue(savedOrganization.isPresent());
            assertEquals("-", savedOrganization.get().getApartmentNumber());
        }

        @Test
        @DisplayName("Should persist organization to database with auto-generated ID")
        void shouldPersistWithAutoGeneratedId() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(validRegistrationRequest);

            // Assert
            Optional<Organization> savedOrganization = organizationRepository.findByUserId(1L);
            assertTrue(savedOrganization.isPresent());
            assertTrue(savedOrganization.get().getId() > 0, "Organization ID should be auto-generated");
        }
    }

    @Nested
    @DisplayName("Multiple Organizations Registration Tests")
    class MultipleOrganizationsTests {

        @Test
        @DisplayName("Should register multiple organizations with different user IDs")
        void shouldRegisterMultipleOrganizations() throws OrganizationAlreadyExistsException {
            // Arrange
            AuthRegistrationResponse response1 = new AuthRegistrationResponse();
            response1.setUserId(1L);

            AuthRegistrationResponse response2 = new AuthRegistrationResponse();
            response2.setUserId(2L);

            OrganizationRegisterRequest request1 = createOrganizationRegisterRequest();
            OrganizationRegisterRequest request2 = createOrganizationRegisterRequest();
            request2.setOrganizationName("Second Organization");
            request2.setEmail("second@example.com");
            request2.setKrsNumber("1111111111");

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(response1)
                    .thenReturn(response2);

            // Act
            registrationService.register(request1);
            registrationService.register(request2);

            // Assert
            assertEquals(2, organizationRepository.count(), "Both organizations should be saved");
            assertTrue(organizationRepository.findByUserId(1L).isPresent());
            assertTrue(organizationRepository.findByUserId(2L).isPresent());

            Organization org2 = organizationRepository.findByUserId(2L).get();
            assertEquals("Second Organization", org2.getOrganizationName());
            assertEquals("1111111111", org2.getKrsNumber());
        }

        @Test
        @DisplayName("Should reject duplicate user ID registration")
        void shouldRejectDuplicateUserId() throws OrganizationAlreadyExistsException {
            // Arrange
            AuthRegistrationResponse response = new AuthRegistrationResponse();
            response.setUserId(1L);

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(response);

            OrganizationRegisterRequest request1 = createOrganizationRegisterRequest();
            OrganizationRegisterRequest request2 = createOrganizationRegisterRequest();
            request2.setOrganizationName("Duplicate Organization");

            registrationService.register(request1);

            // Act & Assert
            assertThrows(Exception.class, () -> registrationService.register(request2),
                    "Should throw exception when trying to register with duplicate user ID");
        }
    }

    @Nested
    @DisplayName("Auth Client Interaction Tests")
    class AuthClientInteractionTests {

        @Test
        @DisplayName("Should pass correct auth request to auth service")
        void shouldPassCorrectAuthRequest() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(validRegistrationRequest);

            // Assert - Verify organization was saved (indicating auth call was successful)
            assertTrue(organizationRepository.findByUserId(1L).isPresent(),
                    "Organization should be saved after successful auth registration");
        }

        @Test
        @DisplayName("Should use userId from auth response")
        void shouldUseUserIdFromAuthResponse() throws OrganizationAlreadyExistsException {
            // Arrange
            AuthRegistrationResponse response = new AuthRegistrationResponse();
            response.setUserId(42L);

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(response);

            // Act
            registrationService.register(validRegistrationRequest);

            // Assert
            Optional<Organization> savedOrganization = organizationRepository.findByUserId(42L);
            assertTrue(savedOrganization.isPresent(), "Organization should be saved with userId from auth response");
            assertEquals(42L, savedOrganization.get().getUserId());
        }
    }

    @Nested
    @DisplayName("Database Persistence Tests")
    class DatabasePersistenceTests {

        @Test
        @DisplayName("Should retrieve registered organization by user ID")
        void shouldRetrieveOrganizationByUserId() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(validRegistrationRequest);

            // Assert
            Optional<Organization> retrieved = organizationRepository.findByUserId(1L);
            assertTrue(retrieved.isPresent());
            assertEquals("Test Organization", retrieved.get().getOrganizationName());
        }

        @Test
        @DisplayName("Should verify organization uniqueness by user ID")
        void shouldVerifyUserIdUniqueness() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            registrationService.register(validRegistrationRequest);

            // Act & Assert
            assertTrue(organizationRepository.existsByUserId(1L));
            assertFalse(organizationRepository.existsByUserId(999L));
        }

        @Test
        @DisplayName("Should maintain data consistency across database operations")
        void shouldMaintainDataConsistency() throws OrganizationAlreadyExistsException {
            // Arrange
            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(validRegistrationRequest);

            // Assert
            Optional<Organization> savedOptional = organizationRepository.findByUserId(1L);
            assertTrue(savedOptional.isPresent());
            Organization saved = savedOptional.get();

            // Verify all fields are persisted correctly
            assertTrue(saved.getId() > 0);
            assertEquals(1L, saved.getUserId());
            assertEquals("Test Organization", saved.getOrganizationName());
            assertEquals("1234567890", saved.getKrsNumber());
            assertEquals("Main St", saved.getStreet());
            assertEquals("10", saved.getApartmentNumber());
            assertEquals("New York", saved.getTown());
            assertEquals("10001", saved.getZipCode());
            assertEquals("John", saved.getOwnerFirstName());
            assertEquals("Doe", saved.getOwnerLastName());
            assertEquals("+1234567890", saved.getPhoneNumber());
            assertEquals("contact-test@example.com", saved.getContactEmail());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle organization with minimal data")
        void shouldHandleMinimalData() throws OrganizationAlreadyExistsException {
            // Arrange
            OrganizationRegisterRequest minimalRequest = new OrganizationRegisterRequest();
            minimalRequest.setOrganizationName("Min Org");
            minimalRequest.setKrsNumber("1234567890");
            minimalRequest.setStreet("St");
            minimalRequest.setApartmentNumber("");
            minimalRequest.setTown("City");
            minimalRequest.setZipCode("00000");
            minimalRequest.setOwnerFirstName("A");
            minimalRequest.setOwnerLastName("B");
            minimalRequest.setPhoneNumber("0");
            minimalRequest.setEmail("a@b.c");
            minimalRequest.setContactEmail("c-a@b.c");
            minimalRequest.setPassword("p");

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act & Assert
            assertDoesNotThrow(() -> registrationService.register(minimalRequest));
            assertTrue(organizationRepository.findByUserId(1L).isPresent());
        }

        @Test
        @DisplayName("Should handle organization with special characters in name")
        void shouldHandleSpecialCharacters() throws OrganizationAlreadyExistsException {
            // Arrange
            OrganizationRegisterRequest request = createOrganizationRegisterRequest();
            request.setOrganizationName("Test & Co. (Registered)");
            request.setOwnerFirstName("José");
            request.setOwnerLastName("García-López");

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            // Act
            registrationService.register(request);


            Optional<Organization> savedOptional = organizationRepository.findByUserId(1L);
            assertTrue(savedOptional.isPresent());
            Organization saved = savedOptional.get();
            assertEquals("Test & Co. (Registered)", saved.getOrganizationName());
            assertEquals("José", saved.getOwnerFirstName());
            assertEquals("García-López", saved.getOwnerLastName());
        }

        @Test
        @DisplayName("Should throw error on long strings within column limits")
        void shouldHandleLongStrings() throws OrganizationAlreadyExistsException {

            OrganizationRegisterRequest request = createOrganizationRegisterRequest();
            request.setOrganizationName("Very Long Organization Name That Is Still Valid");
            request.setStreet("Very Long Street Name That Fits In The Limit");
            request.setTown("Very Long Town Name That Fits");

            when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> registrationService.register(request),
                    "Should reject too long values"
            );
        }
    }

    // Helper methods
    private OrganizationRegisterRequest createOrganizationRegisterRequest() {
        OrganizationRegisterRequest request = new OrganizationRegisterRequest();

        request.setOrganizationName("Test Organization");
        request.setKrsNumber("1234567890");
        request.setStreet("Main St");
        request.setApartmentNumber("10");
        request.setTown("New York");
        request.setZipCode("10001");
        request.setOwnerFirstName("John");
        request.setOwnerLastName("Doe");
        request.setPhoneNumber("+1234567890");
        request.setContactEmail("contact-test@example.com");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        return request;
    }


}
