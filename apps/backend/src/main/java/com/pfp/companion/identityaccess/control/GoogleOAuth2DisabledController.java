package com.pfp.companion.identityaccess.control;

import com.pfp.companion.charactersheet.control.ApiErrorResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/oauth2/authorize")
@ConditionalOnMissingBean(ClientRegistrationRepository.class)
public class GoogleOAuth2DisabledController {

    @GetMapping("/google")
    public ResponseEntity<ApiErrorResponse> google() {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(),
                status.value(), status.getReasonPhrase(),
                "Google OAuth2 is not configured. Set PFP_GOOGLE_CLIENT_ID and PFP_GOOGLE_CLIENT_SECRET, then restart the backend.",
                "/api/v1/auth/oauth2/authorize/google", List.of()));
    }
}
