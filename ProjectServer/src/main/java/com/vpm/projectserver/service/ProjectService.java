package com.vpm.projectserver.service;


import com.vpm.projectserver.config.properties.RabbitMQProperties;
import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.dto.response.VolunteerAssignedResponse;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.ProjectVolunteer;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.entity.mapper.EventMapper;
import com.vpm.projectserver.entity.mapper.ProjectMapper;
import com.vpm.projectserver.entity.pks.ProjectVolunteerId;
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
    private final EventService eventService;
    private final RabbitMQProperties rabbitMQProperties;

    @Autowired
    public ProjectService(
            ProjectRepository projectRepository,
            TagService tagService,
            EventService eventService,
            RabbitMQProperties rabbitMQProperties
    ) {
        this.projectRepository = projectRepository;
        this.tagService = tagService;
        this.eventService = eventService;
        this.rabbitMQProperties = rabbitMQProperties;
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
        return projectRepository.getAllProjectsWithTags().stream()
                .map(ProjectMapper::mapToProjectTemplateFromProjectEntity)
                .toList();
    }

    public ProjectTemplate getProjectById(
            long id
    ) throws NoSuchProjectException {

        Project project = findProjectById(id);


        return  ProjectMapper.mapToProjectTemplateFromProjectEntity(
                project
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

    public ProjectTemplate createProject(
            CreateProjectRequest request
    ) {

        log.info("Creating project");

        Set<Tag> tags = tagService.getTagsById(request.getTagIds());

        logIfEmptyTags(tags);

        Project project = ProjectMapper.fromCreateProjectRequest(request, tags);

        project = projectRepository.save(project);

        log.info("Project with id {} created", project.getId());

        return  ProjectMapper.mapToProjectTemplateFromProjectEntity(project);
    }

    /*
     * This handles asynchronous event
     * 1. Create VolunteerProject locally, assign object to Project and save project
     * 2. use EventService to handle sending event
     */

    public VolunteerAssignedResponse assignVolunteerToProject(
            Long projectId,
            Long volunteerId
    ) throws NoSuchProjectException {

        Project project = findProjectById(projectId);

        ProjectVolunteer projectVolunteer = new ProjectVolunteer(
                new ProjectVolunteerId(projectId, volunteerId),
                project,
                volunteerId
        );

        project.getVolunteers().add(projectVolunteer);

        projectRepository.save(project);

        log.info("Volunteer with id {} assigned to project with id {}", volunteerId, projectId);

        eventService.sendEvent(
                EventMapper.formProjectVolunteer(projectVolunteer),
                rabbitMQProperties.getExchange().getVolunteerAssigned(),
                EventType.VOLUNTEER_ASSIGNED_TO_PROJECT
        );

        return ProjectMapper
                .fromProjectVolunteerToVolunteerAssignedResponse(projectVolunteer);

    }

    /*
     * PUT / PATCH HTTP method
     */

    public ProjectTemplate updateProject(
            ProjectTemplate projectTemplate
    ) throws NoSuchProjectException {

        log.info("Updating project with id {}", projectTemplate.getItemId());

        Project project = findProjectById(
                projectTemplate.getItemId()
        );

        Set<Tag> tags = tagService.getTagsById(
                projectTemplate.getTags().stream()
                        .map(TagTemplate::getItemId)
                        .collect(Collectors.toSet())
        );

        logIfEmptyTags(tags);

        project.update(
                projectTemplate,
                tags
        );

        return ProjectMapper.mapToProjectTemplateFromProjectEntity(
                projectRepository.save(project)
        );

    }

    public ProjectTemplate patchProject (
            Map<String, Object> updates,
            long projectId
    ) throws NoSuchProjectException {

        log.info("Patching project with id {}", projectId);

        Project project = findProjectById(
                projectId
        );

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(project);

        if (updates.containsKey("tags")) {

            Set<Long> tagIds = (Set<Long>) updates.get("tags");

            Set<Tag> tags = tagService.getTagsById(tagIds);

            logIfEmptyTags(tags);

            beanWrapper.setPropertyValue("tags", tags);
            updates.remove("tags");

        }

        beanWrapper.setPropertyValues(updates);

        return ProjectMapper.mapToProjectTemplateFromProjectEntity(
                projectRepository.save(project)
        );
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

    /*
     * Helper methods
     */

    private Project findProjectById(
            long projectId
    ) throws NoSuchProjectException {

        return projectRepository
                .getProjectById(projectId)
                .orElseThrow( () -> {
                    log.error("Project with id {} does not exist", projectId);
                    return new NoSuchProjectException(projectId);
                });

    }

    private void logIfEmptyTags(Set<Tag> tags) {
        if (tags.isEmpty()){
            log.warn("No tags were selected to project");
        }
    }

}
