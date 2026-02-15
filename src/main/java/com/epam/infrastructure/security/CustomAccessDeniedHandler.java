package com.epam.infrastructure.security;

import com.epam.infrastructure.utils.ProblemDetailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ProblemDetail problem = ProblemDetailUtil.createProblemDetail(HttpStatus.FORBIDDEN,
                "Forbidden", accessDeniedException.getMessage());

        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(problem));
    }


}