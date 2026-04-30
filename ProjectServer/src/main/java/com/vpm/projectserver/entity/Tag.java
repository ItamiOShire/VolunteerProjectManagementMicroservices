package com.vpm.projectserver.entity;


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

    @ManyToMany(
            mappedBy = "tags",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Set<Project> projects = new HashSet<>();

}
