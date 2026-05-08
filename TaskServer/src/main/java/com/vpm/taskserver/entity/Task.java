package com.vpm.taskserver.entity;

import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "description",
            nullable = false
    )
    private String description;

    @Column(
            name = "deadline",
            nullable = false
    )
    private LocalDate deadline;

    @Column(
            name = "title",
            nullable = false,
            length = 50
    )
    private String title;

    @Column(
            name = "project_id",
            nullable = false
    )
    private long projectId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.MERGE
    )
    @JoinColumn(
            name = "priority_id",
            nullable = false
    )
    private Priority priority;

    @OneToMany(
            mappedBy = "task",
            orphanRemoval = true,
            cascade = CascadeType.PERSIST
    )
    private List<VolunteerTask>  volunteerTasks;

    @OneToMany(
            mappedBy = "task",
            orphanRemoval = true,
            cascade = CascadeType.PERSIST
    )
    private List<TaskSuggestion> taskSuggestions;

    public void update(UpdateTaskRequest request, Priority priority) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.deadline = request.getDeadline();
        this.priority = priority;
    }

    public List<TaskSuggestion> getTaskSuggestions() {
        if (taskSuggestions == null) {
            taskSuggestions = new ArrayList<>();
        }
        return taskSuggestions;
    }

    public List<VolunteerTask> getVolunteerTasks() {
        if (volunteerTasks == null) {
            volunteerTasks = new ArrayList<>();
        }
        return volunteerTasks;
    }

}
