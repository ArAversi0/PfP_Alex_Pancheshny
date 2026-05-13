package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetRequests.Skill;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterSheetMutationService;
import com.pfp.companion.charactersheet.mediator.CreateCharacterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters")
public class CharacterController {

    private final CreateCharacterService createCharacterService;
    private final CharacterQueryService characterQueryService;
    private final CharacterSheetMutationService mutationService;
    private final CharacterRequestMapper requestMapper;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public CharacterController(CreateCharacterService createCharacterService,
            CharacterQueryService characterQueryService, CharacterSheetMutationService mutationService,
            CharacterRequestMapper requestMapper, CharacterResponseMapper responseMapper,
            AuthenticatedUserId authenticatedUserId) {
        this.createCharacterService = createCharacterService;
        this.characterQueryService = characterQueryService;
        this.mutationService = mutationService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping
    public List<CharacterCardResponse> list(Authentication authentication) {
        return characterQueryService.findCardsForUser(authenticatedUserId.from(authentication))
                .stream()
                .map(responseMapper::toCard)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterCreatedResponse create(@Valid @RequestBody CreateCharacterRequest request,
            Authentication authentication) {
        Character created = createCharacterService.createForUser(request.name(),
                authenticatedUserId.from(authentication));
        return responseMapper.toCreated(created);
    }

    @GetMapping("/{characterId}")
    public CharacterSummaryResponse getSummary(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toSummary(characterQueryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @DeleteMapping("/{characterId}")
    public DeletedResponse deleteCharacter(@PathVariable UUID characterId,
            Authentication authentication) {
        mutationService.deleteCharacter(characterId, authenticatedUserId.from(authentication));
        return DeletedResponse.success();
    }

    @GetMapping("/{characterId}/sheet")
    public CharacterSheetResponse getSheet(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toSheet(characterQueryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PutMapping("/{characterId}/info")
    public CharacterSummaryResponse updateInfo(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterSheetRequests.Info request,
            Authentication authentication) {
        return responseMapper.toSummary(mutationService.updateInfo(characterId,
                authenticatedUserId.from(authentication), request.name(), requestMapper.toInfo(request)));
    }

    @PutMapping("/{characterId}/portrait")
    public PortraitResponse updatePortrait(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterSheetRequests.Portrait request,
            Authentication authentication) {
        Character character = mutationService.updateImage(characterId,
                authenticatedUserId.from(authentication), request.imageUrl());
        return new PortraitResponse(character.id(), character.image());
    }

    @PutMapping("/{characterId}/additional-info")
    public CharacterSheetResponse.AdditionalInfoResponse updateAdditionalInfo(
            @PathVariable UUID characterId,
            @Valid @RequestBody CharacterSheetRequests.AdditionalInfo request,
            Authentication authentication) {
        return responseMapper.toAdditionalInfo(mutationService.updateAdditionalInfo(characterId,
                authenticatedUserId.from(authentication), requestMapper.toAdditionalInfo(request)));
    }

    @PutMapping("/{characterId}/blessings")
    public CharacterSheetResponse.BlessingsResponse updateBlessings(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterSheetRequests.Blessings request,
            Authentication authentication) {
        return responseMapper.toBlessings(mutationService.updateBlessings(characterId,
                authenticatedUserId.from(authentication), requestMapper.toBlessings(request)));
    }

    @PutMapping("/{characterId}/stats")
    public Map<String, CharacterSheetResponse.StatResponse> updateStats(
            @PathVariable UUID characterId, @Valid @RequestBody CharacterSheetRequests.Stats request,
            Authentication authentication) {
        return responseMapper.toStats(mutationService.updateStats(characterId,
                authenticatedUserId.from(authentication), requestMapper.toStats(request)));
    }

    @GetMapping("/{characterId}/stats")
    public Map<String, CharacterSheetResponse.StatResponse> getStats(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toStats(characterQueryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PutMapping("/{characterId}/skills")
    public List<CharacterSheetResponse.SkillResponse> updateSkills(@PathVariable UUID characterId,
            @Valid @RequestBody List<@Valid Skill> request,
            Authentication authentication) {
        return responseMapper.toSkills(mutationService.updateSkills(characterId,
                authenticatedUserId.from(authentication), requestMapper.toSkills(request)));
    }

    @GetMapping("/{characterId}/skills")
    public List<CharacterSheetResponse.SkillResponse> getSkills(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toSkills(characterQueryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PutMapping("/{characterId}/condition")
    public CharacterSheetResponse.ConditionResponse updateCondition(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterSheetRequests.Condition request,
            Authentication authentication) {
        return responseMapper.toCondition(mutationService.updateCondition(characterId,
                authenticatedUserId.from(authentication), requestMapper.toCondition(request)));
    }

    @GetMapping("/{characterId}/condition")
    public CharacterSheetResponse.ConditionResponse getCondition(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toCondition(characterQueryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    public record CreateCharacterRequest(
            @NotBlank @Size(max = 120) String name) {
    }
}
