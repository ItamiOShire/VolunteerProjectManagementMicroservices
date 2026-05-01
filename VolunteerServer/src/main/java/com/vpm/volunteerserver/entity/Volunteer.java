package com.vpm.volunteerserver.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

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
    @Column (
            name = "user_id"
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
            nullable = false,
            length = 12
    )
    private String phoneNumber;

    @Column(
            name = "contact_email",
            nullable = false,
            length = 400
    )
    private String contactEmail;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private List<VolunteerTask> volunteerTasks;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private List<VolunteerProject> volunteerProjects;

    @OneToMany(
            mappedBy = "volunteer",
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private List<TaskSuggestion> volunteerTaskSuggestions;

}
