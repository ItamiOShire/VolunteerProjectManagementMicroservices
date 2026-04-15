package com.vpm.organizationserver.integration.service;

import com.vpm.organizationserver.config.IntegrationTestsDBConfig;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@DisplayName("OrganizationService Integration Tests")
class OrganizationServiceTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationDescriptionRepository organizationDescriptionRepository;

    private static final long TEST_ORGANIZATION_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        organizationDescriptionRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create test organization
        Organization organization = Organization.builder()
                .userId(TEST_ORGANIZATION_USER_ID)
                .organizationName("Test Organization")
                .krsNumber("1234567890")
                .street("Test Street")
                .apartmentNumber("10A")
                .town("Test Town")
                .zipCode("12-345")
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .phoneNumber("123-456-7890")
                .contactEmail("test@example.com")
                .build();

        organization = organizationRepository.save(organization);

        // Create test organization description
        OrganizationDescription organizationDescription = OrganizationDescription.builder()
                .organization(organization)
                .description("Test organization description")
                .imagePath("/test/image.jpg")
                .build();

        organizationDescriptionRepository.save(organizationDescription);

        organization.setOrganizationDescription(organizationDescription);
        organizationRepository.save(organization);
    }

    /**
     * Helper method to fetch fresh test organization from database to avoid stale entity references
     */
    private Organization getTestOrganization() {
        return organizationRepository.findByUserId(TEST_ORGANIZATION_USER_ID)
                .orElseThrow(() -> new RuntimeException("Test organization not found"));
    }

    @Nested
    @DisplayName("GET Operations")
    class GetOperations {

        @Test
        @DisplayName("Should successfully retrieve organization profile")
        void testGetOrganizationProfileSuccess() throws NoSuchOrganizationException {
            // Act
            OrganizationProfileResponse response = organizationService.getOrganizationProfile(
                    TEST_ORGANIZATION_USER_ID
            );

            // Assert
            assertNotNull(response);
            assertEquals("Test Organization", response.getName());
            assertEquals("1234567890", response.getKrsNumber());
            assertTrue(response.getAddress().contains("Test Street"));
            assertTrue(response.getAddress().contains("10A"));
            assertTrue(response.getAddress().contains("12-345"));
            assertTrue(response.getAddress().contains("Test Town"));
            assertEquals("John Doe", response.getOwner());
            assertEquals("123-456-7890 / test@example.com", response.getContact());
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationException when organization does not exist")
        void testGetOrganizationProfileNotFound() {
            // Act & Assert
            assertThrows(
                    NoSuchOrganizationException.class,
                    () -> organizationService.getOrganizationProfile(9999L)
            );
        }

        @Test
        @DisplayName("Should successfully retrieve organization description")
        void testGetOrganizationDescriptionSuccess() throws NoSuchOrganizationException {
            // Act
            OrganizationDescriptionResponse response = organizationService.getOrganizationDescription(
                    TEST_ORGANIZATION_USER_ID
            );

            // Assert
            assertNotNull(response);
            assertEquals("Test Organization", response.getOrganizationName());
            assertEquals("Test organization description", response.getDescription());
            assertEquals("/test/image.jpg", response.getImagePath());
        }

        @Test
        @DisplayName("Should return empty response when organization has no description")
        void testGetOrganizationDescriptionNotFound() throws NoSuchOrganizationException {
            // Arrange
            Organization orgWithoutDesc = Organization.builder()
                    .userId(2002L)
                    .organizationName("No Description Org")
                    .krsNumber("0987654321")
                    .street("Another Street")
                    .town("Another Town")
                    .zipCode("54-321")
                    .ownerFirstName("Jane")
                    .ownerLastName("Smith")
                    .phoneNumber("987-654-3210")
                    .contactEmail("jane@example.com")
                    .build();
            organizationRepository.save(orgWithoutDesc);

            // Act
            OrganizationDescriptionResponse response = organizationService.getOrganizationDescription(
                    orgWithoutDesc.getUserId()
            );

            // Assert
            assertNotNull(response);
            assertNull(response.getDescription());
            assertNull(response.getImagePath());
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationException when getting description for non-existent organization")
        void testGetOrganizationDescriptionOrganizationNotFound() {
            // Act & Assert
            assertThrows(
                    NoSuchOrganizationException.class,
                    () -> organizationService.getOrganizationDescription(9999L)
            );
        }
    }

    @Nested
    @DisplayName("POST Operations")
    class PostOperations {

        @Test
        @DisplayName("Should successfully create organization description")
        void testCreateOrganizationDescriptionSuccess() throws NoSuchOrganizationException {
            // Arrange
            Organization newOrg = Organization.builder()
                    .userId(3003L)
                    .organizationName("New Organization")
                    .krsNumber("1111111111")
                    .street("New Street")
                    .town("New Town")
                    .zipCode("11-111")
                    .ownerFirstName("Bob")
                    .ownerLastName("Builder")
                    .phoneNumber("111-111-1111")
                    .contactEmail("bob@example.com")
                    .build();
            organizationRepository.save(newOrg);

            CreateDescriptionRequest request = new CreateDescriptionRequest();
            request.setDescription("New organization description");
            request.setImagePath("/new/image.jpg");

            // Act
            OrganizationDescriptionResponse result = organizationService.createOrganizationDescription(request, newOrg.getUserId());

            // Assert
            assertNotNull(result);
            OrganizationDescription saved = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(newOrg)
                    .orElseThrow();

            assertNotNull(saved);
            assertEquals("New organization description", saved.getDescription());
            assertEquals("/new/image.jpg", saved.getImagePath());
            assertEquals(newOrg.getId(), saved.getOrganization().getId());
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationException when creating description for non-existent organization")
        void testCreateOrganizationDescriptionNotFound() {
            // Arrange
            CreateDescriptionRequest request = new CreateDescriptionRequest();
            request.setDescription("Description");
            request.setImagePath("/image.jpg");

            // Act & Assert
            assertThrows(
                    NoSuchOrganizationException.class,
                    () -> organizationService.createOrganizationDescription(request, 9999L)
            );
        }

        @Test
        @DisplayName("Should successfully create description with null fields")
        void testCreateOrganizationDescriptionWithNullFields() throws NoSuchOrganizationException {
            // Arrange
            Organization newOrg = Organization.builder()
                    .userId(4004L)
                    .organizationName("Org With Nulls")
                    .krsNumber("2222222222")
                    .street("Null Street")
                    .town("Null Town")
                    .zipCode("22-222")
                    .ownerFirstName("Alice")
                    .ownerLastName("Wonder")
                    .phoneNumber("222-222-2222")
                    .contactEmail("alice@example.com")
                    .build();
            organizationRepository.save(newOrg);

            CreateDescriptionRequest request = new CreateDescriptionRequest();
            request.setDescription(null);
            request.setImagePath(null);

            // Act
            OrganizationDescriptionResponse result = organizationService.createOrganizationDescription(request, newOrg.getUserId());

            // Assert
            assertNotNull(result);
            OrganizationDescription saved = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(newOrg)
                    .orElseThrow();

            assertNotNull(saved);
            assertNull(saved.getDescription());
            assertNull(saved.getImagePath());
        }
    }

    @Nested
    @DisplayName("PUT Operations")
    class PutOperations {

        @Test
        @DisplayName("Should successfully update organization description")
        void testUpdateOrganizationDescriptionSuccess() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            UpdateDescriptionRequest request = new UpdateDescriptionRequest();
            request.setDescription("Updated description");
            request.setImagePath("/updated/image.jpg");

            // Act
            OrganizationDescriptionResponse result = organizationService.updateOrganizationDescription(request, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription updated = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertEquals("Updated description", updated.getDescription());
            assertEquals("/updated/image.jpg", updated.getImagePath());
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationException when updating for non-existent organization")
        void testUpdateOrganizationDescriptionOrganizationNotFound() {
            // Arrange
            UpdateDescriptionRequest request = new UpdateDescriptionRequest();
            request.setDescription("Updated");
            request.setImagePath("/updated.jpg");

            // Act & Assert
            assertThrows(
                    NoSuchOrganizationException.class,
                    () -> organizationService.updateOrganizationDescription(request, 9999L)
            );
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationDescriptionException when description does not exist")
        void testUpdateOrganizationDescriptionNotFound() {
            // Arrange
            Organization orgWithoutDesc = Organization.builder()
                    .userId(5005L)
                    .organizationName("No Description Org")
                    .krsNumber("5555555555")
                    .street("Empty Street")
                    .town("Empty Town")
                    .zipCode("55-555")
                    .ownerFirstName("Eve")
                    .ownerLastName("Empty")
                    .phoneNumber("555-555-5555")
                    .contactEmail("eve@example.com")
                    .build();
            organizationRepository.save(orgWithoutDesc);

            UpdateDescriptionRequest request = new UpdateDescriptionRequest();
            request.setDescription("New description");
            request.setImagePath("/new.jpg");

            // Act & Assert
            assertThrows(
                    NoSuchOrganizationDescriptionException.class,
                    () -> organizationService.updateOrganizationDescription(request, orgWithoutDesc.getUserId())
            );
        }

        @Test
        @DisplayName("Should successfully update with null fields")
        void testUpdateOrganizationDescriptionWithNullFields() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            UpdateDescriptionRequest request = new UpdateDescriptionRequest();
            request.setDescription(null);
            request.setImagePath(null);

            // Act
            OrganizationDescriptionResponse result = organizationService.updateOrganizationDescription(request, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription updated = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertNull(updated.getDescription());
            assertNull(updated.getImagePath());
        }
    }

    @Nested
    @DisplayName("PATCH Operations")
    class PatchOperations {

        @Test
        @DisplayName("Should successfully patch description only")
        void testPatchOrganizationDescriptionDescriptionOnly() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", "Patched description");

            // Act
            OrganizationDescriptionResponse result = organizationService.patchOrganizationDescription(updates, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription patched = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertEquals("Patched description", patched.getDescription());
            assertEquals("/test/image.jpg", patched.getImagePath()); // Should remain unchanged
        }

        @Test
        @DisplayName("Should successfully patch imagePath only")
        void testPatchOrganizationDescriptionImagePathOnly() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("imagePath", "/patched/image.jpg");

            // Act
            OrganizationDescriptionResponse result = organizationService.patchOrganizationDescription(updates, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription patched = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertEquals("Test organization description", patched.getDescription()); // Should remain unchanged
            assertEquals("/patched/image.jpg", patched.getImagePath());
        }

        @Test
        @DisplayName("Should successfully patch multiple fields")
        void testPatchOrganizationDescriptionMultipleFields() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", "Patched description");
            updates.put("imagePath", "/patched/new.jpg");

            // Act
            OrganizationDescriptionResponse result = organizationService.patchOrganizationDescription(updates, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription patched = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertEquals("Patched description", patched.getDescription());
            assertEquals("/patched/new.jpg", patched.getImagePath());
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationException when patching for non-existent organization")
        void testPatchOrganizationDescriptionOrganizationNotFound() {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", "Patched");

            // Act & Assert
            assertThrows(
                    NoSuchOrganizationException.class,
                    () -> organizationService.patchOrganizationDescription(updates, 9999L)
            );
        }

        @Test
        @DisplayName("Should throw NoSuchOrganizationDescriptionException when description does not exist")
        void testPatchOrganizationDescriptionNotFound() {
            // Arrange
            Organization orgWithoutDesc = Organization.builder()
                    .userId(6006L)
                    .organizationName("No Description Org")
                    .krsNumber("6666666666")
                    .street("Patch Street")
                    .town("Patch Town")
                    .zipCode("66-666")
                    .ownerFirstName("Frank")
                    .ownerLastName("Patcher")
                    .phoneNumber("666-666-6666")
                    .contactEmail("frank@example.com")
                    .build();
            organizationRepository.save(orgWithoutDesc);

            Map<String, Object> updates = new HashMap<>();
            updates.put("description", "New");

            // Act & Assert
            assertThrows(
                    NoSuchOrganizationDescriptionException.class,
                    () -> organizationService.patchOrganizationDescription(updates, orgWithoutDesc.getUserId())
            );
        }

        @Test
        @DisplayName("Should handle empty patch update map")
        void testPatchOrganizationDescriptionEmpty() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();

            // Act
            OrganizationDescriptionResponse result = organizationService.patchOrganizationDescription(updates, TEST_ORGANIZATION_USER_ID);

            // Assert - should remain unchanged
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription unchanged = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertEquals("Test organization description", unchanged.getDescription());
            assertEquals("/test/image.jpg", unchanged.getImagePath());
        }

        @Test
        @DisplayName("Should successfully patch with null values")
        void testPatchOrganizationDescriptionWithNullValues() throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", null);
            updates.put("imagePath", null);

            // Act
            OrganizationDescriptionResponse result = organizationService.patchOrganizationDescription(updates, TEST_ORGANIZATION_USER_ID);

            // Assert
            assertNotNull(result);
            Organization testOrg = getTestOrganization();
            OrganizationDescription patched = organizationDescriptionRepository
                    .findOrganizationDescriptionByOrganization(testOrg)
                    .orElseThrow();

            assertNull(patched.getDescription());
            assertNull(patched.getImagePath());
        }
    }
}
