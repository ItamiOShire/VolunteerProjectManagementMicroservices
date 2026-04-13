package com.vpm.projectserver.controller;

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

@WebMvcTest(VolunteersProjectsController.class)
@DisplayName("VolunteersProjectsController Unit Tests")
class VolunteersProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private ProjectTemplate testProjectTemplate;
    private ProjectTemplate testProjectTemplate2;
    private ProjectTemplate testProjectTemplate3;

    @BeforeEach
    void setUp() {
        TagTemplate tagTemplate1 = TagTemplate.builder()
                .tagId(1L)
                .tagName("Java")
                .build();

        testProjectTemplate = ProjectTemplate.builder()
                .projectId(1L)
                .projectTitle("Volunteer Project 1")
                .projectDescription("First volunteer project")
                .imgPath("/path/to/image1.jpg")
                .organizationName("Red Cross")
                .tags(List.of(tagTemplate1))
                .build();

        TagTemplate tagTemplate2 = TagTemplate.builder()
                .tagId(2L)
                .tagName("Python")
                .build();

        testProjectTemplate2 = ProjectTemplate.builder()
                .projectId(2L)
                .projectTitle("Volunteer Project 2")
                .projectDescription("Second volunteer project")
                .imgPath("/path/to/image2.jpg")
                .organizationName("Habitat for Humanity")
                .tags(List.of(tagTemplate2))
                .build();

        TagTemplate tagTemplate3 = TagTemplate.builder()
                .tagId(3L)
                .tagName("JavaScript")
                .build();

        testProjectTemplate3 = ProjectTemplate.builder()
                .projectId(3L)
                .projectTitle("Volunteer Project 3")
                .projectDescription("Third volunteer project")
                .imgPath("/path/to/image3.jpg")
                .organizationName("Environmental Group")
                .tags(List.of(tagTemplate3))
                .build();
    }

    /**
     * All request should have a pair of headers to identify if the request is surely authenticated by gateway and authorized
     * X-User-Id : any integer
     * X-User-Role : ORGANIZATION | VOLUNTEER
     */

    // ========== GET /api/volunteers/{id}/projects ==========

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return all volunteer projects successfully")
    void testGetAllProjectsByVolunteer_Success() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate, testProjectTemplate2, testProjectTemplate3);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].projectId").value(1L))
                .andExpect(jsonPath("$[0].projectTitle").value("Volunteer Project 1"))
                .andExpect(jsonPath("$[1].projectId").value(2L))
                .andExpect(jsonPath("$[2].projectId").value(3L));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return empty list when volunteer has no projects")
    void testGetAllProjectsByVolunteer_EmptyList() throws Exception {
  
        long volunteerId = 999L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(Collections.emptyList());

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return single project")
    void testGetAllProjectsByVolunteer_SingleProject() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId").value(1L))
                .andExpect(jsonPath("$[0].projectTitle").value("Volunteer Project 1"));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return projects with correct volunteer data")
    void testGetAllProjectsByVolunteer_CorrectData() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectTitle").value("Volunteer Project 1"))
                .andExpect(jsonPath("$[0].projectDescription").value("First volunteer project"))
                .andExpect(jsonPath("$[0].organizationName").value("Red Cross"))
                .andExpect(jsonPath("$[0].imgPath").value("/path/to/image1.jpg"));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return projects with complete structure")
    void testGetAllProjectsByVolunteer_CompleteStructure() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").exists())
                .andExpect(jsonPath("$[0].projectTitle").exists())
                .andExpect(jsonPath("$[0].projectDescription").exists())
                .andExpect(jsonPath("$[0].imgPath").exists())
                .andExpect(jsonPath("$[0].organizationName").exists())
                .andExpect(jsonPath("$[0].tags").exists());

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should call service with correct volunteer ID")
    void testGetAllProjectsByVolunteer_CorrectId() throws Exception {
  
        long volunteerId = 42L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(List.of(testProjectTemplate));

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getAllVolunteerProjects(42L);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return 200 OK status")
    void testGetAllProjectsByVolunteer_OkStatus() throws Exception {
  
        long volunteerId = 200L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(List.of(testProjectTemplate));

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return JSON content type")
    void testGetAllProjectsByVolunteer_JsonContentType() throws Exception {
  
        long volunteerId = 200L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(List.of(testProjectTemplate));

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle projects with multiple tags")
    void testGetAllProjectsByVolunteer_MultipleTagsPerProject() throws Exception {
  
        long volunteerId = 200L;

        ProjectTemplate projectWithMultipleTags = ProjectTemplate.builder()
                .projectId(1L)
                .projectTitle("Multi-tag Project")
                .projectDescription("Project with multiple tags")
                .imgPath("/path/to/image.jpg")
                .organizationName("Organization")
                .tags(List.of(
                        TagTemplate.builder().tagId(1L).tagName("Java").build(),
                        TagTemplate.builder().tagId(2L).tagName("Python").build(),
                        TagTemplate.builder().tagId(3L).tagName("JavaScript").build()
                ))
                .build();

        List<ProjectTemplate> projects = List.of(projectWithMultipleTags);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags", hasSize(3)))
                .andExpect(jsonPath("$[0].tags[0].tagId").value(1L))
                .andExpect(jsonPath("$[0].tags[1].tagId").value(2L))
                .andExpect(jsonPath("$[0].tags[2].tagId").value(3L));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle projects without tags")
    void testGetAllProjectsByVolunteer_NoTags() throws Exception {
  
        long volunteerId = 200L;

        ProjectTemplate projectWithoutTags = ProjectTemplate.builder()
                .projectId(1L)
                .projectTitle("No Tags Project")
                .projectDescription("Project without tags")
                .imgPath("/path/to/image.jpg")
                .organizationName("Organization")
                .tags(Collections.emptyList())
                .build();

        List<ProjectTemplate> projects = List.of(projectWithoutTags);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags", hasSize(0)));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle invalid ID format")
    void testGetAllProjectsByVolunteer_InvalidIdFormat() throws Exception {
 
        mockMvc.perform(get("/api/volunteers/{id}/projects", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).getAllVolunteerProjects(anyLong());
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle zero ID")
    void testGetAllProjectsByVolunteer_ZeroId() throws Exception {
  
        long volunteerId = 0L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(Collections.emptyList());

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllVolunteerProjects(0L);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle negative ID")
    void testGetAllProjectsByVolunteer_NegativeId() throws Exception {
  
        long volunteerId = -1L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(Collections.emptyList());

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllVolunteerProjects(-1L);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle large ID")
    void testGetAllProjectsByVolunteer_LargeId() throws Exception {
  
        long largeId = Long.MAX_VALUE;
        when(projectService.getAllVolunteerProjects(largeId))
                .thenReturn(Collections.emptyList());

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", largeId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllVolunteerProjects(largeId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return projects from different organizations")
    void testGetAllProjectsByVolunteer_DifferentOrganizations() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(
                testProjectTemplate,      // Red Cross
                testProjectTemplate2,     // Habitat for Humanity
                testProjectTemplate3      // Environmental Group
        );
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

 
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].organizationName").value("Red Cross"))
                .andExpect(jsonPath("$[1].organizationName").value("Habitat for Humanity"))
                .andExpect(jsonPath("$[2].organizationName").value("Environmental Group"));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should verify correct service method is called")
    void testGetAllProjectsByVolunteer_VerifyServiceCall() throws Exception {
  
        long volunteerId = 200L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(Collections.emptyList());

  
        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"));

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
        verify(projectService, never()).getAllProjects();
        verify(projectService, never()).getAllOrganizationProjects(anyLong());
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should handle only GET method")
    void testGetAllProjectsByVolunteer_MethodOverride() throws Exception {
  
        long volunteerId = 200L;
        when(projectService.getAllVolunteerProjects(volunteerId))
                .thenReturn(List.of(testProjectTemplate));

        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getAllVolunteerProjects(volunteerId);
    }

    @Test
    @DisplayName("GET /api/volunteers/{id}/projects - Should return consistent results for same volunteer")
    void testGetAllProjectsByVolunteer_ConsistentResults() throws Exception {
  
        long volunteerId = 200L;
        List<ProjectTemplate> projects = List.of(testProjectTemplate, testProjectTemplate2);
        when(projectService.getAllVolunteerProjects(volunteerId)).thenReturn(projects);

        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/volunteers/{id}/projects", volunteerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(projectService, times(2)).getAllVolunteerProjects(volunteerId);
    }
}

