package com.vpm.volunteerserver.repository;

import com.vpm.volunteerserver.entity.TaskSuggestion;
import com.vpm.volunteerserver.entity.pks.TaskSuggestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskSuggestionRepository extends JpaRepository<TaskSuggestion, TaskSuggestionId> {
}
