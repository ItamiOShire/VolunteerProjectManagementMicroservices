package com.vpm.authenticationserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String token;

    private Instant expiresAt;

    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    public RefreshToken(
            String token,
            Instant expiresAt,
            Users user
    ) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.user = user;
    }

}
