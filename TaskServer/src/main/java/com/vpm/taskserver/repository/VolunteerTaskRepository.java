package com.vpm.taskserver.repository;

import com.vpm.taskserver.entity.VolunteerTask;
import com.vpm.taskserver.entity.pks.VolunteerTaskId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerTaskRepository extends JpaRepository<VolunteerTask, VolunteerTaskId> {
}
