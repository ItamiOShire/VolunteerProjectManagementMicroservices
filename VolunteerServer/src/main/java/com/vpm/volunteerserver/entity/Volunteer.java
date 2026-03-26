package com.vpm.volunteerserver.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "volunteer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (
            name = "user_id",
            nullable = false,
            unique = true
    )
    private long userId;

    @Column(
            name = "first_name",
            nullable = false,
            length = 25
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 50
    )
    private String lastName;

    @Column(
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Column(
            name = "phone_number",
            nullable = false
    )
    private String phoneNumber;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true
    )
    private List<VolunteerTask> volunteerTasks;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true
    )
    private List<VolunteerProject> volunteerProjects;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true
    )
    private List<TaskSuggestion> volunteerTaskSuggestions;

}
