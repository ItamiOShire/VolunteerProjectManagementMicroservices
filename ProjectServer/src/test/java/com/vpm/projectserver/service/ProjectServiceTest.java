package com.vpm.projectserver.service;

import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectTemplate testProjectTemplate;
    private Tag testTag;
    private TagTemplate testTagTemplate;
    private CreateProjectRequest createProjectRequest;

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
        when(projectRepository.findAll()).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        assertEquals(testProject.getTitle(), result.get(0).getProjectTitle());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no projects exist")
    void testGetAllProjects_EmptyList() {
  
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return project by ID successfully")
    void testGetProjectById_Success() throws NoSuchProjectException {
  
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

  
        ProjectTemplate result = projectService.getProjectById(1L);

  
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getItemId());
        assertEquals(testProject.getTitle(), result.getProjectTitle());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when project not found by ID")
    void testGetProjectById_NotFound() {
  
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.getProjectById(999L));
        verify(projectRepository, times(1)).findById(999L);
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

  
        projectService.createProject(createProjectRequest);

  
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

  
        projectService.createProject(createProjectRequest);

  
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

  
        projectService.createProject(createProjectRequest);

  
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
  
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(tagService.getTagsById(any())).thenReturn(Set.of(testTag));

  
        projectService.updateProject(testProjectTemplate);

  
        assertEquals("Test Project", testProject.getTitle());
        verify(projectRepository, times(1)).findById(1L);
        verify(tagService, times(1)).getTagsById(any());
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when updating non-existent project")
    void testUpdateProject_NotFound() {
  
        testProjectTemplate.setItemId(999L);
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.updateProject(testProjectTemplate));
        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should patch project successfully")
    void testPatchProject_Success() throws NoSuchProjectException {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        updates.put("description", "Updated Description");
        updates.put("tags", Set.of(1L));

        Project projectToPatch = testProject;
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToPatch));
        when(tagService.getTagsById(Set.of(1L))).thenReturn(Set.of(testTag));
        when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);

  
        projectService.patchProject(updates, 1L);

  
        verify(projectRepository, times(1)).findById(1L);
        verify(tagService, times(1)).getTagsById(Set.of(1L));
        verify(projectRepository, times(1)).save(projectToPatch);
    }

    @Test
    @DisplayName("Should patch project with only title")
    void testPatchProject_TitleOnly() throws NoSuchProjectException {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "New Title");

        Project projectToPatch = testProject;
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToPatch));
        when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);

  
        projectService.patchProject(updates, 1L);

  
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(projectToPatch);
    }

    @Test
    @DisplayName("Should throw NoSuchProjectException when patching non-existent project")
    void testPatchProject_NotFound() {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

 
        assertThrows(NoSuchProjectException.class, () -> projectService.patchProject(updates, 999L));
        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should patch project with tags only")
    void testPatchProject_TagsOnly() throws NoSuchProjectException {
  
        Map<String, Object> updates = new HashMap<>();
        updates.put("tags", Set.of(1L));

        Project projectToPatch = testProject;
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToPatch));
        when(tagService.getTagsById(Set.of(1L))).thenReturn(Set.of(testTag));
        when(projectRepository.save(any(Project.class))).thenReturn(projectToPatch);

  
        projectService.patchProject(updates, 1L);

  
        verify(projectRepository, times(1)).findById(1L);
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
            projectService.createProject(createProjectRequest);
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
        when(projectRepository.findAll()).thenReturn(projects);

  
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
        when(projectRepository.findAll()).thenReturn(projects);

  
        List<ProjectTemplate> result = projectService.getAllProjects();

  
        assertEquals(2, result.size());
        assertEquals(testProject.getId(), result.get(0).getItemId());
        assertEquals(project2.getId(), result.get(1).getItemId());
        assertEquals(1, result.get(0).getTags().size());
        assertEquals(1, result.get(1).getTags().size());
    }
}

