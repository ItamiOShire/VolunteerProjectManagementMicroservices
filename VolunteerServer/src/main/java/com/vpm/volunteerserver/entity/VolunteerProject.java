package com.vpm.volunteerserver.entity;


import com.vpm.volunteerserver.entity.pks.VolunteerProjectId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "volunteer_project")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerProject {

    @EmbeddedId
    private VolunteerProjectId volunteerProjectId;

}
