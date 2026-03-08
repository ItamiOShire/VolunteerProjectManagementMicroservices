package com.vpm.volunteerserver.entity.pks;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VolunteerProjectId implements Serializable {

    @Column(
            name = "volunteer_id",
            nullable = false
    )
    private long volunteerId;

    @Column(
            name = "project_id",
            nullable = false
    )
    private long projectId;

}
