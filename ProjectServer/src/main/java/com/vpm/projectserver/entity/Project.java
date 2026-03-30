package com.vpm.projectserver.entity;


import com.vpm.projectserver.dto.ProjectTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "title",
            nullable = false,
            length = 80
    )
    private String title;

    @Column(
            name = "description",
            nullable = false
    )
    private String description;

    @Column(
            name = "img_path",
            nullable = false,
            length = 400
    )
    private String imgPath;

    @Column(
            name = "organization_user_id",
            nullable = false
    )
    private long organizationUserId;

   /*
    * To minimalize synchronous communication between services to retrieve certain information, it is better to duplicate data
    * Especially if it is single field data - better efficiency, risk of inconsistency
    */

    @Column(
            name = "organization_name",
            nullable = false,
            length = 50
    )
    private String organizationName;

    @OneToMany(
            mappedBy = "project", // name of field in other table (with ManyToOne)
            orphanRemoval = true
    )
    private List<ProjectVolunteer> volunteers = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "project_tag",
            joinColumns =
                    @JoinColumn( name = "project_id"),
            inverseJoinColumns =
                    @JoinColumn( name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public void update(
            ProjectTemplate projectTemplate,
            Set<Tag> tags) {

        this.title = projectTemplate.getProjectTitle();
        this.description = projectTemplate.getProjectDescription();
        this.imgPath = projectTemplate.getImgPath();
        this.tags = tags;

    }

}
