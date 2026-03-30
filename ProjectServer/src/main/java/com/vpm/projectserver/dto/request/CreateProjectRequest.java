package com.vpm.projectserver.dto.request;


import lombok.Data;

import java.util.Set;

@Data
public class CreateProjectRequest {

    private String title;

    private String description;

    private String imgPath;

    private long organizationUserId;

    private String organizationName;

    private Set<Long> tagIds;

}
