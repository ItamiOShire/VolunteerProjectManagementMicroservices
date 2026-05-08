package com.vpm.organizationserver.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name= "Organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    // TODO: it is better to use userId as primary key, since it is unique and there is one-to-one relation between organization and user (hibernate does not accept as foreign key anything else but primary key)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private long userId;

    @Column(
            name = "organization_name",
            nullable = false,
            length = 50
    )
    private String organizationName;

    @Column(
            name = "krs_number",
            nullable = false,
            length = 10
    )
    private String krsNumber;

    @Column(
            name = "street",
            nullable = false,
            length = 40
    )
    private String street;

    @Column(
            name = "apartment_number",
            nullable = true,
            length = 10
    )
    private String apartmentNumber;

    @Column(
            name = "town",
            nullable = false,
            length = 40
    )
    private String town;

    @Column(
            name = "zip_code",
            nullable = false,
            length = 6
    )
    private String zipCode;

    @Column(
            name = "owner_first_name",
            nullable = false,
            length = 25
    )
    private String ownerFirstName;

    @Column(
            name = "owner_last_name",
            nullable = false,
            length = 50
    )
    private String ownerLastName;

    @Column(
            name = "phone_number",
            nullable = false,
            length = 30
    )
    private String phoneNumber;

    @Column(
            name = "contact_email",
            nullable = false,
            length = 400
    )
    private String contactEmail;

    @OneToOne(
            mappedBy = "organization",
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private OrganizationDescription organizationDescription;

}
