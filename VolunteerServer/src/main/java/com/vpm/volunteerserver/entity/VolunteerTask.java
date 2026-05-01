package com.vpm.volunteerserver.entity;


import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "volunteer_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerTask {

    @EmbeddedId
    private VolunteerTaskId volunteerTaskId;

    @MapsId("volunteerUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "volunteer_user_id",
            referencedColumnName = "user_id"
    )
    private Volunteer volunteer;

    @Column(
            name = "task_id",
            insertable = false,
            updatable = false
    )
    private long taskId;

}
