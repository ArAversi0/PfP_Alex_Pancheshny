package com.pfp.companion.charactersheet.foundation.persistence;

import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.mediator.AdminCharacterGroup;
import com.pfp.companion.charactersheet.mediator.AdminCharacterRepository;
import com.pfp.companion.charactersheet.mediator.CharacterCard;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaAdminCharacterRepositoryAdapter implements AdminCharacterRepository {

    private final CharacterJpaRepository repository;
    private final CharacterPersistenceMapper mapper;

    JpaAdminCharacterRepositoryAdapter(CharacterJpaRepository repository,
            CharacterPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCharacterGroup> findAllGroupedByOwner() {
        Map<UUID, MutableGroup> groups = new LinkedHashMap<>();
        repository.findAdminCharacterCards().forEach(character -> {
            MutableGroup group = groups.computeIfAbsent(character.getUserId(),
                    ignored -> new MutableGroup(character.getUserId(), character.getEmail()));
            group.characters.add(new CharacterCard(character.getCharacterId(), character.getName(),
                    character.getImageUrl(), character.getLevel(), character.getClassName(),
                    character.getSpecialization(), character.getCreatedAt()));
        });
        return groups.values().stream()
                .map(group -> new AdminCharacterGroup(group.userId, group.email, List.copyOf(group.characters)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Character> findById(UUID characterId) {
        return repository.findByPublicId(characterId).map(mapper::toDomain);
    }

    private static final class MutableGroup {
        private final UUID userId;
        private final String email;
        private final List<CharacterCard> characters = new ArrayList<>();

        private MutableGroup(UUID userId, String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
