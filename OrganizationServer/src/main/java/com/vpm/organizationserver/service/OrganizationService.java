package com.vpm.organizationserver.service;


import com.vpm.organizationserver.dto.request.CreateDescriptionRequest;
import com.vpm.organizationserver.dto.request.UpdateDescriptionRequest;
import com.vpm.organizationserver.dto.response.OrganizationDescriptionResponse;
import com.vpm.organizationserver.dto.response.OrganizationProfileResponse;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.entity.OrganizationDescription;
import com.vpm.organizationserver.entity.mapper.OrganizationDescriptionMapper;
import com.vpm.organizationserver.entity.mapper.OrganizationMapper;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationDescriptionException;
import com.vpm.organizationserver.exception.organization.NoSuchOrganizationException;
import com.vpm.organizationserver.repository.OrganizationDescriptionRepository;
import com.vpm.organizationserver.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationDescriptionRepository organizationDescriptionRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationDescriptionRepository organizationDescriptionRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationDescriptionRepository = organizationDescriptionRepository;
    }

    /*
     * GET HTTP method
     */

    public OrganizationProfileResponse getOrganizationProfile(
            long organizationUserId
    ) throws NoSuchOrganizationException {

        Organization organization = getOrganizationByUserId(
                organizationUserId
        );

        return OrganizationMapper
                .toOrganizationProfileResponse(organization);
    }

    public OrganizationDescriptionResponse getOrganizationDescription(
            long organizationUserId
    ) throws NoSuchOrganizationException {

        Organization organization = getOrganizationByUserId(
                organizationUserId
        );

        Optional<OrganizationDescription> organizationDescription =
                organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization);

        if (organizationDescription.isEmpty()) {
            log.warn("Organization with id {} does not have a description", organizationUserId);
            return OrganizationDescriptionResponse.builder()
                    .build();
        }

        return  OrganizationMapper
                .toOrganizationDescriptionResponse(organization);

    }

    /*
     * POST HTTP method
     */

    public OrganizationDescriptionResponse createOrganizationDescription(
            CreateDescriptionRequest request,
            long organizationUserId
    )  throws NoSuchOrganizationException {

        log.info("Creating organization description for organization with id {}", organizationUserId);

        Organization organization = getOrganizationByUserId(
                organizationUserId
        );

        OrganizationDescription organizationDescription = OrganizationDescriptionMapper
                .fromCreateDescriptionRequest(request, organization);

        organization.setOrganizationDescription(organizationDescription);
        organizationDescription.setOrganization(organization);

       return OrganizationDescriptionMapper
                       .toOrganizationDescriptionResponse(
                               organizationDescriptionRepository.save(organizationDescription)
                       );
    }

    /*
     * PUT / PATCH HTTP methods
     */

    public OrganizationDescriptionResponse updateOrganizationDescription(
            UpdateDescriptionRequest request,
            long organizationUserId
    ) throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {

        log.info("Updating organization description for organization with id {}", organizationUserId);

        Organization organization = getOrganizationByUserId(
                organizationUserId
        );

        OrganizationDescription organizationDescription = getOrganizationDescriptionByOrganization(
                organization
        );

        organizationDescription.update(
                request
        );

        return OrganizationDescriptionMapper
                .toOrganizationDescriptionResponse(
                        organizationDescriptionRepository.save(organizationDescription)
                );
    }

    public OrganizationDescriptionResponse patchOrganizationDescription(
            Map<String, Object> updates,
            long organizationUserId
    )  throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {

        log.info("Patching organization description for organization with id {}", organizationUserId);

        Organization organization = getOrganizationByUserId(
                organizationUserId
        );

        OrganizationDescription organizationDescription = getOrganizationDescriptionByOrganization(
                organization
        );

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(organizationDescription);

        log.info("Patching: {}", updates);

        beanWrapper.setPropertyValues(updates);

        return OrganizationDescriptionMapper
                .toOrganizationDescriptionResponse(
                        organizationDescriptionRepository.save(organizationDescription)
                );
    }

    private Organization getOrganizationByUserId(long organizationUserId) throws NoSuchOrganizationException {
        return organizationRepository
                .findByUserId(organizationUserId)
                .orElseThrow( () -> {
                            OrganizationServiceLogger.organizationNotFound(organizationUserId);
                            return new NoSuchOrganizationException(organizationUserId);
                        }
                );
    }

    private OrganizationDescription getOrganizationDescriptionByOrganization(Organization organization) throws NoSuchOrganizationDescriptionException {
        return organizationDescriptionRepository
                        .findOrganizationDescriptionByOrganization(organization)
                        .orElseThrow( () -> {
                            OrganizationServiceLogger.organizationDescriptionNotFound(organization.getUserId());
                            return new NoSuchOrganizationDescriptionException(organization.getId());
                        });
    }

    private static class OrganizationServiceLogger {

        public static void organizationNotFound(long id) {
            log.error("Organization with user id {} not found", id);
        }

        public static void organizationDescriptionNotFound(long id) {
            log.error("Cannot perform action. Organization with user id {} does not have a description", id);
        }

    }

}
