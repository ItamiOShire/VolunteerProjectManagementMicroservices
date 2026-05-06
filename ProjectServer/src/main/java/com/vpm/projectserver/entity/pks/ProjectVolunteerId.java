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
    private Long projectId;

    @Column(
            name = "volunteer_user_id",
            nullable = false
    )
    private Long volunteerUserId;

}
