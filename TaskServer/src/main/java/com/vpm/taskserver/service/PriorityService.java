package com.vpm.taskserver.service;


import com.vpm.taskserver.dto.template.PriorityTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.mapper.PriorityMapper;
import com.vpm.taskserver.repository.PriorityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PriorityService {

    private final PriorityRepository priorityRepository;

    @Autowired
    public PriorityService(PriorityRepository priorityRepository) {
        this.priorityRepository = priorityRepository;
    }

    /*
     * GET HTTP method
     */

    public List<PriorityTemplate> getAllPriorities() {

        return priorityRepository.findAll().stream()
                .map(PriorityMapper::toPriorityTemplate)
                .toList();
    }

    public Optional<Priority> getPriorityById(long id) {
        return priorityRepository.findById(id);
    }

}
