package com.vpm.projectserver.entity.mapper;

import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;

import java.util.Set;

public class ProjectMapper {

    public static ProjectTemplate mapToProjectTemplateFromProjectEntity(Project project) {

        return ProjectTemplate.builder()
                .itemId(project.getId())
                .projectTitle(project.getTitle())
                .organizationName(project.getOrganizationName())
                .projectDescription(project.getDescription())
                .imgPath(project.getImgPath())
                .tags(
                        project.getTags().stream().map(
                                tag -> TagTemplate.builder()
                                        .itemId(tag.getId())
                                        .tagName(tag.getName())
                                        .build()
                        ).toList()
                ).build();

    }

    public static Project fromCreateProjectRequest(
            CreateProjectRequest request,
            Set<Tag> tags) {

        return Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imgPath(request.getImgPath())
                .organizationUserId(request.getOrganizationUserId())
                .organizationName(request.getOrganizationName())
                .tags(tags)
                .build();

    }

}
