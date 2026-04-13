package com.vpm.projectserver.service;


import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.entity.mapper.ProjectMapper;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TagService tagService;

    @Autowired
    public ProjectService(
            ProjectRepository projectRepository,
            TagService tagService) {
        this.projectRepository = projectRepository;
        this.tagService = tagService;
    }

    /*
     *
     * Returning ProjectTemplate DTO to prevent infinite JSON serialization and deserialization
     * Use ProjectMapper to map database entity to proper client data
     *
     */

    /*
     * GET HTTP method
     */

    public List<ProjectTemplate> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::mapToProjectTemplateFromProjectEntity)
                .toList();
    }

    public ProjectTemplate getProjectById(
            long id
    ) throws NoSuchProjectException {

        Optional<Project> project = projectRepository.findById(id);

        if(project.isEmpty()){
            log.error("Project with id {} not found", id);
            throw new NoSuchProjectException(id);
        }

        return  ProjectMapper.mapToProjectTemplateFromProjectEntity(
                project.get()
        );

    }

    public List<ProjectTemplate> getAllOrganizationProjects(
            long organizationId
    ) {
        List<ProjectTemplate> projects = projectRepository.getProjectsByOrganizationUserId(organizationId)
                .stream()
                .map(ProjectMapper::mapToProjectTemplateFromProjectEntity)
                .toList();

        if (projects.isEmpty()){
            log.warn("Organization with id {} does not have any projects or organization does not exists.", organizationId);
        }

            return projects;
    }

    public List<ProjectTemplate> getAllVolunteerProjects(
            long volunteerId
    ) {

        List<ProjectTemplate> projects = projectRepository.getProjectsByVolunteerId(volunteerId)
                .stream()
                .map(ProjectMapper::mapToProjectTemplateFromProjectEntity)
                .toList();

        if (projects.isEmpty()){
            log.warn("Volunteer with id {} does not have any projects or organization does not exists.", volunteerId);
        }

        return projects;
    }

    /*
     * POST HTTP method
     */

    public void createProject(
            CreateProjectRequest request
    ) {

        log.info("Creating project");

        Set<Tag> tags = tagService.getTagsById(request.getTagIds());

        if (tags.isEmpty()){
            log.warn("No tags were selected to project");
        }

        Project project = ProjectMapper.fromCreateProjectRequest(request, tags);

        projectRepository.save(project);

        log.info("Project with id {} created", project.getId());

    }

    /*
     * PUT / PATCH HTTP method
     */

    public void updateProject(
            ProjectTemplate projectTemplate
    ) throws NoSuchProjectException {

        log.info("Updating project with id {}", projectTemplate.getProjectId());

        Optional<Project> project = projectRepository.findById(projectTemplate.getProjectId());

        if (project.isEmpty()){
            log.error("Project with id {} does not exist", projectTemplate.getProjectId());
            throw new NoSuchProjectException(projectTemplate.getProjectId());
        }

        Set<Tag> tags = tagService.getTagsById(
                projectTemplate.getTags().stream()
                        .map(TagTemplate::getTagId)
                        .collect(Collectors.toSet())
        );

        if (tags.isEmpty()){
            log.warn("No tags were selected to project");
        }

        Project projectToUpdate = project.get();

        projectToUpdate.update(
                projectTemplate,
                tags
        );

    }

    public void patchProject (
            Map<String, Object> updates,
            long projectId
    ) throws NoSuchProjectException {

        log.info("Patching project with id {}", projectId);

        Optional<Project> project = projectRepository.findById(projectId);

        if (project.isEmpty()){
            log.error("Project with id {} does not exist", projectId);
            throw new NoSuchProjectException(projectId);
        }

        Project projectToPatch = project.get();

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(projectToPatch);

        if (updates.containsKey("tags")) {

            Set<Long> tagIds = (Set<Long>) updates.get("tags");

            Set<Tag> tags = tagService.getTagsById(tagIds);

            if (tags.isEmpty()){
                log.warn("No tags were selected to project");
            }

            beanWrapper.setPropertyValue("tags", tags);
            updates.remove("tags");

        }

        beanWrapper.setPropertyValues(updates);

        projectRepository.save(projectToPatch);

    }

    /*
     * DELETE HTTP method
     */

    public void deleteProject(
            long projectId
    ) {

        log.info("Deleting project with id {}", projectId);

        if (!projectRepository.existsById(projectId)){
            log.error("Project with id {} does not exist", projectId);
            throw new NoSuchProjectException(projectId);
        }

        projectRepository.deleteById(projectId);

    }

}
