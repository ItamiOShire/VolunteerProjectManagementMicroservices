package com.vpm.projectserver.repository;

import com.vpm.projectserver.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
    SELECT p FROM Project p
    JOIN FETCH p.volunteers v
    JOIN FETCH p.tags
    WHERE v.volunteerUserId = :volunteerId
""")
    List<Project> getProjectsByVolunteerId(@Param("volunteerId") Long id);

    /**
     * Solves N+1 queries problem - use this on entities where you use relations mapping (n-m, n-1)
     * @return
     */
    @Query("""
    SELECT p FROM Project p
    LEFT JOIN FETCH p.tags
""")
    List<Project> getAllProjectsWithTags();

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN FETCH p.tags
    WHERE p.id = :projectId
""")
    Optional<Project> getProjectById(@Param("projectId")Long id);

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN FETCH p.volunteers
    WHERE p.id = :projectId
""")
    Optional<Project> getProjectByIdWithVolunteers(@Param("projectId")Long id);

    @Query("""
    SELECT p FROM Project p
    JOIN FETCH p.tags
    WHERE p.organizationUserId = :organizationUserId
""")
    List<Project> getProjectsByOrganizationUserId(@Param("organizationUserId") Long id);

}
