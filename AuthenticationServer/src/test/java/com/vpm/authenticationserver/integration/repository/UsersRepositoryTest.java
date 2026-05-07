package com.vpm.authenticationserver.integration.repository;

import com.vpm.authenticationserver.config.IntegrationTestsDBConfig;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static  org.junit.jupiter.api.Assertions.*;


@SpringBootTest
/*
 * By this import, you enable Testcontainers -> see this configuration class
 */
@Import(IntegrationTestsDBConfig.class)
public class UsersRepositoryTest {

    @Autowired
    private UsersRepository repository;

    /*
     * Testing data
     */

    Users volunteer = new Users(
            null,
            "integrationtestvolunteer@gmail.com",
            "hashedpassword",
            "VOLUNTEER",
            null
    );

    Users organization = new Users(
            null,
            "integrationtestorganization@gmail.com",
            "hashedpassword",
            "ORGANIZATION",
            null
    );

    @Nested
    @DisplayName("Saving users")
    class Save {

        @Test
        @DisplayName("Should save user in database")
        public void  shouldSaveUser() {
            Users saved = repository.save(volunteer);

            assertNotNull(saved);

        }

        @Test
        @DisplayName("Saved user should have different id")
        public void  shouldSaveUserWithDifferentId() {

            Users saved = repository.save(organization);

            assertNotNull(saved);
            assertNotEquals(0, saved.getId());

        }

        @Test
        @DisplayName("Saved user should have the same business data as before saving")
        public void  shouldSaveUserWithSameBusinessData() {

            organization.setEmail("integrationtestorganization2@gmail.com");
            Users saved = repository.save(organization);

            assertNotNull(saved);
            assertEquals(organization.getEmail(),  saved.getEmail());
            assertEquals(organization.getPassword(),  saved.getPassword());
            assertEquals(organization.getRole(),  saved.getRole());

        }

        @Test
        @DisplayName("Should throw error on null user")
        public void  shouldThrowOnNullUser() {

            assertThrows(InvalidDataAccessApiUsageException.class, () -> repository.save(null));

        }

    }

    @Nested
    @DisplayName("Finding users")
    class Find {

        @Test
        @DisplayName("Should find existing user")
        public void  shouldFindExistingUser() {

            Optional<Users> user = repository.findByEmail("testvolunteer@gmail.com");

            assertFalse(user.isEmpty());

        }

        @Test
        @DisplayName("Should return empty optional on non existing user")
        public void  shouldReturnEmptyOptionalOnNonExistingUser() {

            Optional<Users> user = repository.findByEmail("nonexistingemail@gmial.com");

            assertTrue(user.isEmpty());
            assertThrows(NoSuchElementException.class , user::get);

        }

    }

}
