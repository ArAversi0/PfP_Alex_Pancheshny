package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterSheetMutationService;
import com.pfp.companion.charactersheet.mediator.ItemPlacement;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/characters/{characterId}/inventory")
public class InventoryController {

    private final CharacterQueryService queryService;
    private final CharacterSheetMutationService mutationService;
    private final CharacterRequestMapper requestMapper;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public InventoryController(CharacterQueryService queryService,
            CharacterSheetMutationService mutationService, CharacterRequestMapper requestMapper,
            CharacterResponseMapper responseMapper, AuthenticatedUserId authenticatedUserId) {
        this.queryService = queryService;
        this.mutationService = mutationService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping
    public CharacterSheetResponse.InventoryResponse getInventory(@PathVariable UUID characterId,
            Authentication authentication) {
        return responseMapper.toInventory(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PostMapping("/rows")
    public CharacterSheetResponse.InventoryResponse addRows(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.AddInventoryRows request,
            Authentication authentication) {
        Character character = mutationService.addInventoryRows(characterId,
                authenticatedUserId.from(authentication), request.rowsToAdd());
        return responseMapper.toInventory(character);
    }

    @DeleteMapping("/rows")
    public CharacterSheetResponse.InventoryResponse removeRow(@PathVariable UUID characterId,
            Authentication authentication) {
        Character character = mutationService.removeInventoryRow(characterId,
                authenticatedUserId.from(authentication));
        return responseMapper.toInventory(character);
    }

    @PostMapping("/slots/move")
    public CharacterSheetResponse.InventoryResponse moveItem(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.MoveInventoryItem request,
            Authentication authentication) {
        Character character = mutationService.moveInventoryItem(characterId,
                authenticatedUserId.from(authentication), request.fromSlotIndex(),
                request.toSlotIndex());
        return responseMapper.toInventory(character);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterSheetResponse createItem(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.ItemUpsert request,
            Authentication authentication) {
        mutationService.addItem(characterId,
                authenticatedUserId.from(authentication), requestMapper.toNewItem(request));
        return responseMapper.toSheet(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @GetMapping("/items/{itemId}")
    public CharacterSheetResponse.ItemResponse getItem(@PathVariable UUID characterId,
            @PathVariable UUID itemId, Authentication authentication) {
        return responseMapper.toItem(queryService.findOwnedItem(characterId,
                authenticatedUserId.from(authentication), itemId));
    }

    @PutMapping("/items/{itemId}")
    public CharacterSheetResponse updateItem(@PathVariable UUID characterId,
            @PathVariable UUID itemId,
            @Valid @RequestBody CharacterAssetRequests.ItemUpsert request,
            Authentication authentication) {
        Character character = mutationService.updateItem(characterId,
                authenticatedUserId.from(authentication), requestMapper.toItem(itemId, request));
        return responseMapper.toSheet(character);
    }

    @DeleteMapping("/items/{itemId}")
    public CharacterSheetResponse throwAwayItem(@PathVariable UUID characterId, @PathVariable UUID itemId,
            Authentication authentication) {
        Character character = mutationService.throwAwayItem(characterId,
                authenticatedUserId.from(authentication), itemId);
        return responseMapper.toSheet(character);
    }

    @PostMapping("/items/{itemId}/sell")
    public CharacterSheetResponse sellTradeItem(@PathVariable UUID characterId, @PathVariable UUID itemId,
            Authentication authentication) {
        Character character = mutationService.sellTradeItem(characterId,
                authenticatedUserId.from(authentication), itemId);
        return responseMapper.toSheet(character);
    }
}
