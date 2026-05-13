package com.vpm.taskserver.api.internal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "project-service", url = "${services.project-service.url}")
public interface ProjectClient{

    @GetMapping("/api/internal/volunteers/{volunteerId}/projects/{projectId}")
    Boolean isVolunteerAssignedToProject(
            @PathVariable("volunteerId") Long volunteerId,
            @PathVariable("projectId") Long projectId
    );

}
