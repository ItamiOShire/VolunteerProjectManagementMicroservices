package com.vpm.organizationserver.repository;

import com.vpm.organizationserver.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByUserId(long organizationUserId);

    Optional<Organization> findByUserId(long organizationUserId);
}
