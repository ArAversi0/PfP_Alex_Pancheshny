package com.pfp.companion.identityaccess.control;

import static org.assertj.core.api.Assertions.assertThat;

import com.pfp.companion.charactersheet.control.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GoogleOAuth2DisabledControllerTest {

    @Test
    void returnsActionableConfigurationError() {
        ResponseEntity<ApiErrorResponse> response = new GoogleOAuth2DisabledController().google();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .contains("PFP_GOOGLE_CLIENT_ID")
                .contains("PFP_GOOGLE_CLIENT_SECRET")
                .contains("restart the backend");
    }
}
