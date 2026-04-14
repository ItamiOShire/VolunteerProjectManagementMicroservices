package com.vpm.projectserver.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProjectTemplate {

    /*
     * itemId field matches object's ID in database
     *
     * This is to help frontend use 'key' for mapping data to components
     */

    private long itemId;

    private String projectTitle;

    private String organizationName;

    private String projectDescription;

    private String imgPath;

    private List<TagTemplate> tags;

}
