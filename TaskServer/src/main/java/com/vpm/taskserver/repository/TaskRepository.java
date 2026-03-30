package com.vpm.taskserver.repository;

import com.vpm.taskserver.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> getTasksByProjectId(Long projectId);
    
    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.volunteerTasks vt
    WHERE vt.id.volunteerUserId = :volunteerId
""")
    List<Task> getTasksByVolunteerId(@Param("volunteerId")  Long volunteerId);

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.volunteerTasks vt
    WHERE t.projectId = :projectId AND vt.id.volunteerUserId = :volunteerId
""")
    List<Task> getTasksInProjectByVolunteerId(
            @Param("projectId") Long projectId,
            @Param("volunteerId") Long volunteerId
    );

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.taskSuggestions ts
    WHERE t.projectId = :projectId AND ts.id.volunteerUserId = :volunteerId
""")
    List<Task> getTaskSuggestionsInProjectByVolunteerId(
            @Param("projectId") long projectId,
            @Param("volunteerId") long volunteerId
    );

}
