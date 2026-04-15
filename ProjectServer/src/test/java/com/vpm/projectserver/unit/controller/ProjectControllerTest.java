package com.vpm.projectserver.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.projectserver.controller.ProjectController;
import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@DisplayName("ProjectController Unit Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectTemplate testProjectTemplate;
    private CreateProjectRequest createProjectRequest;
    private TagTemplate testTagTemplate;

    @BeforeEach
    void setUp() {
        testTagTemplate = TagTemplate.builder()
                .itemId(1L)
                .tagName("Java")
                .build();

        testProjectTemplate = ProjectTemplate.builder()
                .itemId(1L)
                .projectTitle("Test Project")
                .projectDescription("This is a test project")
                .imgPath("/path/to/image.jpg")
                .organizationName("Test Organization")
                .tags(List.of(testTagTemplate))
                .build();

        createProjectRequest = new CreateProjectRequest();
        createProjectRequest.setTitle("New Project");
        createProjectRequest.setDescription("Description");
        createProjectRequest.setImgPath("/path/to/img.jpg");
        createProjectRequest.setOrganizationUserId(100L);
        createProjectRequest.setOrganizationName("Test Organization");
        createProjectRequest.setTagIds(Set.of(1L));
    }

    /**
     * All request should have a pair of headers to identify if the request is surely authenticated by gateway and authorized
     * X-User-Id : any integer
     * X-User-Role : ORGANIZATION | VOLUNTEER
     */

    // ========== GET /api/projects ==========

    @Test
    @DisplayName("GET /api/projects - Should return all projects successfully")
    void testGetAllProjects_Success() throws Exception {

        List<ProjectTemplate> projects = List.of(testProjectTemplate);
        when(projectService.getAllProjects()).thenReturn(projects);

 
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemId").value(1L))
                .andExpect(jsonPath("$[0].projectTitle").value("Test Project"))
                .andExpect(jsonPath("$[0].organizationName").value("Test Organization"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("GET /api/projects - Should return empty list when no projects exist")
    void testGetAllProjects_EmptyList() throws Exception {
  
        when(projectService.getAllProjects()).thenReturn(Collections.emptyList());

 
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("GET /api/projects - Should return multiple projects with correct structure")
    void testGetAllProjects_MultipleProjects() throws Exception {
  
        ProjectTemplate project2 = ProjectTemplate.builder()
                .itemId(2L)
                .projectTitle("Project 2")
                .projectDescription("Description 2")
                .imgPath("/path/to/img2.jpg")
                .organizationName("Organization 2")
                .tags(Collections.emptyList())
                .build();

        List<ProjectTemplate> projects = List.of(testProjectTemplate, project2);
        when(projectService.getAllProjects()).thenReturn(projects);

 
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemId").value(1L))
                .andExpect(jsonPath("$[1].itemId").value(2L))
                .andExpect(jsonPath("$[0].tags", hasSize(1)))
                .andExpect(jsonPath("$[1].tags", hasSize(0)));

        verify(projectService, times(1)).getAllProjects();
    }

    // ========== GET /api/projects/{id} ==========

    @Test
    @DisplayName("GET /api/projects/{id} - Should return project by ID successfully")
    void testGetProjectById_Success() throws Exception {
  
        when(projectService.getProjectById(1L)).thenReturn(testProjectTemplate);

 
        mockMvc.perform(get("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.projectTitle").value("Test Project"))
                .andExpect(jsonPath("$.projectDescription").value("This is a test project"))
                .andExpect(jsonPath("$.imgPath").value("/path/to/image.jpg"))
                .andExpect(jsonPath("$.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].itemId").value(1L))
                .andExpect(jsonPath("$.tags[0].tagName").value("Java"));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    @DisplayName("GET /api/projects/{id} - Should return 404 when project not found")
    void testGetProjectById_NotFound() throws Exception {
  
        when(projectService.getProjectById(999L))
                .thenThrow(new NoSuchProjectException(999L));

 
        mockMvc.perform(get("/api/projects/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectById(999L);
    }

    @Test
    @DisplayName("GET /api/projects/{id} - Should handle string ID as number")
    void testGetProjectById_InvalidIdFormat() throws Exception {
 
        mockMvc.perform(get("/api/projects/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== POST /api/projects ==========

     @Test
     @DisplayName("POST /api/projects - Should create project successfully")
     void testCreateProject_Success() throws Exception {

         when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(testProjectTemplate);


         mockMvc.perform(post("/api/projects")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(createProjectRequest)))
                 .andExpect(status().isCreated())
                 .andExpect(jsonPath("$.itemId").value(1L))
                 .andExpect(jsonPath("$.projectTitle").value("Test Project"));

         verify(projectService, times(1)).createProject(any(CreateProjectRequest.class));
     }

     @Test
     @DisplayName("POST /api/projects - Should create project with correct request body")
     void testCreateProject_VerifyRequestBody() throws Exception {

         when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(testProjectTemplate);


         mockMvc.perform(post("/api/projects")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(createProjectRequest)))
                 .andExpect(status().isCreated());


         verify(projectService, times(1)).createProject(argThat(request ->
                 request.getTitle().equals("New Project") &&
                 request.getDescription().equals("Description") &&
                 request.getOrganizationUserId() == 100L &&
                 request.getTagIds().contains(1L)
         ));
     }

     @Test
     @DisplayName("POST /api/projects - Should create project without tags")
     void testCreateProject_NoTags() throws Exception {

         createProjectRequest.setTagIds(Collections.emptySet());
         when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(testProjectTemplate);


         mockMvc.perform(post("/api/projects")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(createProjectRequest)))
                 .andExpect(status().isCreated());

         verify(projectService, times(1)).createProject(any(CreateProjectRequest.class));
     }

    @Test
    @DisplayName("POST /api/projects - Should return 400 for invalid request body")
    void testCreateProject_InvalidRequestBody() throws Exception {
  
        String invalidJson = "{ invalid json }";

 
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(any());
    }

    @Test
    @DisplayName("POST /api/projects - Should handle null request body")
    void testCreateProject_NullRequest() throws Exception {
 
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content("null"))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(any());
    }

    // ========== PUT /api/projects/{id} ==========

     @Test
     @DisplayName("PUT /api/projects/{id} - Should update project successfully")
     void testUpdateProject_Success() throws Exception {

         testProjectTemplate.setItemId(1L);
         when(projectService.updateProject(any(ProjectTemplate.class))).thenReturn(testProjectTemplate);


         mockMvc.perform(put("/api/projects/1")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(testProjectTemplate)))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.itemId").value(1L));

         verify(projectService, times(1)).updateProject(any(ProjectTemplate.class));
     }

    @Test
    @DisplayName("PUT /api/projects/{id} - Should handle project not found exception")
    void testUpdateProject_NotFound() throws Exception {
  
        testProjectTemplate.setItemId(999L);
        doThrow(new NoSuchProjectException(999L))
                .when(projectService).updateProject(any(ProjectTemplate.class));

 
        mockMvc.perform(put("/api/projects/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content(objectMapper.writeValueAsString(testProjectTemplate)))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).updateProject(any(ProjectTemplate.class));
    }

     @Test
     @DisplayName("PUT /api/projects/{id} - Should update project with all fields")
     void testUpdateProject_AllFields() throws Exception {

         testProjectTemplate.setItemId(1L);
         testProjectTemplate.setProjectTitle("Updated Title");
         testProjectTemplate.setProjectDescription("Updated Description");
         when(projectService.updateProject(any(ProjectTemplate.class))).thenReturn(testProjectTemplate);


         mockMvc.perform(put("/api/projects/1")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(testProjectTemplate)))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.projectTitle").value("Updated Title"));

         verify(projectService, times(1)).updateProject(argThat(template ->
                 template.getProjectTitle().equals("Updated Title") &&
                 template.getProjectDescription().equals("Updated Description")
         ));
     }

    @Test
    @DisplayName("PUT /api/projects/{id} - Should return 400 for invalid JSON")
    void testUpdateProject_InvalidJSON() throws Exception {
 
        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content("{ invalid }"))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).updateProject(any());
    }

    // ========== PATCH /api/projects/{id} ==========

     @Test
     @DisplayName("PATCH /api/projects/{id} - Should patch project successfully")
     @SuppressWarnings("unchecked")
     void testPatchProject_Success() throws Exception {

         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "Updated Title");
         when(projectService.patchProject(any(Map.class), anyLong())).thenReturn(testProjectTemplate);


         mockMvc.perform(patch("/api/projects/1")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(updates)))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.itemId").value(1L));

         verify(projectService, times(1)).patchProject(any(Map.class), eq(1L));
     }

     @Test
     @DisplayName("PATCH /api/projects/{id} - Should patch with multiple fields")
     @SuppressWarnings("unchecked")
     void testPatchProject_MultipleFields() throws Exception {

         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "New Title");
         updates.put("description", "New Description");
         updates.put("tags", Set.of(1L, 2L));
         when(projectService.patchProject(any(Map.class), anyLong())).thenReturn(testProjectTemplate);


         mockMvc.perform(patch("/api/projects/1")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(updates)))
                 .andExpect(status().isOk());

         verify(projectService, times(1)).patchProject(any(Map.class), eq(1L));
     }

    @Test
    @DisplayName("PATCH /api/projects/{id} - Should handle project not found")
    @SuppressWarnings("unchecked")
    void testPatchProject_NotFound() throws Exception {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        doThrow(new NoSuchProjectException(999L))
                .when(projectService).patchProject(any(Map.class), eq(999L));

 
        mockMvc.perform(patch("/api/projects/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).patchProject(any(Map.class), eq(999L));
    }

     @Test
     @DisplayName("PATCH /api/projects/{id} - Should handle empty updates")
     @SuppressWarnings("unchecked")
     void testPatchProject_EmptyUpdates() throws Exception {

         Map<String, Object> updates = new HashMap<>();
         when(projectService.patchProject(any(Map.class), anyLong())).thenReturn(testProjectTemplate);


         mockMvc.perform(patch("/api/projects/1")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("X-User-Id", 12)
                 .header("X-User-Role", "ORGANIZATION")
                 .content(objectMapper.writeValueAsString(updates)))
                 .andExpect(status().isOk());

         verify(projectService, times(1)).patchProject(any(Map.class), eq(1L));
     }

    @Test
    @DisplayName("PATCH /api/projects/{id} - Should return 400 for invalid JSON")
    @SuppressWarnings("unchecked")
    void testPatchProject_InvalidJSON() throws Exception {
 
        mockMvc.perform(patch("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .content("{ invalid }"))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).patchProject(any(), anyLong());
    }

    // ========== DELETE /api/projects/{id} ==========

    @Test
    @DisplayName("DELETE /api/projects/{id} - Should delete project successfully")
    void testDeleteProject_Success() throws Exception {
  
        doNothing().when(projectService).deleteProject(1L);

 
        mockMvc.perform(delete("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} - Should handle project not found")
    void testDeleteProject_NotFound() throws Exception {
  
        doThrow(new NoSuchProjectException(999L))
                .when(projectService).deleteProject(999L);

 
        mockMvc.perform(delete("/api/projects/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).deleteProject(999L);
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} - Should call service with correct ID")
    void testDeleteProject_CorrectId() throws Exception {
  
        doNothing().when(projectService).deleteProject(42L);

 
        mockMvc.perform(delete("/api/projects/42")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).deleteProject(42L);
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} - Should handle invalid ID format")
    void testDeleteProject_InvalidIdFormat() throws Exception {
 
        mockMvc.perform(delete("/api/projects/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).deleteProject(anyLong());
    }

    // ========== Content Type Tests ==========

    @Test
    @DisplayName("Should accept application/json content type")
    void testContentType_ApplicationJson() throws Exception {
  
        when(projectService.getAllProjects()).thenReturn(List.of(testProjectTemplate));

 
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(projectService, times(1)).getAllProjects();
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("GET /api/projects - Should handle zero as project ID")
    void testGetProjectById_ZeroId() throws Exception {
  
        when(projectService.getProjectById(0L))
                .thenThrow(new NoSuchProjectException(0L));

 
        mockMvc.perform(get("/api/projects/0")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/projects - Should handle negative ID")
    void testGetProjectById_NegativeId() throws Exception {
  
        when(projectService.getProjectById(-1L))
                .thenThrow(new NoSuchProjectException(-1L));

 
        mockMvc.perform(get("/api/projects/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle large project ID")
    void testGetProjectById_LargeId() throws Exception {
  
        long largeId = Long.MAX_VALUE;
        when(projectService.getProjectById(largeId))
                .thenThrow(new NoSuchProjectException(largeId));

 
        mockMvc.perform(get("/api/projects/" + largeId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", 12)
                .header("X-User-Role", "ORGANIZATION"))
                .andExpect(status().isNotFound());
    }
}

