package com.vpm.volunteerserver.repository;

import com.vpm.volunteerserver.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    @Query("""
    SELECT v FROM Volunteer v
    WHERE v.userId = :volunteerUserId
""")
    Optional<Volunteer> findByUserId(Long volunteerUserId);

    @Query("""
    SELECT v FROM Volunteer v
    JOIN v.volunteerProjects vp
    ON vp.volunteerProjectId.projectId = :projectId
""")
    List<Volunteer> getVolunteersInProjectByProjectId(@Param("projectId") Long projectId);


    @Query("""
    SELECT v FROM Volunteer v
    JOIN v.volunteerProjects vp
    ON vp.volunteerProjectId.projectId = :projectId
    JOIN v.volunteerTasks vt
    ON vt.volunteerTaskId.taskId = :taskId
"""
    )
    List<Volunteer> getVolunteersInProjectAssignedToTask(
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId
    );


    /*
     * This query returns all volunteers in given project whether they are assigned to given task or not
     * By this, entity Volunteer is gonna have empty volunteerTasks list, which indicates that volunteer is not assigned to given task
     */
    @Query("""
    SELECT v FROM Volunteer v
    JOIN v.volunteerProjects vp
    ON vp.volunteerProjectId.projectId = :projectId
    LEFT JOIN FETCH v.volunteerTasks vt
    WHERE vt.volunteerTaskId.taskId = :taskId
""")
    List<Volunteer> getAllVolunteersInProjectAssignedOrNotToTask(
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId
    );


    @Query("""
    SELECT v FROM Volunteer v
    JOIN v.volunteerProjects vp
    ON vp.volunteerProjectId.projectId = :projectId
    JOIN FETCH v.volunteerTaskSuggestions vts
    WHERE vts.taskSuggestionId.taskId = :taskId
""")
    List<Volunteer> getVolunteersInProjectWhoReportedTaskSuggestion(
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId
    );

    @Query("""
    SELECT v FROM Volunteer v
    JOIN v.volunteerProjects vp
    ON vp.volunteerProjectId.projectId = :projectId
    LEFT JOIN v.volunteerTasks vt
    ON vt.volunteerTaskId.taskId = :taskId
    LEFT JOIN FETCH v.volunteerTaskSuggestions vts
    WHERE vts.taskSuggestionId.taskId = :taskId
""")
    List<Volunteer> getVolunteersInProjectNotAssignedToTaskWithTaskSuggestion(
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId
    );

}
