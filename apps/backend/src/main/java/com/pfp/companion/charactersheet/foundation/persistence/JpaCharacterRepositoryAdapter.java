package com.pfp.companion.charactersheet.foundation.persistence;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.mediator.CharacterRepository;
import com.pfp.companion.charactersheet.mediator.CharacterCard;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCharacterRepositoryAdapter implements CharacterRepository {

    private final CharacterJpaRepository characterRepository;
    private final CharacterOwnerJpaRepository userRepository;
    private final CurrencyJpaRepository currencyRepository;
    private final CharacterPersistenceMapper mapper;

    public JpaCharacterRepositoryAdapter(CharacterJpaRepository characterRepository,
            CharacterOwnerJpaRepository userRepository, CurrencyJpaRepository currencyRepository,
            CharacterPersistenceMapper mapper) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(UUID userId) {
        return characterRepository.countByUserPublicId(userId);
    }

    @Override
    @Transactional
    public Character save(Character character) {
        if (character.ownership().type() != Ownership.Type.AUTHENTICATED) {
            throw new IllegalArgumentException("local guest character must not be stored in PostgreSQL");
        }
        CharacterOwnerJpaEntity user = userRepository.findByPublicId(character.ownership().userId())
                .orElseThrow(() -> new IllegalArgumentException("unknown character owner"));
        CurrencyJpaEntity currency = currencyRepository.findByCode(character.money().displayCurrency().name())
                .orElseThrow(() -> new IllegalStateException("currency seed is missing"));
        if (characterRepository.deleteDirectByPublicId(character.id()) > 0) {
            characterRepository.flush();
        }
        return mapper.toDomain(characterRepository.saveAndFlush(mapper.toJpa(character, user, currency)));
    }

    @Transactional(readOnly = true)
    public Optional<Character> findById(UUID publicId) {
        Objects.requireNonNull(publicId);
        return characterRepository.findByPublicId(publicId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CharacterCard> findCardsByUserId(UUID userId) {
        return characterRepository.findAllByUserPublicIdOrderByCreatedAtDesc(userId).stream()
                .map(character -> new CharacterCard(character.publicId, character.name, character.image,
                        character.info.level, character.info.className,
                        character.info.specialization, character.createdAt))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (characterRepository.deleteDirectByPublicId(id) > 0) {
            characterRepository.flush();
        }
    }
}
