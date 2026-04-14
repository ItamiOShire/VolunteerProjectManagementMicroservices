package com.vpm.organizationserver.unit.service;

import com.vpm.organizationserver.dto.request.CreateDescriptionRequest;
import com.vpm.organizationserver.dto.request.UpdateDescriptionRequest;
import com.vpm.organizationserver.dto.response.OrganizationDescriptionResponse;
import com.vpm.organizationserver.dto.response.OrganizationProfileResponse;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.entity.OrganizationDescription;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationDescriptionException;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationException;
import com.vpm.organizationserver.repository.OrganizationDescriptionRepository;
import com.vpm.organizationserver.repository.OrganizationRepository;
import com.vpm.organizationserver.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationDescriptionRepository organizationDescriptionRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(organizationRepository, organizationDescriptionRepository);
    }

    // ==================== getOrganizationProfile Tests ====================

    @Test
    @DisplayName("Successfully retrieve organization profile")
    void testGetOrganizationProfile_Success() throws NoSuchOrganizationException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));

  
        OrganizationProfileResponse response = organizationService.getOrganizationProfile(organizationUserId);


        assertNotNull(response);
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
    }

    @Test
    @DisplayName("Throw exception when organization profile not found")
    void testGetOrganizationProfile_OrganizationNotFound() {
  
        long organizationUserId = 999L;
        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationException.class,
                () -> organizationService.getOrganizationProfile(organizationUserId));
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
    }

    // ==================== getOrganizationDescription Tests ====================

    @Test
    @DisplayName("Successfully retrieve organization description when it exists")
    void testGetOrganizationDescription_Success_WithDescription() throws NoSuchOrganizationException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        organization.setOrganizationDescription(description);

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

  
        OrganizationDescriptionResponse response = organizationService.getOrganizationDescription(organizationUserId);

  
        assertNotNull(response);
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
        verify(organizationDescriptionRepository, times(1))
                .findOrganizationDescriptionByOrganization(organization);
    }

    @Test
    @DisplayName("Throw exception when retrieving description for non-existent organization")
    void testGetOrganizationDescription_OrganizationNotFound() {
  
        long organizationUserId = 999L;
        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationException.class,
                () -> organizationService.getOrganizationDescription(organizationUserId));
    }

    @Test
    @DisplayName("Return empty description response when description does not exist")
    void testGetOrganizationDescription_NoDescription() throws NoSuchOrganizationException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.empty());

  
        OrganizationDescriptionResponse response = organizationService.getOrganizationDescription(organizationUserId);

  
        assertNotNull(response);
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
        verify(organizationDescriptionRepository, times(1))
                .findOrganizationDescriptionByOrganization(organization);
    }

    // ==================== createOrganizationDescription Tests ====================

    @Test
    @DisplayName("Successfully create organization description")
    void testCreateOrganizationDescription_Success() throws NoSuchOrganizationException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        CreateDescriptionRequest request = createDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));

  
        organizationService.createOrganizationDescription(request, organizationUserId);

  
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
        verify(organizationDescriptionRepository, times(1)).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Verify created description entity contains correct data")
    void testCreateOrganizationDescription_VerifyDescriptionSaved() throws NoSuchOrganizationException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        CreateDescriptionRequest request = createDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));

        ArgumentCaptor<OrganizationDescription> descriptionCaptor =
                ArgumentCaptor.forClass(OrganizationDescription.class);

  
        organizationService.createOrganizationDescription(request, organizationUserId);

  
        verify(organizationDescriptionRepository).save(descriptionCaptor.capture());
        OrganizationDescription capturedDescription = descriptionCaptor.getValue();

        assertEquals("Test Description", capturedDescription.getDescription());
        assertEquals("/path/to/image.jpg", capturedDescription.getImagePath());
        assertEquals(organization, capturedDescription.getOrganization());
    }

    @Test
    @DisplayName("Throw exception when creating description for non-existent organization")
    void testCreateOrganizationDescription_OrganizationNotFound() {
  
        long organizationUserId = 999L;
        CreateDescriptionRequest request = createDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationException.class,
                () -> organizationService.createOrganizationDescription(request, organizationUserId));
        verify(organizationDescriptionRepository, never()).save(any(OrganizationDescription.class));
    }

    // ==================== updateOrganizationDescription Tests ====================

    @Test
    @DisplayName("Successfully update organization description")
    void testUpdateOrganizationDescription_Success() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        UpdateDescriptionRequest request = updateDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

  
        organizationService.updateOrganizationDescription(request, organizationUserId);

  
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
        verify(organizationDescriptionRepository, times(1))
                .findOrganizationDescriptionByOrganization(organization);
        verify(organizationDescriptionRepository, times(1)).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Verify updated description contains correct data")
    void testUpdateOrganizationDescription_VerifyDescriptionUpdated()
            throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        UpdateDescriptionRequest request = updateDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

        ArgumentCaptor<OrganizationDescription> descriptionCaptor =
                ArgumentCaptor.forClass(OrganizationDescription.class);

  
        organizationService.updateOrganizationDescription(request, organizationUserId);

  
        verify(organizationDescriptionRepository).save(descriptionCaptor.capture());
        OrganizationDescription capturedDescription = descriptionCaptor.getValue();

        assertEquals("Updated Description", capturedDescription.getDescription());
        assertEquals("/path/to/updated/image.jpg", capturedDescription.getImagePath());
    }

    @Test
    @DisplayName("Throw exception when updating description for non-existent organization")
    void testUpdateOrganizationDescription_OrganizationNotFound() {
  
        long organizationUserId = 999L;
        UpdateDescriptionRequest request = updateDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationException.class,
                () -> organizationService.updateOrganizationDescription(request, organizationUserId));
        verify(organizationDescriptionRepository, never()).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Throw exception when updating non-existent organization description")
    void testUpdateOrganizationDescription_DescriptionNotFound() {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        UpdateDescriptionRequest request = updateDescriptionRequest();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationDescriptionException.class,
                () -> organizationService.updateOrganizationDescription(request, organizationUserId));
        verify(organizationDescriptionRepository, never()).save(any(OrganizationDescription.class));
    }

    // ==================== patchOrganizationDescription Tests ====================

    @Test
    @DisplayName("Successfully patch organization description")
    void testPatchOrganizationDescription_Success() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Patched Description");

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

  
        organizationService.patchOrganizationDescription(updates, organizationUserId);

  
        verify(organizationRepository, times(1)).findByUserId(organizationUserId);
        verify(organizationDescriptionRepository, times(1))
                .findOrganizationDescriptionByOrganization(organization);
        verify(organizationDescriptionRepository, times(1)).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Successfully patch multiple description fields")
    void testPatchOrganizationDescription_PatchMultipleFields()
            throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Patched Description");
        updates.put("imagePath", "/new/path/image.jpg");

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

  
        organizationService.patchOrganizationDescription(updates, organizationUserId);

  
        verify(organizationDescriptionRepository, times(1)).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Throw exception when patching description for non-existent organization")
    void testPatchOrganizationDescription_OrganizationNotFound() {
  
        long organizationUserId = 999L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Patched Description");

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationException.class,
                () -> organizationService.patchOrganizationDescription(updates, organizationUserId));
        verify(organizationDescriptionRepository, never()).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Throw exception when patching non-existent organization description")
    void testPatchOrganizationDescription_DescriptionNotFound() {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Patched Description");

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchOrganizationDescriptionException.class,
                () -> organizationService.patchOrganizationDescription(updates, organizationUserId));
        verify(organizationDescriptionRepository, never()).save(any(OrganizationDescription.class));
    }

    @Test
    @DisplayName("Successfully patch description with empty updates map")
    void testPatchOrganizationDescription_EmptyUpdates()
            throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
  
        long organizationUserId = 1L;
        Organization organization = createOrganization(organizationUserId);
        OrganizationDescription description = createOrganizationDescription(organization);
        Map<String, Object> updates = new HashMap<>();

        when(organizationRepository.findByUserId(organizationUserId))
                .thenReturn(Optional.of(organization));
        when(organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization))
                .thenReturn(Optional.of(description));

  
        organizationService.patchOrganizationDescription(updates, organizationUserId);

  
        verify(organizationDescriptionRepository, times(1)).save(any(OrganizationDescription.class));
    }

    // ==================== Helper Methods ====================

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

    private OrganizationDescription createOrganizationDescription(Organization organization) {
        return OrganizationDescription.builder()
                .id(1L)
                .organization(organization)
                .description("Test Description")
                .imagePath("/path/to/image.jpg")
                .build();
    }

    private CreateDescriptionRequest createDescriptionRequest() {
        CreateDescriptionRequest request = new CreateDescriptionRequest();
        request.setDescription("Test Description");
        request.setImagePath("/path/to/image.jpg");
        return request;
    }

    private UpdateDescriptionRequest updateDescriptionRequest() {
        UpdateDescriptionRequest request = new UpdateDescriptionRequest();
        request.setDescription("Updated Description");
        request.setImagePath("/path/to/updated/image.jpg");
        return request;
    }
}

