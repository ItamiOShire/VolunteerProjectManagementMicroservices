package com.vpm.projectserver.unit.controller;

import com.vpm.projectserver.controller.OrganizationsProjectsController;
import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationsProjectsController.class)
@DisplayName("OrganizationsProjectsController Unit Tests")
class OrganizationsProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private ProjectTemplate testProjectTemplate;
    private ProjectTemplate testProjectTemplate2;

    @BeforeEach
    void setUp() {
        TagTemplate testTagTemplate = TagTemplate.builder()
                .itemId(1L)
                .tagName("Java")
                .build();

        testProjectTemplate = ProjectTemplate.builder()
                .itemId(1L)
                .projectTitle("Organization Project 1")
                .projectDescription("First project for organization")
                .imgPath("/path/to/image1.jpg")
                .organizationName("Tech Corp")
                .tags(List.of(testTagTemplate))
                .build();

        TagTemplate testTagTemplate2 = TagTemplate.builder()
                .itemId(2L)
                .tagName("Python")
                .build();

        testProjectTemplate2 = ProjectTemplate.builder()
                .itemId(2L)
                .projectTitle("Organization Project 2")
                .projectDescription("Second project for organization")
                .imgPath("/path/to/image2.jpg")
                .organizationName("Tech Corp")
                .tags(List.of(testTagTemplate2))
                .build();
    }

    /**
     * All request should have a pair of headers to identify if the request is surely authenticated by gateway and authorized
     * X-User-Id : any integer
     * X-User-Role : ORGANIZATION | VOLUNTEER
     */

    // ========== GET /api/organizations/{id}/projects ==========

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return all organization projects successfully")
    void testGetProjectsByOrganization_Success() throws Exception {

        long organizationId = 100L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate, testProjectTemplate2);
        when(projectService.getAllOrganizationProjects(organizationId)).thenReturn(projects);

        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemId").value(1L))
                .andExpect(jsonPath("$[0].projectTitle").value("Organization Project 1"))
                .andExpect(jsonPath("$[0].organizationName").value("Tech Corp"))
                .andExpect(jsonPath("$[1].itemId").value(2L))
                .andExpect(jsonPath("$[1].projectTitle").value("Organization Project 2"))
                .andExpect(jsonPath("$[1].tags", hasSize(1)));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return empty list when organization has no projects")
    void testGetProjectsByOrganization_EmptyList() throws Exception {

        long organizationId = 999L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should call service with correct organization ID")
    void testGetProjectsByOrganization_CorrectId() throws Exception {

        long organizationId = 42L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(List.of(testProjectTemplate));


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value(1L));

        verify(projectService, times(1)).getAllOrganizationProjects(42L);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return projects with complete structure")
    void testGetProjectsByOrganization_CompleteStructure() throws Exception {

        long organizationId = 100L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllOrganizationProjects(organizationId)).thenReturn(projects);


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").exists())
                .andExpect(jsonPath("$[0].projectTitle").exists())
                .andExpect(jsonPath("$[0].projectDescription").exists())
                .andExpect(jsonPath("$[0].imgPath").exists())
                .andExpect(jsonPath("$[0].organizationName").exists())
                .andExpect(jsonPath("$[0].tags").exists())
                .andExpect(jsonPath("$[0].itemId").value(1L))
                .andExpect(jsonPath("$[0].projectTitle").value("Organization Project 1"))
                .andExpect(jsonPath("$[0].projectDescription").value("First project for organization"))
                .andExpect(jsonPath("$[0].imgPath").value("/path/to/image1.jpg"))
                .andExpect(jsonPath("$[0].organizationName").value("Tech Corp"));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should handle single project")
    void testGetProjectsByOrganization_SingleProject() throws Exception {

        long organizationId = 100L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllOrganizationProjects(organizationId)).thenReturn(projects);


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemId").value(1L));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return projects with different tag counts")
    void testGetProjectsByOrganization_DifferentTagCounts() throws Exception {

        long organizationId = 100L;
        ProjectTemplate projectWithTags = testProjectTemplate;

        ProjectTemplate projectWithoutTags = ProjectTemplate.builder()
                .itemId(3L)
                .projectTitle("No Tags Project")
                .projectDescription("Project without tags")
                .imgPath("/path/to/image3.jpg")
                .organizationName("Tech Corp")
                .tags(Collections.emptyList())
                .build();

        List<ProjectTemplate> projects = List.of(projectWithTags, projectWithoutTags);
        when(projectService.getAllOrganizationProjects(organizationId)).thenReturn(projects);


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tags", hasSize(1)))
                .andExpect(jsonPath("$[1].tags", hasSize(0)));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return OK status code")
    void testGetProjectsByOrganization_OkStatus() throws Exception {

        long organizationId = 100L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(List.of(testProjectTemplate));


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should return JSON content type")
    void testGetProjectsByOrganization_JsonContentType() throws Exception {

        long organizationId = 100L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(List.of(testProjectTemplate));


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should handle invalid ID format")
    void testGetProjectsByOrganization_InvalidIdFormat() throws Exception {

        mockMvc.perform(get("/api/organizations/{id}/projects", "abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).getAllOrganizationProjects(anyLong());
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should handle zero ID")
    void testGetProjectsByOrganization_ZeroId() throws Exception {

        long organizationId = 0L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllOrganizationProjects(0L);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should handle negative ID")
    void testGetProjectsByOrganization_NegativeId() throws Exception {

        long organizationId = -1L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllOrganizationProjects(-1L);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should handle large ID")
    void testGetProjectsByOrganization_LargeId() throws Exception {

        long largeId = Long.MAX_VALUE;
        when(projectService.getAllOrganizationProjects(largeId))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/organizations/{id}/projects", largeId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllOrganizationProjects(largeId);
    }

    @Test
    @DisplayName("GET /api/organizations/{id}/projects - Should verify correct service method is called")
    void testGetProjectsByOrganization_VerifyServiceCall() throws Exception {

        long organizationId = 123L;
        when(projectService.getAllOrganizationProjects(organizationId))
                .thenReturn(Collections.emptyList());


        mockMvc.perform(get("/api/organizations/{id}/projects", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"));


        verify(projectService, times(1)).getAllOrganizationProjects(organizationId);
        verify(projectService, never()).getAllProjects();
        verify(projectService, never()).getAllVolunteerProjects(anyLong());
    }
}

