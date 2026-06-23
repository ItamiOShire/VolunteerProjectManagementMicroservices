package com.vpm.taskserver.repository;

import com.vpm.taskserver.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// TODO: refactor queries as tables task_volunteer and task_suggestion got refactored and adjusted

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.priority
    WHERE t.id = :taskId
""")
    Optional<Task> getTaskById(@Param("taskId")Long id);

    @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.volunteerTasks
    WHERE t.id = :taskId
""")
    Optional<Task> getTaskByIdWithVolunteers(@Param("taskId") Long id);


    @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.priority
    WHERE t.projectId = :projectId
""")
    List<Task> getTasksByProjectId(@Param("projectId")Long projectId);

    @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.priority
""")
    List<Task> getAllTasksWithPriority();
    
    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.volunteerTasks vt
    LEFT JOIN FETCH t.priority
    WHERE vt.id.volunteerUserId = :volunteerId
""")
    List<Task> getTasksByVolunteerId(@Param("volunteerId")  Long volunteerId);

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.volunteerTasks vt
    LEFT JOIN FETCH t.priority
    WHERE t.projectId = :projectId AND vt.id.volunteerUserId = :volunteerId
""")
    List<Task> getTasksInProjectByVolunteerId(
            @Param("projectId") Long projectId,
            @Param("volunteerId") Long volunteerId
    );

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.taskSuggestions ts
    LEFT JOIN FETCH t.priority
    WHERE t.projectId = :projectId AND ts.id.volunteerUserId = :volunteerId
""")
    List<Task> getTaskSuggestionsInProjectByVolunteerId(
            @Param("projectId") long projectId,
            @Param("volunteerId") long volunteerId
    );

}
