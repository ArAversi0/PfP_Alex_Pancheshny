package com.pfp.companion.identityaccess.control;

import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.mediator.AuthApplicationService;
import com.pfp.companion.identityaccess.mediator.AuthSession;
import com.pfp.companion.identityaccess.mediator.RefreshedTokens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return toUserResponse(authService.register(request.email(), request.password(),
                request.confirmPassword()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toAuthResponse(authService.login(request.email(), request.password()));
    }

    @GetMapping("/verify-email")
    public GenericMessageResponse verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return new GenericMessageResponse("Email verified.");
    }

    @PostMapping("/verify-email/resend")
    public GenericMessageResponse resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        return new GenericMessageResponse(authService.resendVerification(request.email()));
    }

    @PostMapping("/refresh")
    public RefreshedTokens refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public GenericMessageResponse logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return new GenericMessageResponse("Logged out.");
    }

    @PostMapping("/oauth2/exchange")
    public AuthResponse exchangeOAuth2Code(@Valid @RequestBody OAuth2ExchangeRequest request) {
        return toAuthResponse(authService.exchangeOAuth2Code(request.code()));
    }

    @PostMapping("/password/forgot")
    public GenericMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return new GenericMessageResponse(authService.forgotPassword(request.email()));
    }

    @PostMapping("/password/reset")
    public GenericMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
        return new GenericMessageResponse("Password updated.");
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return toUserResponse(authService.requireUser(UUID.fromString(authentication.getName())));
    }

    private static AuthResponse toAuthResponse(AuthSession session) {
        return new AuthResponse(session.accessToken(), session.refreshToken(), session.tokenType(),
                toUserResponse(session.user()));
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(user.id(), user.email(), user.role().name(), user.emailVerified());
    }

    public record RegisterRequest(@NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Size(min = 8) String confirmPassword) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {
    }

    public record OAuth2ExchangeRequest(@NotBlank String code) {
    }

    public record ResendVerificationRequest(@NotBlank @Email String email) {
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    public record ResetPasswordRequest(@NotBlank String token,
            @NotBlank @Size(min = 8) String newPassword,
            @NotBlank @Size(min = 8) String confirmPassword) {
    }

    public record AuthResponse(String accessToken, String refreshToken, String tokenType,
            UserResponse user) {
    }

    public record UserResponse(UUID id, String email, String role, boolean emailVerified) {
    }

    public record GenericMessageResponse(String message) {
    }
}
