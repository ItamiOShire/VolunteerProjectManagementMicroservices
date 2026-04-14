package com.vpm.organizationserver.security;

import com.vpm.organizationserver.exception.request.MissingRequiredHeaderException;
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
     * Set of required headers - those headers are signature of authorized request
     * Gateway adds those headers if user is authorized
     */
    private final Set<String> requiredHeaders = new HashSet<>(
            Set.of(
                    "X-User-Id",
                    "X-User-Role"
            )
    );

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        try {
            for (String header : requiredHeaders) {

                String headerValue = request.getHeader(header);

                if (headerValue == null || headerValue.isEmpty()) {

                    throw new MissingRequiredHeaderException(header);

                }
            }

            filterChain.doFilter(request, response);

        } catch (MissingRequiredHeaderException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required header: " + e.getMessage());
        }

    }

}
