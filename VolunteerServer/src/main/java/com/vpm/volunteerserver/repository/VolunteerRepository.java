package com.vpm.volunteerserver.repository;

import com.vpm.volunteerserver.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long volunteerUserId);
}
