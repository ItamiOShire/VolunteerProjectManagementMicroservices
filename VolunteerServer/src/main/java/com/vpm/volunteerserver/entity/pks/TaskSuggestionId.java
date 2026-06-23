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
public class TaskSuggestionId {

    @Column (
            name = "volunteer_user_id",
            nullable = false
    )
    private Long volunteerUserId;

    @Column (
            name = "task_id",
            nullable = false
    )
    private Long taskId;

    @Column(
            name = "project_id",
            nullable = false
    )
    private Long projectId;

}
