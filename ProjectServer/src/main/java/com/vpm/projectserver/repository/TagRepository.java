package com.vpm.projectserver.repository;

import com.vpm.projectserver.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("""
    SELECT t
    FROM Tag t
    LEFT JOIN FETCH t.projects
    WHERE t.id IN :tagIds
""")
    Set<Tag> getAllTagsWithProjects(@Param("tagIds") Set<Long> tagIds);

}
