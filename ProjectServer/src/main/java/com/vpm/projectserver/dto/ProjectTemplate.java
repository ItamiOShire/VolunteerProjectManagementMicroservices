package com.vpm.projectserver.dto;

import com.vpm.projectserver.entity.Tag;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProjectTemplate {

    private long projectId;

    private String projectTitle;

    private String organizationName;

    private String projectDescription;

    private String imgPath;

    private List<TagTemplate> tags;

}
