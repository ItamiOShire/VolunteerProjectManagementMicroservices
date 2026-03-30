package com.vpm.organizationserver.entity;


import com.vpm.organizationserver.dto.request.UpdateDescriptionRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization_description")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_user_id",
            referencedColumnName = "user_id"
    )
    private Organization organization;

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
