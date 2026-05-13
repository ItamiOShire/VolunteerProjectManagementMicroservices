package com.vpm.projectserver.service;

import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Set<Tag> getTagsById(Set<Long> tagIds) {
        return new HashSet<>(tagRepository.getAllTagsWithProjects(tagIds));
    }

}
