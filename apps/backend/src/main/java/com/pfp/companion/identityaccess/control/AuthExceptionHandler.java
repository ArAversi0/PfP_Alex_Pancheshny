package com.pfp.companion.identityaccess.control;

import com.pfp.companion.charactersheet.control.ApiErrorResponse;
import com.pfp.companion.identityaccess.mediator.AuthException;
import com.pfp.companion.identityaccess.mediator.DuplicateEmailException;
import com.pfp.companion.identityaccess.mediator.EmailDeliveryException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    ResponseEntity<ApiErrorResponse> handleAuth(AuthException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(EmailDeliveryException.class)
    ResponseEntity<ApiErrorResponse> handleDeliveryFailure(EmailDeliveryException exception,
            HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    private static ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI(), List.of()));
    }
}
