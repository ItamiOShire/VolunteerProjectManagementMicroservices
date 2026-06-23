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
            name = "volunteer_user_id",
            nullable = false
    )
    private Long volunteerUserId;

    @Column(
            name = "project_id",
            nullable = false
    )
    private Long projectId;

}
