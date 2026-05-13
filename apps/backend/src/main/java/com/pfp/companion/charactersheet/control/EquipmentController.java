package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.EquippedItemResponse;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterSheetMutationService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/equipment")
public class EquipmentController {

    private final CharacterQueryService queryService;
    private final CharacterSheetMutationService mutationService;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public EquipmentController(CharacterQueryService queryService,
            CharacterSheetMutationService mutationService, CharacterResponseMapper responseMapper,
            AuthenticatedUserId authenticatedUserId) {
        this.queryService = queryService;
        this.mutationService = mutationService;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping
    public Map<String, EquippedItemResponse> getEquipment(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toEquipment(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PostMapping("/equip")
    public CharacterSheetResponse equipItem(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.EquipItem request,
            Authentication authentication) {
        Character character = mutationService.equipItem(characterId,
                authenticatedUserId.from(authentication), request.itemId(), request.slotCode());
        return responseMapper.toSheet(character);
    }

    @PostMapping("/unequip")
    public CharacterSheetResponse unequipItem(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.UnequipItem request,
            Authentication authentication) {
        Character character = mutationService.unequipItem(characterId,
                authenticatedUserId.from(authentication), request.slotCode());
        return responseMapper.toSheet(character);
    }
}
