package com.vpm.projectserver.entity.mapper;

import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.TagTemplate;
import com.vpm.projectserver.entity.Project;

public class ProjectMapper {

    public static ProjectTemplate mapToProjectTemplateFromProjectEntity(Project project) {

        return ProjectTemplate.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .organizationName(project.getOrganizationName())
                .projectDescription(project.getDescription())
                .imgPath(project.getImgPath())
                .tags(
                        project.getTags().stream().map(
                                tag -> TagTemplate.builder()
                                        .tagId(tag.getId())
                                        .tagName(tag.getName())
                                        .build()
                        ).toList()
                ).build();

    }

}
