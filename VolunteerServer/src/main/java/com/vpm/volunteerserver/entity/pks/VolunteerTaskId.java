package com.vpm.volunteerserver.entity.pks;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VolunteerTaskId {

    @Column(
            name = "volunteer_id",
            nullable = false
    )
    private long volunteerId;

    @Column(
            name = "task_id",
            nullable = false
    )
    private long taskId;

}
