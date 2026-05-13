package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.SpellResponse;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterSheetMutationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/spells")
public class SpellController {

    private final CharacterQueryService queryService;
    private final CharacterSheetMutationService mutationService;
    private final CharacterRequestMapper requestMapper;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public SpellController(CharacterQueryService queryService,
            CharacterSheetMutationService mutationService, CharacterRequestMapper requestMapper,
            CharacterResponseMapper responseMapper, AuthenticatedUserId authenticatedUserId) {
        this.queryService = queryService;
        this.mutationService = mutationService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping
    public List<SpellResponse> listSpells(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toSpells(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterSheetResponse createSpell(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.SpellUpsert request,
            Authentication authentication) {
        mutationService.addSpell(characterId, authenticatedUserId.from(authentication),
                requestMapper.toNewSpell(request));
        return responseMapper.toSheet(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @GetMapping("/{spellId}")
    public SpellResponse getSpell(@PathVariable UUID characterId, @PathVariable UUID spellId,
            Authentication authentication) {
        return responseMapper.toSpell(queryService.findOwnedSpell(characterId,
                authenticatedUserId.from(authentication), spellId));
    }

    @PutMapping("/{spellId}")
    public CharacterSheetResponse updateSpell(@PathVariable UUID characterId, @PathVariable UUID spellId,
            @Valid @RequestBody CharacterAssetRequests.SpellUpsert request,
            Authentication authentication) {
        mutationService.updateSpell(characterId, authenticatedUserId.from(authentication),
                requestMapper.toSpell(spellId, request));
        return responseMapper.toSheet(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @DeleteMapping("/{spellId}")
    public CharacterSheetResponse deleteSpell(@PathVariable UUID characterId, @PathVariable UUID spellId,
            Authentication authentication) {
        Character character = mutationService.deleteSpell(characterId,
                authenticatedUserId.from(authentication), spellId);
        return responseMapper.toSheet(character);
    }
}
