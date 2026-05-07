package com.vpm.authenticationserver.unit.service;

import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.repository.UsersRepository;
import com.vpm.authenticationserver.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static  org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    Users mockedUser;

    @BeforeEach
    public void setup(){
        mockedUser = new Users(
                1L,
                "testemail@gmail.com",
                "hashedpassword",
                "VOLUNTEER",
                null
        );
    }

    /*
     * Positive testing
     */

    @Test
    public void successfulLoadingUserByUsername() {
        String email = "testemail@gmail.com";
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));

        Users user = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(user);
        assertEquals(email, user.getEmail());

    }

    @Test
    public void shouldNotThrowOnSuccessfulLoadingUserByUsername() {
        String email = "testemail@gmail.com";
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));

        assertDoesNotThrow(
                () -> customUserDetailsService.loadUserByUsername(email),
                "should not throw error on finding user"
        );

    }

    /*
     * Negative testing
     */

    @Test
    public void shouldThrowErrorOnNullEmail() {
        when(usersRepository.findByEmail(null)).thenThrow(NullPointerException.class);

        assertThrows(
                NullPointerException.class,
                () -> customUserDetailsService.loadUserByUsername(null)
        );

        verify(usersRepository, times(1)).findByEmail(null);
    }

    @Test
    public void shouldThrowErrorOnNotFindingUser() {

        when(usersRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("testemail2@gmail.com")
        );

        verify(usersRepository, times(1)).findByEmail(any(String.class));

    }

}
