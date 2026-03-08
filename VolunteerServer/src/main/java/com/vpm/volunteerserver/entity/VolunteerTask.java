package com.vpm.volunteerserver.entity;


import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "volunteer_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VolunteerTaskId.class)
public class VolunteerTask {

    @EmbeddedId
    private VolunteerTaskId volunteerTaskId;

}
