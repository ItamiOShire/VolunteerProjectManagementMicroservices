package com.vpm.projectserver.security;


import com.vpm.projectserver.exception.project.MissingRequiredHeaderException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestFilter extends OncePerRequestFilter {

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws MissingRequiredHeaderException {

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userId == null) {
            throw new MissingRequiredHeaderException("X-User-Id");
        }

        if (userRole == null) {
            throw new MissingRequiredHeaderException("X-User-Role");
        }

    }

}
