package com.pfp.companion.charactersheet.control;

import com.pfp.companion.charactersheet.control.CharacterSheetResponse.MoneyResponse;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.mediator.CharacterQueryService;
import com.pfp.companion.charactersheet.mediator.CharacterSheetMutationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/money")
public class MoneyController {

    private final CharacterQueryService queryService;
    private final CharacterSheetMutationService mutationService;
    private final CharacterResponseMapper responseMapper;
    private final AuthenticatedUserId authenticatedUserId;

    public MoneyController(CharacterQueryService queryService,
            CharacterSheetMutationService mutationService, CharacterResponseMapper responseMapper,
            AuthenticatedUserId authenticatedUserId) {
        this.queryService = queryService;
        this.mutationService = mutationService;
        this.responseMapper = responseMapper;
        this.authenticatedUserId = authenticatedUserId;
    }

    @GetMapping
    public MoneyResponse getMoney(@PathVariable UUID characterId, Authentication authentication) {
        return responseMapper.toMoney(queryService.findOwnedCharacter(characterId,
                authenticatedUserId.from(authentication)));
    }

    @PostMapping("/currency")
    public MoneyResponse selectDisplayCurrency(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.SelectCurrency request,
            Authentication authentication) {
        Character character = mutationService.selectDisplayCurrency(characterId,
                authenticatedUserId.from(authentication), request.displayCurrency());
        return responseMapper.toMoney(character);
    }

    @PutMapping
    public MoneyResponse setMoneyAmount(@PathVariable UUID characterId,
            @Valid @RequestBody CharacterAssetRequests.SetMoneyAmount request,
            Authentication authentication) {
        Character character = mutationService.setMoneyAmountBase(characterId,
                authenticatedUserId.from(authentication), request.amountBase());
        return responseMapper.toMoney(character);
    }
}
