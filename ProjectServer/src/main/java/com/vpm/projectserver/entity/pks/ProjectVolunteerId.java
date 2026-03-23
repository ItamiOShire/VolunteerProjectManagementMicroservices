package com.vpm.projectserver.entity.pks;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVolunteerId implements Serializable {


    @Column(
            name = "project_id",
            nullable = false
    )
    private long projectId;

    @Column(
            name = "volunteer_id",
            nullable = false
    )
    private long volunteerId;

}
