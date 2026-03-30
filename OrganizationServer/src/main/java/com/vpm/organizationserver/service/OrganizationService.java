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

import java.util.HashMap;
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

        Optional<Organization> organization = organizationRepository.findByUserId(organizationUserId);

        if (organization.isEmpty()) {
            log.error("Organization with id {} not found", organizationUserId);
            throw new NoSuchOrganizationException(organizationUserId);
        }


        return OrganizationMapper
                .toOrganizationProfileResponse(organization.get());
    }

    public OrganizationDescriptionResponse getOrganizationDescription(
            long organizationUserId
    ) throws NoSuchOrganizationException {

        Optional<Organization> organization = organizationRepository.findByUserId(organizationUserId);

        if (organization.isEmpty()) {
            log.error("Organization with id {} not found", organizationUserId);
            throw new NoSuchOrganizationException(organizationUserId);
        }

        Optional<OrganizationDescription> organizationDescription =
                organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization.get());

        if (organizationDescription.isEmpty()) {
            log.warn("Organization with id {} does not have a description", organizationUserId);
            return OrganizationDescriptionResponse.builder()
                    .build();
        }

        return  OrganizationMapper
                .toOrganizationDescriptionResponse(organization.get());

    }

    /*
     * POST HTTP method
     */

    public void createOrganizationDescription(
            CreateDescriptionRequest request,
            long organizationUserId
    )  throws NoSuchOrganizationException {

        log.info("Creating organization description for organization with id {}", organizationUserId);

        Optional<Organization> organization = organizationRepository.findByUserId(organizationUserId);

        if (organization.isEmpty()) {
            log.error("Organization with id {} not found", organizationUserId);
            throw new NoSuchOrganizationException(organizationUserId);
        }

        OrganizationDescription organizationDescription = OrganizationDescriptionMapper
                .fromCreateDescriptionRequest(request, organization.get());

        organizationDescriptionRepository.save(organizationDescription);
    }

    // TODO: make logger class for similar logs and handler for empty entities

    /*
     * PUT / PATCH HTTP methods
     */

    public void updateOrganizationDescription(
            UpdateDescriptionRequest request,
            long organizationUserId
    ) throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {

        log.info("Updating organization description for organization with id {}", organizationUserId);

        Optional<Organization> organization = organizationRepository.findByUserId(organizationUserId);

        if (organization.isEmpty()) {

            log.error("Organization with id {} not found", organizationUserId);
            throw new NoSuchOrganizationException(organizationUserId);

        }

        Optional<OrganizationDescription> organizationDescription =
                organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization.get());


        if (organizationDescription.isEmpty()) {
            log.error("Cannot update. Organization with id {} does not have a description", organizationUserId);
            throw new NoSuchOrganizationDescriptionException(organizationUserId);
        }

        OrganizationDescription descriptionToUpdate = organizationDescription.get();

        descriptionToUpdate.update(
                request
        );

        organizationDescriptionRepository.save(descriptionToUpdate);
    }

    public void patchOrganizationDescription(
            Map<String, Object> updates,
            long organizationUserId
    )  throws NoSuchOrganizationException, NoSuchOrganizationDescriptionException {

        log.info("Patching organization description for organization with id {}", organizationUserId);

        Optional<Organization> organization = organizationRepository.findByUserId(organizationUserId);

        if (organization.isEmpty()) {
            log.error("Organization with id {} not found", organizationUserId);
            throw new NoSuchOrganizationException(organizationUserId);
        }

        Optional<OrganizationDescription> organizationDescription =
                organizationDescriptionRepository.findOrganizationDescriptionByOrganization(organization.get());

        if (organizationDescription.isEmpty()) {
            log.error("Cannot patch. Organization with id {} does not have a description", organizationUserId);
            throw new NoSuchOrganizationDescriptionException(organizationUserId);
        }

        OrganizationDescription descriptionToPatch = organizationDescription.get();

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(updates);

        log.info("Patching: {}", updates);

        beanWrapper.setPropertyValues(updates);

        organizationDescriptionRepository.save(descriptionToPatch);
    }

}
