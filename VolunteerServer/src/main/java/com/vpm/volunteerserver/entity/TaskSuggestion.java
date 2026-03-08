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

}
