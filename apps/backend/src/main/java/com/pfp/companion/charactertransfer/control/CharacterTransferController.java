package com.pfp.companion.charactertransfer.control;

import com.pfp.companion.charactersheet.control.AuthenticatedUserId;
import com.pfp.companion.charactersheet.control.CharacterCreatedResponse;
import com.pfp.companion.charactersheet.control.CharacterResponseMapper;
import com.pfp.companion.charactertransfer.mediator.CharacterTransferApplicationService;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/characters")
public class CharacterTransferController {

    private final CharacterTransferApplicationService transferService;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public CharacterTransferController(CharacterTransferApplicationService transferService,
            CharacterResponseMapper responseMapper, AuthenticatedUserId authenticatedUserId) {
        this.transferService = transferService;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping(value = "/{characterId}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportCharacter(@PathVariable UUID characterId,
            Authentication authentication) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(transferService.exportOwnedCharacter(characterId,
                        authenticatedUserId.from(authentication)));
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterCreatedResponse importCharacter(@RequestBody String json,
            Authentication authentication) {
        return responseMapper.toCreated(transferService.importAsNewCharacter(json,
                authenticatedUserId.from(authentication)));
    }
}
