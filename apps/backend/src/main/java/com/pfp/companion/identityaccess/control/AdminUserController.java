package com.pfp.companion.identityaccess.control;

import com.pfp.companion.identityaccess.mediator.AdminUserService;
import com.pfp.companion.identityaccess.mediator.AdminUserService.AdminDashboardSummary;
import com.pfp.companion.identityaccess.mediator.AdminUserSummary;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public AdminDashboardSummary dashboard() {
        return service.dashboard();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return service.listUsers().stream().map(AdminUserResponse::from).toList();
    }

    @DeleteMapping("/users/{userId}")
    public DeletedResponse deleteUser(Authentication authentication, @PathVariable UUID userId) {
        service.deleteUser(UUID.fromString(authentication.getName()), userId);
        return new DeletedResponse(true);
    }

    public record AdminUserResponse(UUID id, String email, String role, boolean emailVerified,
            Instant createdAt, long characterCount) {

        static AdminUserResponse from(AdminUserSummary user) {
            return new AdminUserResponse(user.id(), user.email(), user.role().name(),
                    user.emailVerified(), user.createdAt(), user.characterCount());
        }
    }

    public record DeletedResponse(boolean deleted) {
    }
}
