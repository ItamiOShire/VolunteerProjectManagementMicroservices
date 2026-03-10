package com.vpm.volunteerserver.repository;

import com.vpm.volunteerserver.entity.VolunteerTask;
import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerTaskRepository extends JpaRepository<VolunteerTask, VolunteerTaskId> {
}
