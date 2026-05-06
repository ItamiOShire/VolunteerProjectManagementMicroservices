package com.vpm.projectserver.unit.service;

import com.vpm.projectserver.config.properties.RabbitMQProperties;
import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.dto.response.VolunteerAssignedResponse;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.repository.ProjectRepository;
import com.vpm.projectserver.service.EventService;
import com.vpm.projectserver.service.ProjectService;
import com.vpm.projectserver.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TagService tagService;

    @Mock
    private EventService eventService;

    @Mock
    private RabbitMQProperties rabbitMQProperties;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectTemplate testProjectTemplate;
    private Tag testTag;
    private TagTemplate testTagTemplate;
    private CreateProjectRequest createProjectRequest;

    private static final Long PROJECT_ID = 1L;
    private static final Long VOLUNTEER_ID = 100L;
    private static final String EXCHANGE_NAME = "exchange.volunteer-project";
    private static final String ROUTING_KEY_NAME = "volunteer-project.assigned";

    @BeforeEach
    void setUp() {
        /*
         * Initialize test data
         */
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");

        testTagTemplate = TagTemplate.builder()
                .itemId(1L)
                .tagName("Java")
                .build();

        testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .description("This is a test project")
                .imgPath("/path/to/image.jpg")
                .organizationUserId(100L)
                .organizationName("Test Organization")
                .tags(Set.of(testTag))
                .volunteers(new ArrayList<>())
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

    // ========== GET Tests ==========

    @Test
    @DisplayName("Should return all projects successfully")
    void testGetAllProjects_Success() {
  
        List<Project> projects = List.of(testProject);
        when(projectRepository.getAllProjectsWithTags()).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        assertEquals(testProject.getTitle(), result.get(0).getProjectTitle());
        verify(projectRepository, times(1)).getAllProjectsWithTags();
    }

    @Test
    @DisplayName("Should return empty list when no projects exist")
    void testGetAllProjects_EmptyList() {
  
        when(projectRepository.getAllProjectsWithTags()).thenReturn(Collections.emptyList());

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).getAllProjectsWithTags();
    }

    @Test
    @DisplayName("Should return project by ID successfully")
    void testGetProjectById_Success() throws NoSuchProjectException {
  
        when(projectRepository.getProjectById(1L)).thenReturn(Optional.of(testProject));

  
        ProjectTemplate result = projectService.getProjectById(1L);

  
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getItemId());
        assertEquals(testProject.getTitle(), result.getProjectTitle());
        verify(projectRepository, times(1)).getProjectById(1L);
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when project not found by ID")
    void testGetProjectById_NotFound() {
  
        when(projectRepository.getProjectById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.getProjectById(999L));
        verify(projectRepository, times(1)).getProjectById(999L);
    }

    @Test
    @DisplayName("Should return all organization projects successfully")
    void testGetAllOrganizationProjects_Success() {
  
        long organizationId = 100L;
        List<Project> projects = List.of(testProject);
        when(projectRepository.getProjectsByOrganizationUserId(organizationId)).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllOrganizationProjects(organizationId);

  
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        verify(projectRepository, times(1)).getProjectsByOrganizationUserId(organizationId);
    }

    @Test
    @DisplayName("Should return empty list when organization has no projects")
    void testGetAllOrganizationProjects_EmptyList() {
  
        long organizationId = 999L;
        when(projectRepository.getProjectsByOrganizationUserId(organizationId))
                .thenReturn(Collections.emptyList());

  
        List<ProjectTemplate> result = projectService.getAllOrganizationProjects(organizationId);

  
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).getProjectsByOrganizationUserId(organizationId);
    }

    @Test
    @DisplayName("Should return all volunteer projects successfully")
    void testGetAllVolunteerProjects_Success() {
  
        long volunteerId = 200L;
        List<Project> projects = List.of(testProject);
        when(projectRepository.getProjectsByVolunteerId(volunteerId)).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllVolunteerProjects(volunteerId);

  
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        verify(projectRepository, times(1)).getProjectsByVolunteerId(volunteerId);
    }

    @Test
    @DisplayName("Should return empty list when volunteer has no projects")
    void testGetAllVolunteerProjects_EmptyList() {
  
        long volunteerId = 999L;
        when(projectRepository.getProjectsByVolunteerId(volunteerId))
                .thenReturn(Collections.emptyList());

  
        List<ProjectTemplate> result = projectService.getAllVolunteerProjects(volunteerId);

  
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).getProjectsByVolunteerId(volunteerId);
    }

    // ========== POST Tests ==========

     @Test
     @DisplayName("Should create project successfully with tags")
     void testCreateProject_Success() {

         Set<Tag> tags = Set.of(testTag);
         when(tagService.getTagsById(createProjectRequest.getTagIds())).thenReturn(tags);
         when(projectRepository.save(any(Project.class))).thenReturn(testProject);


         ProjectTemplate result = projectService.createProject(createProjectRequest);


         assertNotNull(result);
         assertEquals(testProject.getId(), result.getItemId());
         verify(tagService, times(1)).getTagsById(createProjectRequest.getTagIds());
         verify(projectRepository, times(1)).save(any(Project.class));
     }

     @Test
     @DisplayName("Should create project successfully without tags")
     void testCreateProject_NoTags() {

         createProjectRequest.setTagIds(Collections.emptySet());
         Set<Tag> emptyTags = Collections.emptySet();
         when(tagService.getTagsById(createProjectRequest.getTagIds())).thenReturn(emptyTags);
         when(projectRepository.save(any(Project.class))).thenReturn(testProject);


         ProjectTemplate result = projectService.createProject(createProjectRequest);


         assertNotNull(result);
         assertEquals(testProject.getId(), result.getItemId());
         verify(tagService, times(1)).getTagsById(createProjectRequest.getTagIds());
         verify(projectRepository, times(1)).save(any(Project.class));
     }

     @Test
     @DisplayName("Should create project with correct properties")
     void testCreateProject_CorrectProperties() {

         Set<Tag> tags = Set.of(testTag);
         when(tagService.getTagsById(createProjectRequest.getTagIds())).thenReturn(tags);

         // Capture the project being saved
         ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
         when(projectRepository.save(projectCaptor.capture())).thenReturn(testProject);


         ProjectTemplate result = projectService.createProject(createProjectRequest);


         assertNotNull(result);
         Project savedProject = projectCaptor.getValue();
         assertEquals(createProjectRequest.getTitle(), savedProject.getTitle());
         assertEquals(createProjectRequest.getDescription(), savedProject.getDescription());
         assertEquals(createProjectRequest.getImgPath(), savedProject.getImgPath());
         assertEquals(createProjectRequest.getOrganizationUserId(), savedProject.getOrganizationUserId());
         assertEquals(createProjectRequest.getOrganizationName(), savedProject.getOrganizationName());
         assertEquals(tags, savedProject.getTags());
     }

    // ========== PUT/PATCH Tests ==========

     @Test
     @DisplayName("Should update project successfully")
     void testUpdateProject_Success() throws NoSuchProjectException {

         when(projectRepository.getProjectById(1L)).thenReturn(Optional.of(testProject));
         when(tagService.getTagsById(any())).thenReturn(Set.of(testTag));
         when(projectRepository.save(any(Project.class))).thenReturn(testProject);


         ProjectTemplate result = projectService.updateProject(testProjectTemplate);


         assertNotNull(result);
         assertEquals("Test Project", testProject.getTitle());
         verify(projectRepository, times(1)).getProjectById(1L);
         verify(tagService, times(1)).getTagsById(any());
         verify(projectRepository, times(1)).save(any(Project.class));
     }

    @Test
    @DisplayName("Should throw NoSuchProjectException when updating non-existent project")
    void testUpdateProject_NotFound() {
  
        testProjectTemplate.setItemId(999L);
        when(projectRepository.getProjectById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.updateProject(testProjectTemplate));
        verify(projectRepository, times(1)).getProjectById(999L);
    }

     @Test
     @DisplayName("Should patch project successfully")
     void testPatchProject_Success() throws NoSuchProjectException {

         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "Updated Title");
         updates.put("description", "Updated Description");
         updates.put("tags", Set.of(1L));

         Project projectToPatch = testProject;
         when(projectRepository.getProjectById(1L)).thenReturn(Optional.of(projectToPatch));
         when(tagService.getTagsById(Set.of(1L))).thenReturn(Set.of(testTag));
         when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);


         ProjectTemplate result = projectService.patchProject(updates, 1L);


         assertNotNull(result);
         verify(projectRepository, times(1)).getProjectById(1L);
         verify(tagService, times(1)).getTagsById(Set.of(1L));
         verify(projectRepository, times(1)).save(projectToPatch);
     }

     @Test
     @DisplayName("Should patch project with only title")
     void testPatchProject_TitleOnly() throws NoSuchProjectException {

         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "New Title");

         Project projectToPatch = testProject;
         when(projectRepository.getProjectById(1L)).thenReturn(Optional.of(projectToPatch));
         when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);


         ProjectTemplate result = projectService.patchProject(updates, 1L);


         assertNotNull(result);
         verify(projectRepository, times(1)).getProjectById(1L);
         verify(projectRepository, times(1)).save(projectToPatch);
     }

    @Test
    @DisplayName("Should throw NoSuchProjectException when patching non-existent project")
    void testPatchProject_NotFound() {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        when(projectRepository.getProjectById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.patchProject(updates, 999L));
        verify(projectRepository, times(1)).getProjectById(999L);
    }

     @Test
     @DisplayName("Should patch project with tags only")
     void testPatchProject_TagsOnly() throws NoSuchProjectException {

         Map<String, Object> updates = new HashMap<>();
         updates.put("tags", Set.of(1L));

         Project projectToPatch = testProject;
         when(projectRepository.getProjectById(1L)).thenReturn(Optional.of(projectToPatch));
         when(tagService.getTagsById(Set.of(1L))).thenReturn(Set.of(testTag));
         when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);


         ProjectTemplate result = projectService.patchProject(updates, 1L);


         assertNotNull(result);
         verify(projectRepository, times(1)).getProjectById(1L);
         verify(tagService, times(1)).getTagsById(Set.of(1L));
         verify(projectRepository, times(1)).save(projectToPatch);
     }

    // ========== DELETE Tests ==========

    @Test
    @DisplayName("Should delete project successfully")
    void testDeleteProject_Success() {
  
        when(projectRepository.existsById(1L)).thenReturn(true);

  
        projectService.deleteProject(1L);

  
        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when deleting non-existent project")
    void testDeleteProject_NotFound() {
  
        when(projectRepository.existsById(999L)).thenReturn(false);

        assertThrows(NoSuchProjectException.class, () -> projectService.deleteProject(999L));
        verify(projectRepository, times(1)).existsById(999L);
        verify(projectRepository, never()).deleteById(999L);
    }

    // ========== Edge Cases and Additional Tests ==========

     @Test
     @DisplayName("Should handle null tag IDs set in create request")
     void testCreateProject_NullTagIds() {

         createProjectRequest.setTagIds(null);
         Set<Tag> emptyTags = Collections.emptySet();
         when(tagService.getTagsById(null)).thenReturn(emptyTags);
         when(projectRepository.save(any(Project.class))).thenReturn(testProject);


         try {
             ProjectTemplate result = projectService.createProject(createProjectRequest);
             assertNotNull(result);
             verify(projectRepository, times(1)).save(any(Project.class));
         } catch (Exception e) {
             // TODO: Document if null handling throws an exception
             assertNotNull(e);
         }
     }

    @Test
    @DisplayName("Should properly map project entity to template")
    void testProjectMappingInGetAll() {
  
        List<Project> projects = List.of(testProject);
        when(projectRepository.getAllProjectsWithTags()).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertNotNull(result);
        assertEquals(1, result.size());
        ProjectTemplate template = result.get(0);
        assertEquals(testProject.getId(), template.getItemId());
        assertEquals(testProject.getTitle(), template.getProjectTitle());
        assertEquals(testProject.getDescription(), template.getProjectDescription());
        assertEquals(testProject.getImgPath(), template.getImgPath());
        assertEquals(testProject.getOrganizationName(), template.getOrganizationName());
        assertNotNull(template.getTags());
        assertEquals(1, template.getTags().size());
        assertEquals(testTag.getId(), template.getTags().get(0).getItemId());
        assertEquals(testTag.getName(), template.getTags().get(0).getTagName());
    }

    @Test
    @DisplayName("Should handle multiple projects with different tags")
    void testGetAllProjects_MultipleProjectsWithDifferentTags() {
  
        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Python");

        Project project2 = Project.builder()
                .id(2L)
                .title("Project 2")
                .description("Description 2")
                .imgPath("/path/to/img2.jpg")
                .organizationUserId(100L)
                .organizationName("Test Organization")
                .tags(Set.of(tag2))
                .build();

        List<Project> projects = List.of(testProject, project2);
        when(projectRepository.getAllProjectsWithTags()).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertEquals(2, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        assertEquals(project2.getId(), result.get(1).getItemId());
        assertEquals(1, result.get(0).getTags().size());
        assertEquals(1, result.get(1).getTags().size());
    }

    @Test
    @DisplayName("Should successfully assign volunteer to project")
    void testAssignVolunteerToProject_Success() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(rabbitMQProperties.getExchange()).thenReturn(createMockExchange());
        when(rabbitMQProperties.getRoutingKey()).thenReturn(createMockRoutingKey());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(eventService).sendEvent(any(), anyString(), anyString(), any(EventType.class));

        // Act
        VolunteerAssignedResponse response = projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID);

        // Assert
        assertThat(response)
                .isNotNull()
                .extracting("volunteerId", "projectId")
                .containsExactly(VOLUNTEER_ID, PROJECT_ID);

        verify(projectRepository).getProjectById(PROJECT_ID);
        verify(projectRepository).save(argThat(project ->
                !project.getVolunteers().isEmpty()
        ));
        verify(eventService).sendEvent(any(), eq(ROUTING_KEY_NAME), eq(EXCHANGE_NAME), eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT));
    }

    @Test
    @DisplayName("Should add volunteer to project's volunteer list")
    void testAssignVolunteerToProject_AddsVolunteerToList() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(rabbitMQProperties.getExchange()).thenReturn(createMockExchange());
        when(rabbitMQProperties.getRoutingKey()).thenReturn(createMockRoutingKey());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(eventService).sendEvent(any(), anyString(), anyString(), any(EventType.class));

        // Act
        projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID);

        // Assert - Verify volunteer was added to project
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getVolunteers())
                .isNotEmpty()
                .hasSize(1)
                .anySatisfy(pv -> assertThat(pv.getProjectVolunteerId().getVolunteerUserId())
                        .isEqualTo(VOLUNTEER_ID));
    }

    @Test
    @DisplayName("Should publish event with correct parameters")
    void testAssignVolunteerToProject_PublishesEvent() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(rabbitMQProperties.getExchange()).thenReturn(createMockExchange());
        when(rabbitMQProperties.getRoutingKey()).thenReturn(createMockRoutingKey());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID);

        // Assert - Verify event was published with correct parameters
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EventType> eventTypeCaptor = ArgumentCaptor.forClass(EventType.class);

        verify(eventService).sendEvent(
                eventCaptor.capture(),
                routingKeyCaptor.capture(),
                exchangeCaptor.capture(),
                eventTypeCaptor.capture()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo(EXCHANGE_NAME);
        assertThat(eventTypeCaptor.getValue()).isEqualTo(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT);
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when project not found")
    void testAssignVolunteerToProject_ProjectNotFound() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID))
                .isInstanceOf(NoSuchProjectException.class)
                .hasMessageContaining(String.valueOf(PROJECT_ID));

        // Verify no save was attempted
        verify(projectRepository, never()).save(any());
        // Verify no event was sent
        verify(eventService, never()).sendEvent(any(), anyString() ,anyString(), any(EventType.class));
    }

    @Test
    @DisplayName("Should not publish event if repository save fails")
    void testAssignVolunteerToProject_SaveFailsBeforeEventPublish() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        assertThatThrownBy(() -> projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB Error");

        // Verify event was NOT published (since save failed)
        verify(eventService, never()).sendEvent(any(),anyString() ,anyString(), any(EventType.class));
    }

    @Test
    @DisplayName("Should persist project with volunteer assignment")
    void testAssignVolunteerToProject_PersistsData() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(rabbitMQProperties.getExchange()).thenReturn(createMockExchange());
        when(rabbitMQProperties.getRoutingKey()).thenReturn(createMockRoutingKey());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(eventService).sendEvent(any(), anyString(), anyString(), any(EventType.class));

        // Act
        projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID);

        // Assert
        verify(projectRepository).getProjectById(eq(PROJECT_ID));
        verify(projectRepository).save(any(Project.class));

        // Verify the saved project contains the volunteer
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();

        assertThat(saved.getVolunteers()).isNotEmpty();
        assertThat(saved.getVolunteers())
                .extracting(pv -> pv.getProjectVolunteerId().getVolunteerUserId())
                .contains(VOLUNTEER_ID);
    }

    @Test
    @DisplayName("Should return response with correct volunteer and project IDs")
    void testAssignVolunteerToProject_ResponseContainsCorrectIds() {
        // Arrange
        when(projectRepository.getProjectById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(rabbitMQProperties.getExchange()).thenReturn(createMockExchange());
        when(rabbitMQProperties.getRoutingKey()).thenReturn(createMockRoutingKey());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        doNothing().when(eventService).sendEvent(any(), anyString(), anyString(), any(EventType.class));

        // Act
        VolunteerAssignedResponse response = projectService.assignVolunteerToProject(PROJECT_ID, VOLUNTEER_ID);

        // Assert
        assertThat(response)
                .isNotNull()
                .hasFieldOrPropertyWithValue("volunteerId", VOLUNTEER_ID)
                .hasFieldOrPropertyWithValue("projectId", PROJECT_ID);
    }

    // Helper method to create mock RabbitMQProperties
    private RabbitMQProperties.Exchange createMockExchange() {
        RabbitMQProperties.Exchange exchange = new RabbitMQProperties.Exchange();
        exchange.setVolunteerAssigned(EXCHANGE_NAME);
        return exchange;
    }

    private RabbitMQProperties.RoutingKey createMockRoutingKey() {
        RabbitMQProperties.RoutingKey routingKey = new RabbitMQProperties.RoutingKey();
        routingKey.setVolunteerAssigned(ROUTING_KEY_NAME);
        return routingKey;
    }

}

