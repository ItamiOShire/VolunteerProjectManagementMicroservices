package com.vpm.volunteerserver.entity;


import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "volunteer_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
