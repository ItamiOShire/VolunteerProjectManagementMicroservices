package com.vpm.projectserver.service;


import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.mapper.ProjectMapper;
import com.vpm.projectserver.exception.project.NoSuchProjectException;
import com.vpm.projectserver.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /*
     *
     * Returning ProjectTemplate DTO to prevent infinite JSON serialization and deserialization
     * Use ProjectMapper to map database entity to proper client data
     *
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

}
