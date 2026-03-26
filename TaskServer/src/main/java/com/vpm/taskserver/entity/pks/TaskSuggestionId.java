package com.vpm.taskserver.entity.pks;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSuggestionId {

    @Column(
            name = "volunteer_user_id",
            nullable = false
    )
    private long volunteerUserId;
    @Column (
            name = "task_id",
            nullable = false
    )
    private long taskId;

}
