package org.backendbrilliance.uiservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(Exception.class)
    public void handleAll(
            Exception e,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, IOException {

        // Skip SSE streams entirely — never intercept them
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return;
        }

        int status = 500;
        String error = "INTERNAL_ERROR";
        String message = e.getMessage();

        // Map known exception types to status codes
        if (e instanceof TierLimitException) {
            status = 403;
            error = "TIER_LIMIT";
        } else if (e instanceof jakarta.persistence.EntityNotFoundException) {
            status = 404;
            error = "NOT_FOUND";
        } else if (e instanceof org.springframework.security.access.AccessDeniedException) {
            status = 403;
            error = "FORBIDDEN";
        }

        // Force JSON regardless of what Spring cached from SSE
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of("error", error, "message", message != null ? message : "Unknown error"))
        );
    }

}