package com.vpm.volunteerserver.entity;


import com.vpm.volunteerserver.entity.pks.VolunteerProjectId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "volunteer_project")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolunteerProject {

    @EmbeddedId
    private VolunteerProjectId volunteerProjectId;

    @MapsId("volunteerUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "volunteer_user_id",
            referencedColumnName = "user_id"
            // Resolved: BUG! hibernate demands referenced columns to be primary key of that entity
            //       it forbids to use any other field than primary key - otherwise, this leads to unpredictable behaviours, like generating non-existing sequences for referenced columns
    )
    private Volunteer volunteer;

    @Column(
            name = "project_id",
            insertable = false,
            updatable = false
    )
    private long projectId;

}
