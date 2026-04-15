package com.vpm.taskserver.entity;

import com.vpm.taskserver.entity.pks.VolunteerTaskId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table( name = "volunteer_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerTask {

    @EmbeddedId
    private VolunteerTaskId id;

    @MapsId("taskId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "task_id",
            nullable = false
    )
    private Task task;

    @Column(
            name = "volunteer_user_id",
            insertable = false,
            updatable = false
    )
    private long volunteerId;

}
