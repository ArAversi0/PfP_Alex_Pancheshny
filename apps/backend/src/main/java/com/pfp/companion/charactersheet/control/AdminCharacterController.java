package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.mediator.AdminCharacterGroup;
import com.pfp.companion.charactersheet.mediator.AdminCharacterService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/characters")
public class AdminCharacterController {

    private final AdminCharacterService service;
    private final CharacterResponseMapper responseMapper;

    public AdminCharacterController(AdminCharacterService service,
            CharacterResponseMapper responseMapper) {
        this.service = service;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public List<AdminCharacterGroupResponse> list() {
        return service.findAllGroupedByOwner().stream()
                .map(group -> new AdminCharacterGroupResponse(group.userId(), group.email(),
                        group.characters().stream().map(responseMapper::toCard).toList()))
                .toList();
    }

    @GetMapping("/{characterId}/sheet")
    public CharacterSheetResponse sheet(@PathVariable UUID characterId) {
        return responseMapper.toSheet(service.findCharacter(characterId));
    }

    public record AdminCharacterGroupResponse(UUID userId, String email,
            List<CharacterCardResponse> characters) {
    }
}
