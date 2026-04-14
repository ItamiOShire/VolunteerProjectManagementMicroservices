package com.vpm.organizationserver.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.organizationserver.controller.OrganizationController;
import com.vpm.organizationserver.dto.request.CreateDescriptionRequest;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.dto.request.UpdateDescriptionRequest;
import com.vpm.organizationserver.dto.response.OrganizationDescriptionResponse;
import com.vpm.organizationserver.dto.response.OrganizationProfileResponse;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationDescriptionException;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationException;
import com.vpm.organizationserver.exception.organization.OrganizationAlreadyExistsException;
import com.vpm.organizationserver.service.OrganizationService;
import com.vpm.organizationserver.service.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private RegistrationService registrationService;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String VALID_USER_ID = "1";
    private static final String VALID_USER_ROLE = "ORGANIZATION";

    @Test
    @DisplayName("Successfully retrieve organization profile with valid headers")
    void testGetOrganizationProfile_Success() throws Exception {
        long organizationUserId = 1L;
        OrganizationProfileResponse response = OrganizationProfileResponse.builder()
                .name("Test Organization")
                .krsNumber("1234567890")
                .address("Main St 10, New York 10001")
                .owner("John Doe")
                .contact("test@example.com, +1234567890")
                .build();

        when(organizationService.getOrganizationProfile(organizationUserId))
                .thenReturn(response);

        mockMvc.perform(get("/api/organizations/{id}", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Organization"))
                .andExpect(jsonPath("$.krsNumber").value("1234567890"));

        verify(organizationService, times(1)).getOrganizationProfile(organizationUserId);
    }

    @Test
    @DisplayName("Return 404 when organization profile not found")
    void testGetOrganizationProfile_NotFound() throws Exception {
        long organizationUserId = 999L;

        when(organizationService.getOrganizationProfile(organizationUserId))
                .thenThrow(new NoSuchOrganizationException(organizationUserId));

        mockMvc.perform(get("/api/organizations/{id}", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1)).getOrganizationProfile(organizationUserId);
    }

    @Test
    @DisplayName("Successfully retrieve organization description with valid headers")
    void testGetOrganizationDescription_Success() throws Exception {
        long organizationUserId = 1L;
        OrganizationDescriptionResponse response = OrganizationDescriptionResponse.builder()
                .organizationName("Test Organization")
                .description("Test Description")
                .imagePath("/path/to/image.jpg")
                .build();

        when(organizationService.getOrganizationDescription(organizationUserId))
                .thenReturn(response);

        mockMvc.perform(get("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(organizationService, times(1)).getOrganizationDescription(organizationUserId);
    }

    @Test
    @DisplayName("Return 404 when organization for description not found")
    void testGetOrganizationDescription_OrganizationNotFound() throws Exception {
        long organizationUserId = 999L;

        when(organizationService.getOrganizationDescription(organizationUserId))
                .thenThrow(new NoSuchOrganizationException(organizationUserId));

        mockMvc.perform(get("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1)).getOrganizationDescription(organizationUserId);
    }

    @Test
    @DisplayName("Successfully register organization with valid data")
    void testRegisterOrganization_Success() throws Exception {
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();

        doNothing().when(registrationService).register(any(OrganizationRegisterRequest.class));

        mockMvc.perform(post("/api/organizations/register")
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(registrationService, times(1)).register(any(OrganizationRegisterRequest.class));
    }

    @Test
    @DisplayName("Return 400 when registering organization that already exists")
    void testRegisterOrganization_AlreadyExists() throws Exception {
        OrganizationRegisterRequest request = createOrganizationRegisterRequest();
        OrganizationAlreadyExistsException exception =
                new OrganizationAlreadyExistsException("Email already in use", null);

        doThrow(exception).when(registrationService).register(any(OrganizationRegisterRequest.class));

        mockMvc.perform(post("/api/organizations/register")
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(registrationService, times(1)).register(any(OrganizationRegisterRequest.class));
    }

    @Test
    @DisplayName("Successfully create organization description")
    void testCreateOrganizationDescription_Success() throws Exception {
        long organizationUserId = 1L;
        CreateDescriptionRequest request = createDescriptionRequest();

        doNothing().when(organizationService)
                .createOrganizationDescription(any(CreateDescriptionRequest.class), anyLong());

        mockMvc.perform(post("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(organizationService, times(1))
                .createOrganizationDescription(any(CreateDescriptionRequest.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 404 when creating description for non-existent organization")
    void testCreateOrganizationDescription_OrganizationNotFound() throws Exception {
        long organizationUserId = 999L;
        CreateDescriptionRequest request = createDescriptionRequest();

        doThrow(new NoSuchOrganizationException(organizationUserId))
                .when(organizationService)
                .createOrganizationDescription(any(CreateDescriptionRequest.class), anyLong());

        mockMvc.perform(post("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1))
                .createOrganizationDescription(any(CreateDescriptionRequest.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Successfully update organization description")
    void testUpdateOrganizationDescription_Success() throws Exception {
        long organizationUserId = 1L;
        UpdateDescriptionRequest request = updateDescriptionRequest();

        doNothing().when(organizationService)
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), anyLong());

        mockMvc.perform(put("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(organizationService, times(1))
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 404 when updating description for non-existent organization")
    void testUpdateOrganizationDescription_OrganizationNotFound() throws Exception {
        long organizationUserId = 999L;
        UpdateDescriptionRequest request = updateDescriptionRequest();

        doThrow(new NoSuchOrganizationException(organizationUserId))
                .when(organizationService)
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), anyLong());

        mockMvc.perform(put("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1))
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 404 when updating non-existent organization description")
    void testUpdateOrganizationDescription_DescriptionNotFound() throws Exception {
        long organizationUserId = 1L;
        UpdateDescriptionRequest request = updateDescriptionRequest();

        doThrow(new NoSuchOrganizationDescriptionException(organizationUserId))
                .when(organizationService)
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), anyLong());

        mockMvc.perform(put("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1))
                .updateOrganizationDescription(any(UpdateDescriptionRequest.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Successfully patch organization description")
    void testPatchOrganizationDescription_Success() throws Exception {
        long organizationUserId = 1L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Updated Description");

        doNothing().when(organizationService)
                .patchOrganizationDescription(any(Map.class), anyLong());

        mockMvc.perform(patch("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        verify(organizationService, times(1))
                .patchOrganizationDescription(any(Map.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Successfully patch multiple description fields")
    void testPatchOrganizationDescription_MultipleFields() throws Exception {
        long organizationUserId = 1L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Updated Description");
        updates.put("imagePath", "/new/path/image.jpg");

        doNothing().when(organizationService)
                .patchOrganizationDescription(any(Map.class), anyLong());

        mockMvc.perform(patch("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        verify(organizationService, times(1))
                .patchOrganizationDescription(any(Map.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 404 when patching description for non-existent organization")
    void testPatchOrganizationDescription_OrganizationNotFound() throws Exception {
        long organizationUserId = 999L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Updated Description");

        doThrow(new NoSuchOrganizationException(organizationUserId))
                .when(organizationService)
                .patchOrganizationDescription(any(Map.class), anyLong());

        mockMvc.perform(patch("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1))
                .patchOrganizationDescription(any(Map.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 404 when patching non-existent organization description")
    void testPatchOrganizationDescription_DescriptionNotFound() throws Exception {
        long organizationUserId = 1L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Updated Description");

        doThrow(new NoSuchOrganizationDescriptionException(organizationUserId))
                .when(organizationService)
                .patchOrganizationDescription(any(Map.class), anyLong());

        mockMvc.perform(patch("/api/organizations/{id}/description", organizationUserId)
                .header(USER_ID_HEADER, VALID_USER_ID)
                .header(USER_ROLE_HEADER, VALID_USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());

        verify(organizationService, times(1))
                .patchOrganizationDescription(any(Map.class), eq(organizationUserId));
    }

    @Test
    @DisplayName("Return 400 when request does not contain required headers")
    void testRequest_MissingHeaders() throws Exception {
        long organizationUserId = 1L;
        mockMvc.perform(get("/api/organizations/{id}/description", organizationUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(organizationService, times(0)).getOrganizationDescription(anyLong());
    }

    @Test
    @DisplayName("Return 400 when request contains incomplete headers")
    void testRequest_InvalidHeader() throws Exception {
        long organizationUserId = 1L;
        mockMvc.perform(get("/api/organizations/{id}/description", organizationUserId)
                .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, VALID_USER_ID))
                .andExpect(status().isBadRequest());

        verify(organizationService, times(0)).getOrganizationDescription(anyLong());
    }

    @Test
    @DisplayName("Return 400 when request contains empty headers")
    void testRequest_EmptyHeader() throws Exception {
        long organizationUserId = 1L;
        mockMvc.perform(get("/api/organizations/{id}/description", organizationUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "")
                        .header(USER_ROLE_HEADER, ""))
                .andExpect(status().isBadRequest());

        verify(organizationService, times(0)).getOrganizationDescription(anyLong());
    }

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
