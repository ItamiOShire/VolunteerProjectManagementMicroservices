package com.vpm.organizationserver.repository;

import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.entity.OrganizationDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationDescriptionRepository extends JpaRepository<OrganizationDescription, Long> {

    Optional<OrganizationDescription> findOrganizationDescriptionByOrganization(Organization organization);
}
