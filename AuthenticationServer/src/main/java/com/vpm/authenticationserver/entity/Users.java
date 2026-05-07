package com.vpm.authenticationserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String role;

    /*
     * To remove refresh token, detach it from the user and let orphanRemoval handle the deletion.
     */
    @OneToMany(
            mappedBy = "user",
            orphanRemoval = true
    )
    @ToString.Exclude
    List<RefreshToken> refreshTokens;

    @Override
    @NullMarked
    public Set<GrantedAuthority> getAuthorities() {
        return Set.of(() -> role);
    }

    @Override
    @NullMarked
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public List<RefreshToken> getRefreshTokens() {
        if (refreshTokens == null) {
            refreshTokens = new ArrayList<>();
        }
        return refreshTokens;
    }

}
