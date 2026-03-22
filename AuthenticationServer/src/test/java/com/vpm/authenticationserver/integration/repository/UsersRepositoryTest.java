package com.vpm.authenticationserver.integration.repository;

import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.entity.mapper.UsersMapper;
import com.vpm.authenticationserver.repository.UsersRepository;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.NoSuchElementException;
import java.util.Optional;

import static  org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Testcontainers
public class UsersRepositoryTest {

    /*
     * Database container managed by spring
     * image should be the same as production
     * database name, password and username does not matter until you use connection parameters in app.yaml
     */

    @Container
    static PostgreSQLContainer<?> database =
            new PostgreSQLContainer<>("postgres:17.9")
                    .withDatabaseName("Users")
                    .withPassword("password")
                    .withUsername("admin");

    /*
     * Injecting containers database metadata into spring app.yaml
     */

    @DynamicPropertySource
    static void configureProperties (DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.datasource.username", database::getUsername);
    }

    @Autowired
    private UsersRepository repository;

    /*
     * Testing data
     */

    Users volunteer = new Users(
            0,
            "integrationtestvolunteer@gmail.com",
            "hashedpassword",
            "VOLUNTEER",
            null
    );

    Users organization = new Users(
            0,
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

    @Test
    public void findUserByEmailTest() {

        Optional<Users> foundUser = repository.findByEmail("email@email.com");

        assertTrue(foundUser.isPresent());
        assertEquals("email@email.com",  foundUser.get().getEmail());

    }


}
