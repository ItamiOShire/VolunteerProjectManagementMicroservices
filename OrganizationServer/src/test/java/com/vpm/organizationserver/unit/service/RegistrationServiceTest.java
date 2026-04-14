package com.vpm.organizationserver.unit.service;

import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.organizationserver.api.internal.AuthClient;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.exception.organization.OrganizationAlreadyExistsException;
import com.vpm.organizationserver.repository.OrganizationRepository;
import com.vpm.organizationserver.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AuthClient authClient;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(organizationRepository, authClient);
    }

    /*
     * Test successful registration
     */

    @Test
    @DisplayName("Successfully register organization with valid data")
    void testRegister_Success() throws OrganizationAlreadyExistsException {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        Organization savedOrganization = createOrganization(1L);

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

  
        registrationService.register(request);

  
        verify(authClient, times(1)).registerOrganizationInAuthServer(any(AuthRegistrationRequest.class));
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    @DisplayName("Verify auth request contains correct email, password, and role")
    void testRegister_VerifyAuthRequestDetails() throws OrganizationAlreadyExistsException {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        Organization savedOrganization = createOrganization(1L);

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

        ArgumentCaptor<AuthRegistrationRequest> authRequestCaptor = ArgumentCaptor.forClass(AuthRegistrationRequest.class);

  
        registrationService.register(request);

  
        verify(authClient).registerOrganizationInAuthServer(authRequestCaptor.capture());
        AuthRegistrationRequest capturedAuthRequest = authRequestCaptor.getValue();

        assertEquals("test@example.com", capturedAuthRequest.getEmail());
        assertEquals("password123", capturedAuthRequest.getPassword());
        assertEquals("ORGANIZATION", capturedAuthRequest.getRole());
    }

    @Test
    @DisplayName("Verify organization entity is saved with all correct fields")
    void testRegister_VerifyOrganizationSaved() throws OrganizationAlreadyExistsException {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        Organization savedOrganization = createOrganization(1L);

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);

  
        registrationService.register(request);

  
        verify(organizationRepository).save(organizationCaptor.capture());
        Organization capturedOrganization = organizationCaptor.getValue();

        assertEquals("Test Organization", capturedOrganization.getOrganizationName());
        assertEquals("1234567890", capturedOrganization.getKrsNumber());
        assertEquals(1L, capturedOrganization.getUserId());
        assertEquals("Main St", capturedOrganization.getStreet());
        assertEquals("New York", capturedOrganization.getTown());
        assertEquals("10001", capturedOrganization.getZipCode());
        assertEquals("John", capturedOrganization.getOwnerFirstName());
        assertEquals("Doe", capturedOrganization.getOwnerLastName());
        assertEquals("+1234567890", capturedOrganization.getPhoneNumber());
    }

    @Test
    @DisplayName("Throw exception when auth client service fails")
    void testRegister_AuthClientThrowsException() {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        Exception authException = new RuntimeException("Auth service error");

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenThrow(authException);

        assertThrows(RuntimeException.class, () -> registrationService.register(request));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    @DisplayName("Throw exception when organization already exists")
    void testRegister_RepositoryThrowsOrganizationAlreadyExistsException() {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        OrganizationAlreadyExistsException repositoryException =
                new OrganizationAlreadyExistsException("Organization already exists", null);

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenThrow(repositoryException);

        assertThrows(OrganizationAlreadyExistsException.class, () -> registrationService.register(request));
    }

    @Test
    @DisplayName("Throw exception when database error occurs")
    void testRegister_RepositoryThrowsGeneralException() {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        RuntimeException repositoryException = new RuntimeException("Database error");

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenThrow(repositoryException);

        assertThrows(RuntimeException.class, () -> registrationService.register(request));
    }

    @Test
    @DisplayName("Set apartment number to dash when empty")
    void testRegister_WithEmptyApartmentNumber() {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        request.setApartmentNumber(""); // Empty apartment number

        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        Organization savedOrganization = createOrganization(1L);
        savedOrganization.setApartmentNumber("-");

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);

  
        registrationService.register(request);

  
        verify(organizationRepository).save(organizationCaptor.capture());
        Organization capturedOrganization = organizationCaptor.getValue();
        assertEquals("-", capturedOrganization.getApartmentNumber());
    }

    @Test
    @DisplayName("Save apartment number as provided when not empty")
    void testRegister_WithNonEmptyApartmentNumber() {
  
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        request.setApartmentNumber("58"); // Non-empty apartment number

        AuthRegistrationResponse authResponse = new AuthRegistrationResponse(1L);
        Organization savedOrganization = createOrganization(1L);
        savedOrganization.setApartmentNumber("58");

        when(authClient.registerOrganizationInAuthServer(any(AuthRegistrationRequest.class)))
                .thenReturn(authResponse);
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);

  
        registrationService.register(request);

  
        verify(organizationRepository).save(organizationCaptor.capture());
        Organization capturedOrganization = organizationCaptor.getValue();
        assertEquals("58", capturedOrganization.getApartmentNumber());
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
        request.setType("non-profit");
        request.setOwnerFirstName("John");
        request.setOwnerLastName("Doe");
        request.setPhoneNumber("+1234567890");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        return request;
    }

    private Organization createOrganization(long userId) {
        return Organization.builder()
                .id(1L)
                .userId(userId)
                .organizationName("Test Organization")
                .krsNumber("1234567890")
                .street("Main St")
                .apartmentNumber("10")
                .town("New York")
                .zipCode("10001")
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .phoneNumber("+1234567890")
                .build();
    }
}
