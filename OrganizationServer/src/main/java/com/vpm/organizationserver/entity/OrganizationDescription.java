package com.vpm.organizationserver.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_description")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDescription {

    @Id
    @Column(
            name = "organization_id"
    )
    private long organizationId;

    @Lob
    @Column (
            name = "description",
            nullable = true
    )
    private String description;

    @Column(
            name = "image_path",
            nullable = true
    )
    private String imagePath;

}
