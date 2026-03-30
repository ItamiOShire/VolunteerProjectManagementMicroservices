package com.vpm.authenticationserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class RequestFilter extends OncePerRequestFilter {

    /*
     * services allowed for internal API communication with Authentication Server. This is a basic validation mechanism to ensure that only authorized services can access the registration endpoint.
     */
    private final Set<String> allowedServices = new HashSet<>(
            Set.of(
                    "volunteer-service",
                    "organization-service"
            )
    );

    //TODO: change hardcoded values of headers in filter

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/internal")) {
            String internalRequest = request.getHeader("X-INTERNAL-REQUEST");
            String serviceName = request.getHeader("X-SERVICE-NAME");

            if (internalRequest == null || !internalRequest.equals("true")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing X-INTERNAL-REQUEST header");

                return;
            }

            if (serviceName == null || !allowedServices.contains(serviceName)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing X-SERVICE-NAME header");
                return;
            }
        }

        filterChain.doFilter(request, response);

    }

}
