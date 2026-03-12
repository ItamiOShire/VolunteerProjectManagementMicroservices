package com.vpm.organizationserver.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "Organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "org_name",
            nullable = false,
            length = 50
    )
    private String orgName;

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
            nullable = true
    )
    private int apartmentNumber;

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

}
