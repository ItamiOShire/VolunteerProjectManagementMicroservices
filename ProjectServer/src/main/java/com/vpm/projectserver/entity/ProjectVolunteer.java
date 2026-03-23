package com.vpm.projectserver.entity;


import com.vpm.projectserver.entity.pks.ProjectVolunteerId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_volunteer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVolunteer {

    @EmbeddedId
    private ProjectVolunteerId projectVolunteerId;

    @MapsId("projectId") // maps field 'projectId' in ProjectVolunteerId class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(
            name = "volunteer_id",
            insertable = false,
            updatable = false
    )
    private long volunteerId;
}
