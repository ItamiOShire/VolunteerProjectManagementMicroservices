package com.vpm.projectserver.integration;

import com.vpm.projectserver.config.IntegrationTestsDBConfig;
import com.vpm.projectserver.config.IntegrationTestsRabbitMQConfig;
import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.dto.response.VolunteerAssignedResponse;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.exception.project.VolunteerAlreadyAssignedToProjectException;
import com.vpm.projectserver.repository.ProjectRepository;
import com.vpm.projectserver.repository.TagRepository;
import com.vpm.projectserver.service.EventService;
import com.vpm.projectserver.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({
        IntegrationTestsDBConfig.class,
        IntegrationTestsRabbitMQConfig.class
})
@DisplayName("ProjectService Integration Tests")
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TagRepository tagRepository;

    @MockitoBean
    EventService eventService;

    private Project testProject1;
    private Project testProject2;
    private Tag testTag1;
    private Tag testTag2;


    /*
     * Test data
     */
    @BeforeEach
    public void setUp() {
        // Clear repositories before each test
        projectRepository.deleteAll();
        tagRepository.deleteAll();


        testTag1 = new Tag();
        testTag1.setName("Environment");


        testTag2 = new Tag();
        testTag2.setName("Community");


        testProject1 = Project.builder()
                .title("Clean Up Beach")
                .description("Help clean up the local beach")
                .imgPath("/images/beach.png")
                .organizationUserId(1L)
                .organizationName("Earth Care")
                .tags(Set.of(testTag1))
                .build();


        testProject2 = Project.builder()
                .title("Community Garden")
                .description("Build and maintain a community garden")
                .imgPath("/images/garden.png")
                .organizationUserId(1L)
                .organizationName("Earth Care")
                .tags(Set.of(testTag1, testTag2))
                .build();

        testTag2 = tagRepository.save(testTag2);
        testTag1 = tagRepository.save(testTag1);

        testProject1 = projectRepository.save(testProject1);
        testProject2 = projectRepository.save(testProject2);

    }

    /* ============ GET HTTP Method Tests ============ */

    @Test
    @DisplayName("Should retrieve all projects")
    public void testGetAllProjects() {
        // Act
        List<ProjectTemplate> projects = projectService.getAllProjects();

        // Assert
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.stream().anyMatch(p -> p.getProjectTitle().equals("Clean Up Beach")));
        assertTrue(projects.stream().anyMatch(p -> p.getProjectTitle().equals("Community Garden")));
    }

    @Test
    @DisplayName("Should retrieve empty list when no projects exist")
    public void testGetAllProjectsEmpty() {
        // Arrange
        projectRepository.deleteAll();

        // Act
        List<ProjectTemplate> projects = projectService.getAllProjects();

        // Assert
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve project by id")
    public void testGetProjectById() {
        // Act
        ProjectTemplate project = projectService.getProjectById(testProject1.getId());

        // Assert
        assertNotNull(project);
        assertEquals(testProject1.getId(), project.getItemId());
        assertEquals("Clean Up Beach", project.getProjectTitle());
        assertEquals("Earth Care", project.getOrganizationName());
        assertEquals("Help clean up the local beach", project.getProjectDescription());
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when project does not exist")
    public void testGetProjectByIdNotFound() {
        // Act & Assert
        assertThrows(NoSuchProjectException.class, () -> projectService.getProjectById(9999L));
    }

    @Test
    @DisplayName("Should retrieve all projects by organization id")
    public void testGetAllOrganizationProjects() {
        // Act
        List<ProjectTemplate> projects = projectService.getAllOrganizationProjects(1L);

        // Assert
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.stream().allMatch(p -> p.getOrganizationName().equals("Earth Care")));
    }

    @Test
    @DisplayName("Should return empty list when organization has no projects")
    public void testGetAllOrganizationProjectsEmpty() {
        // Act
        List<ProjectTemplate> projects = projectService.getAllOrganizationProjects(9999L);

        // Assert
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve all projects by volunteer id")
    public void testGetAllVolunteerProjects() {
        // Arrange - This requires volunteer-project association
        // For now, testing the empty case since volunteers are added separately

        // Act
        List<ProjectTemplate> projects = projectService.getAllVolunteerProjects(1L);

        // Assert
        assertNotNull(projects);
        // Initially empty as we haven't added volunteers
    }

    /* ============ POST HTTP Method Tests ============ */

     @Test
     @DisplayName("Should create a new project successfully")
     public void testCreateProject() {
         // Arrange
         CreateProjectRequest request = new CreateProjectRequest();
         request.setTitle("Tree Planting Drive");
         request.setDescription("Plant 100 trees in the city");
         request.setImgPath("/images/trees.png");
         request.setOrganizationUserId(2L);
         request.setOrganizationName("Green Future");
         request.setTagIds(Set.of(testTag1.getId(), testTag2.getId()));

         int initialCount = (int) projectRepository.count();

         // Act
         ProjectTemplate result = projectService.createProject(request);

         // Assert
         assertNotNull(result);
         assertEquals(initialCount + 1, projectRepository.count());

         assertEquals("Tree Planting Drive", result.getProjectTitle());
         assertEquals("Green Future", result.getOrganizationName());
         assertEquals(2, result.getTags().size());
     }

    /* ============ PUT/PATCH HTTP Method Tests ============ */

     @Test
     @DisplayName("Should update an existing project")
     public void testUpdateProject() {
         // Arrange
         ProjectTemplate updateTemplate = ProjectTemplate.builder()
                 .itemId(testProject1.getId())
                 .projectTitle("Updated Beach Cleanup")
                 .projectDescription("Help clean and maintain the beach")
                 .imgPath("/images/beach_updated.png")
                 .organizationName("Earth Care")
                 .tags(List.of(
                         TagTemplate.builder().itemId(testTag2.getId()).tagName("Community").build()
                 ))
                 .build();

         // Act
         ProjectTemplate result = projectService.updateProject(updateTemplate);

         // Assert
         assertNotNull(result);
         assertEquals("Updated Beach Cleanup", result.getProjectTitle());
         assertEquals("Help clean and maintain the beach", result.getProjectDescription());
         assertEquals(1, result.getTags().size());
         assertTrue(result.getTags().stream().anyMatch(t -> t.getItemId() == testTag2.getId()));
     }

    @Test
    @DisplayName("Should throw NoSuchProjectException when updating non-existent project")
    public void testUpdateProjectNotFound() {
        // Arrange
        ProjectTemplate updateTemplate = ProjectTemplate.builder()
                .itemId(9999L)
                .projectTitle("Non-existent Project")
                .build();

        // Act & Assert
        assertThrows(NoSuchProjectException.class, () -> projectService.updateProject(updateTemplate));
    }

     @Test
     @DisplayName("Should patch project with single field update")
     public void testPatchProjectSingleField() {
         // Arrange
         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "Patched Beach Cleanup");

         // Act
         ProjectTemplate result = projectService.patchProject(updates, testProject1.getId());

         // Assert
         assertNotNull(result);
         Optional<Project> patchedProject = projectRepository.findById(testProject1.getId());
         assertTrue(patchedProject.isPresent());
         assertEquals("Patched Beach Cleanup", patchedProject.get().getTitle());
         // Other fields should remain unchanged
         assertEquals("Help clean up the local beach", patchedProject.get().getDescription());
     }

     @Test
     @DisplayName("Should patch project with multiple field updates")
     public void testPatchProjectMultipleFields() {
         // Arrange
         Map<String, Object> updates = new HashMap<>();
         updates.put("title", "Comprehensive Cleanup");
         updates.put("description", "Clean, maintain, and beautify the beach");
         updates.put("imgPath", "/images/beach_new.png");

         // Act
         ProjectTemplate result = projectService.patchProject(updates, testProject1.getId());

         // Assert
         assertNotNull(result);
         Optional<Project> patchedProject = projectRepository.findById(testProject1.getId());
         assertTrue(patchedProject.isPresent());
         Project project = patchedProject.get();
         assertEquals("Comprehensive Cleanup", project.getTitle());
         assertEquals("Clean, maintain, and beautify the beach", project.getDescription());
         assertEquals("/images/beach_new.png", project.getImgPath());
     }

     @Test
     @DisplayName("Should patch project with tags update")
     public void testPatchProjectWithTags() {
         // Arrange
         Map<String, Object> updates = new HashMap<>();
         updates.put("tags", Set.of(testTag2.getId()));

         // Act
         ProjectTemplate result = projectService.patchProject(updates, testProject1.getId());

         // Assert
         assertNotNull(result);
         assertEquals(1, result.getTags().size());
         assertTrue(result.getTags().stream().anyMatch(t -> t.getItemId() == testTag2.getId()));
     }

    @Test
    @DisplayName("Should throw NoSuchProjectException when patching non-existent project")
    public void testPatchProjectNotFound() {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "New Title");

        // Act & Assert
        assertThrows(NoSuchProjectException.class, () -> projectService.patchProject(updates, 9999L));
    }

    /* ============ DELETE HTTP Method Tests ============ */

    @Test
    @DisplayName("Should delete an existing project")
    public void testDeleteProject() {
        // Arrange
        long projectIdToDelete = testProject1.getId();
        assertTrue(projectRepository.existsById(projectIdToDelete));

        // Act
        projectService.deleteProject(projectIdToDelete);

        // Assert
        assertFalse(projectRepository.existsById(projectIdToDelete));
        assertEquals(1, projectRepository.count());
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when deleting non-existent project")
    public void testDeleteProjectNotFound() {
        // Act & Assert
        assertThrows(NoSuchProjectException.class, () -> projectService.deleteProject(9999L));
    }

    @Test
    @DisplayName("Should delete all projects")
    public void testDeleteAllProjects() {
        // Arrange
        assertEquals(2, projectRepository.count());

        // Act
        projectService.deleteProject(testProject1.getId());
        projectService.deleteProject(testProject2.getId());

        // Assert
        assertEquals(0, projectRepository.count());
    }

    /* ============ Additional Integration Tests ============ */

    @Test
    @DisplayName("Should verify tags are correctly associated with project")
    public void testProjectTagAssociation() {
        // Act
        ProjectTemplate project = projectService.getProjectById(testProject2.getId());

        // Assert
        assertNotNull(project.getTags());
        assertEquals(2, project.getTags().size());

        Set<String> tagNames = project.getTags().stream()
                .map(TagTemplate::getTagName)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(tagNames.contains("Environment"));
        assertTrue(tagNames.contains("Community"));
    }

      @Test
      @DisplayName("Should maintain data consistency after multiple operations")
      public void testDataConsistencyAcrossOperations() {
          // Arrange
          int initialCount = (int) projectRepository.count();

          // Create a new project
          CreateProjectRequest newRequest = new CreateProjectRequest();
          newRequest.setTitle("Charity Run");
          newRequest.setDescription("Organize a charity run for the community");
          newRequest.setImgPath("/images/run.png");
          newRequest.setOrganizationUserId(4L);
          newRequest.setOrganizationName("Health Plus");
          newRequest.setTagIds(Set.of(testTag1.getId()));

          // Act
          ProjectTemplate createdResult = projectService.createProject(newRequest);
          List<ProjectTemplate> allProjects = projectService.getAllProjects();

          // Assert
          assertNotNull(createdResult);
          assertEquals(initialCount + 1, allProjects.size());
          assertEquals(initialCount + 1, projectRepository.count());
      }

    /* ============ Assign Volunteer to Project Tests ============ */

    @Nested
    @DisplayName("Assign Volunteer to Project Tests")
    class AssignVolunteerToProjectTests {

        private static final Long VOLUNTEER_ID_1 = 100L;
        private static final Long VOLUNTEER_ID_2 = 101L;
        private static final Long VOLUNTEER_ID_3 = 102L;

        @BeforeEach
        public void setUpAssignmentTests() {
            reset(eventService);
        }

        @Test
        @DisplayName("Should successfully assign volunteer to project")
        public void testSuccessfullyAssignVolunteerToProject() {
            // Arrange
            Long projectId = testProject1.getId();

            // Act
            VolunteerAssignedResponse response = projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert
            assertNotNull(response);
            assertEquals(VOLUNTEER_ID_1, response.getVolunteerId());
            assertEquals(projectId, response.getProjectId());

            // Verify event was sent
            verify(eventService, times(1)).sendEvent(
                any(),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
            );
        }

        @Test
        @DisplayName("Should persist volunteer assignment in database")
        public void testVolunteerAssignmentPersistedInDatabase() {
            // Arrange
            Long projectId = testProject1.getId();

            // Act
            projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert - Verify project now has the volunteer in its volunteers collection
            Project updatedProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            assertNotNull(updatedProject.getVolunteers());
            assertEquals(1, updatedProject.getVolunteers().size());
            assertTrue(updatedProject.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == VOLUNTEER_ID_1));
        }

        @Test
        @DisplayName("Should update project volunteers collection correctly")
        public void testUpdateProjectVolunteersCollection() {
            // Arrange
            Long projectId = testProject1.getId();
            Project initialProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            int initialVolunteerCount = initialProject.getVolunteers().size();

            // Act
            projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert
            Project updatedProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            assertEquals(initialVolunteerCount + 1, updatedProject.getVolunteers().size());
        }

        @Test
        @DisplayName("Should send event when volunteer is assigned to project")
        public void testEventSentWhenVolunteerAssigned() {
            // Arrange
            Long projectId = testProject1.getId();

            // Act
            projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert - Verify eventService.sendEvent was called
            verify(eventService).sendEvent(
                any(),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
            );
        }

        @Test
        @DisplayName("Should return correct VolunteerAssignedResponse")
        public void testReturnCorrectResponse() {
            // Arrange
            Long projectId = testProject1.getId();

            // Act
            VolunteerAssignedResponse response = projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert
            assertNotNull(response);
            assertEquals(VOLUNTEER_ID_1, response.getVolunteerId());
            assertEquals(projectId, response.getProjectId());
        }

        @Test
        @DisplayName("Should assign multiple volunteers to same project")
        public void testAssignMultipleVolunteersToSameProject() {
            // Arrange
            Long projectId = testProject1.getId();
            Long volunteerId1 = VOLUNTEER_ID_1;
            Long volunteerId2 = VOLUNTEER_ID_2;
            Long volunteerId3 = VOLUNTEER_ID_3;

            // Act
            projectService.assignVolunteerToProject(projectId, volunteerId1);
            projectService.assignVolunteerToProject(projectId, volunteerId2);
            projectService.assignVolunteerToProject(projectId, volunteerId3);

            // Assert
            Project updatedProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            assertEquals(3, updatedProject.getVolunteers().size());

            assertTrue(updatedProject.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == volunteerId1));
            assertTrue(updatedProject.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == volunteerId2));
            assertTrue(updatedProject.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == volunteerId3));

            // Verify event was sent 3 times
            verify(eventService, times(3)).sendEvent(
                any(),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
            );
        }

        @Test
        @DisplayName("Should assign same volunteer to multiple projects")
        public void testAssignSameVolunteerToMultipleProjects() {
            // Arrange
            Long volunteerId = VOLUNTEER_ID_1;
            Long projectId1 = testProject1.getId();
            Long projectId2 = testProject2.getId();

            // Act
            projectService.assignVolunteerToProject(projectId1, volunteerId);
            projectService.assignVolunteerToProject(projectId2, volunteerId);

            // Assert
            Project project1 = projectRepository.getProjectByIdWithVolunteers(projectId1).orElseThrow();
            Project project2 = projectRepository.getProjectByIdWithVolunteers(projectId2).orElseThrow();

            assertTrue(project1.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == volunteerId));
            assertTrue(project2.getVolunteers().stream()
                    .anyMatch(pv -> pv.getVolunteerUserId() == volunteerId));

            project2.getVolunteers().forEach(pv -> System.out.println("Project: " + pv.getProjectVolunteerId().getProjectId() + " Volunteer: " + pv.getVolunteerUserId()));

            assertEquals(1, project1.getVolunteers().size());
            assertEquals(1, project2.getVolunteers().size());

            // Verify event was sent twice
            verify(eventService, times(2)).sendEvent(
                any(),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
            );
        }

        @Test
        @DisplayName("Should throw NoSuchProjectException when project does not exist")
        public void testThrowExceptionWhenProjectNotFound() {
            // Arrange
            Long nonExistentProjectId = 9999L;

            // Act & Assert
            assertThrows(
                NoSuchProjectException.class,
                () -> projectService.assignVolunteerToProject(nonExistentProjectId, VOLUNTEER_ID_1)
            );

            // Verify event was NOT sent when project doesn't exist
            verify(eventService, never()).sendEvent(any(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should not assign same volunteer twice to same project successfully")
        public void testAssignSameVolunteerTwiceToSameProject() {
            // Arrange
            Long projectId = testProject1.getId();

            // Act - Assign twice (second assignment might replace or update the first)
            VolunteerAssignedResponse response1 = projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);
            assertThrows(
                VolunteerAlreadyAssignedToProjectException.class,
                () -> projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1)
            );

            // Assert - Both responses should be valid
            assertNotNull(response1);

            // Verify both assignments were processed
            Project updatedProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            assertNotNull(updatedProject.getVolunteers());

            // Events should be sent for both calls (or deduplicated depending on business logic)
            verify(eventService, atLeast(1)).sendEvent(
                any(),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
            );
        }

        @Test
        @DisplayName("Should verify ProjectVolunteer ID is correctly set")
        public void testProjectVolunteerIdCorrectlySet() {
            // Arrange
            Long projectId = testProject1.getId();
            Long volunteerId = VOLUNTEER_ID_1;

            // Act
            projectService.assignVolunteerToProject(projectId, volunteerId);

            // Assert
            Project updatedProject = projectRepository.getProjectByIdWithVolunteers(projectId).orElseThrow();
            var projectVolunteer = updatedProject.getVolunteers().stream()
                    .filter(pv -> pv.getVolunteerUserId() == volunteerId)
                    .findFirst()
                    .orElseThrow();

            assertNotNull(projectVolunteer.getProjectVolunteerId());
            assertEquals(projectId, projectVolunteer.getProjectVolunteerId().getProjectId());
            assertEquals(volunteerId.longValue(), projectVolunteer.getProjectVolunteerId().getVolunteerUserId());
        }

        @Test
        @DisplayName("Should maintain project data integrity after volunteer assignment")
        public void testProjectDataIntegrityAfterAssignment() {
            // Arrange
            Long projectId = testProject1.getId();

            Project originalProject = projectRepository.findById(projectId).orElseThrow();
            String originalTitle = originalProject.getTitle();
            String originalDescription = originalProject.getDescription();

            // Act
            projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);

            // Assert
            Project updatedProject = projectRepository.findById(projectId).orElseThrow();
            assertEquals(originalTitle, updatedProject.getTitle());
            assertEquals(originalDescription, updatedProject.getDescription());
            assertEquals(originalProject.getOrganizationUserId(), updatedProject.getOrganizationUserId());
            assertEquals(originalProject.getOrganizationName(), updatedProject.getOrganizationName());
        }

    }

}
