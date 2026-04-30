package com.vpm.taskserver.entity;

import com.vpm.taskserver.entity.pks.TaskSuggestionId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_suggestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSuggestion {

    @EmbeddedId
    private TaskSuggestionId id;

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
