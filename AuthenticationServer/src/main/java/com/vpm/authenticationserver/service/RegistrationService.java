package com.vpm.authenticationserver.service;

import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.entity.mapper.UsersMapper;
import com.vpm.authenticationserver.exception.user.UserAlreadyExistsException;
import com.vpm.authenticationserver.repository.UsersRepository;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Slf4j
public class RegistrationService {

    private final UsersRepository usersRepository;

    private final Map<Class<? extends Throwable>, Supplier<String>> errors =
            new HashMap<>(
                    Map.of(
                            UserAlreadyExistsException.class, () -> "Error during registration: "
                    )
            );

    @Autowired
    public RegistrationService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    private void validateException(Exception e) {
        Supplier<String> errorMessage = errors
                .getOrDefault(
                        e.getClass(),
                        () -> "Unexpected error during registration: "
                );
        log.error("{} {}", errorMessage.get(), e.getMessage());
    }

    public AuthRegistrationResponse registerUserInAuthService(
            AuthRegistrationRequest request
    ) throws UserAlreadyExistsException {

        Users user = UsersMapper.mapByRegistrationRequest(request);

        try {

            log.info("Attempting to register user with email: {}", request.getEmail());

            Optional<Users> foundUser = usersRepository.findByEmail(user.getEmail());

            if (foundUser.isPresent()) {
                log.warn("User with email {} already exists", request.getEmail());
                throw new UserAlreadyExistsException(request.getEmail());
            }

            Users savedUser = usersRepository.save(user);

            log.info("Successfully registered user with email: {}", request.getEmail());

            return new AuthRegistrationResponse(savedUser.getId());

        } catch (Exception e) {

            validateException(e);

            throw e;
        }

    }
}
