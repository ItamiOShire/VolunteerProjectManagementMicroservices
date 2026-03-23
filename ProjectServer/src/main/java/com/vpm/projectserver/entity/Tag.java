package com.vpm.projectserver.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "name",
            nullable = false,
            length = 40
    )
    private String name;

    @ManyToMany
    @JoinTable(
            name = "project_tag",
            joinColumns =
                    @JoinColumn(name = "tag_id"),
            inverseJoinColumns =
                    @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

}
