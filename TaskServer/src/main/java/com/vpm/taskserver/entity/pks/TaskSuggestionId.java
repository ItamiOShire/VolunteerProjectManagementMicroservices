package com.vpm.taskserver.entity.pks;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskSuggestionId {

    @Column(
            name = "volunteer_user_id",
            nullable = false
    )
    private Long volunteerUserId;
    @Column (
            name = "task_id",
            nullable = false
    )
    private Long taskId;

}
