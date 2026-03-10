package com.vpm.volunteerserver.repository;

import com.vpm.volunteerserver.entity.VolunteerProject;
import com.vpm.volunteerserver.entity.pks.VolunteerProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerProjectRepository extends JpaRepository<VolunteerProject, VolunteerProjectId> {
}
