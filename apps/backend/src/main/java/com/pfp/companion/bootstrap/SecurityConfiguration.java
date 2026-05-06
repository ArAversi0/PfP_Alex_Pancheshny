package com.pfp.companion.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfp.companion.charactersheet.control.ApiErrorResponse;
import com.pfp.companion.identityaccess.control.GoogleOAuth2LoginFailureHandler;
import com.pfp.companion.identityaccess.control.GoogleOAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;
    private final GoogleOAuth2LoginSuccessHandler oauth2SuccessHandler;
    private final GoogleOAuth2LoginFailureHandler oauth2FailureHandler;
    private final ObjectMapper objectMapper;

    public SecurityConfiguration(
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository,
            GoogleOAuth2LoginSuccessHandler oauth2SuccessHandler,
            GoogleOAuth2LoginFailureHandler oauth2FailureHandler,
            ObjectMapper objectMapper) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.oauth2FailureHandler = oauth2FailureHandler;
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login",
                                "/api/v1/auth/verify-email", "/api/v1/auth/verify-email/resend",
                                "/api/v1/auth/refresh", "/api/v1/auth/password/forgot",
                                "/api/v1/auth/password/reset", "/api/v1/auth/oauth2/**",
                                "/login/oauth2/code/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lore/**", "/api/v1/rules/**")
                                .permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(request, response, HttpStatus.UNAUTHORIZED,
                                        "authentication is required"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(request, response, HttpStatus.FORBIDDEN,
                                        "access is denied")))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(request, response, HttpStatus.UNAUTHORIZED,
                                        "authentication is required"))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        ClientRegistrationRepository clients = clientRegistrationRepository.getIfAvailable();
        if (clients != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .clientRegistrationRepository(clients)
                    .authorizationEndpoint(endpoint ->
                            endpoint.baseUri("/api/v1/auth/oauth2/authorize"))
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler(oauth2FailureHandler));
        }
        return http.build();
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
            HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(Instant.now(),
                status.value(), status.getReasonPhrase(), message, request.getRequestURI(),
                List.of()));
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            return role == null ? java.util.List.of() : java.util.List.of(new SimpleGrantedAuthority(role));
        });
        return converter;
    }
}
