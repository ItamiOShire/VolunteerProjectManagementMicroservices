package com.vpm.projectserver.repository;

import com.vpm.projectserver.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
    SELECT p FROM Project p
    JOIN FETCH p.volunteers v
    WHERE v.volunteerUserId = :volunteerId
""")
    List<Project> getProjectsByVolunteerId(@Param("volunteerId") Long id);

    List<Project> getProjectsByOrganizationUserId(Long id);

}
