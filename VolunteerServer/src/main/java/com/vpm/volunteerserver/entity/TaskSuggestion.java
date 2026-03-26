package com.vpm.volunteerserver.entity;

import com.vpm.volunteerserver.entity.pks.TaskSuggestionId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_suggestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSuggestion {

    @EmbeddedId
    private TaskSuggestionId taskSuggestionId;

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
