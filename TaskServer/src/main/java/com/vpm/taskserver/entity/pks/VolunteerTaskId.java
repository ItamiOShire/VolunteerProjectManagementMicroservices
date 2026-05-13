package com.vpm.taskserver.entity.pks;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class VolunteerTaskId {

    @Column(
            name = "volunteer_user_id",
            nullable = false
    )
    private Long volunteerUserId;

    @Column(
            name = "task_id",
            nullable = false
    )
    private Long taskId;

}