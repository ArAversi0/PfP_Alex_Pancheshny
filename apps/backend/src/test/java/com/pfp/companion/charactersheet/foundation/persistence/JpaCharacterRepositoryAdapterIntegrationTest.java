package com.pfp.companion.charactersheet.foundation.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.pfp.companion.charactersheet.entity.AdditionalInfo;
import com.pfp.companion.charactersheet.entity.BlessingInspiration;
import com.pfp.companion.charactersheet.entity.Character;
import com.pfp.companion.charactersheet.entity.CharacterCondition;
import com.pfp.companion.charactersheet.entity.CharacterInfo;
import com.pfp.companion.charactersheet.entity.CharacterStats;
import com.pfp.companion.charactersheet.entity.Currency;
import com.pfp.companion.charactersheet.entity.EquipmentSlotCode;
import com.pfp.companion.charactersheet.entity.EquipmentType;
import com.pfp.companion.charactersheet.entity.Item;
import com.pfp.companion.charactersheet.entity.ItemType;
import com.pfp.companion.charactersheet.entity.Ownership;
import com.pfp.companion.charactersheet.entity.SkillName;
import com.pfp.companion.charactersheet.entity.Spell;
import com.pfp.companion.charactersheet.entity.SpellClass;
import com.pfp.companion.charactersheet.entity.SpellType;
import com.pfp.companion.charactersheet.entity.StatGroup;
import com.pfp.companion.bootstrap.PfpCompanionApplication;
import com.pfp.companion.content.entity.LoreArticle;
import com.pfp.companion.content.entity.RuleBookArticle;
import com.pfp.companion.content.entity.RuleCategory;
import com.pfp.companion.content.foundation.persistence.JpaContentRepositoryAdapter;
import com.pfp.companion.identityaccess.entity.OAuthIdentity;
import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.foundation.persistence.JpaUserRepositoryAdapter;
import com.pfp.companion.identityaccess.mediator.AuthTokenRepository;
import com.pfp.gamerules.BodyPart;
import com.pfp.gamerules.BodyPartHealth;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = PfpCompanionApplication.class)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class JpaCharacterRepositoryAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JpaCharacterRepositoryAdapter repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaUserRepositoryAdapter userRepository;

    @Autowired
    private JpaContentRepositoryAdapter contentRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID userPublicId;

    @BeforeEach
    void createUser() {
        userPublicId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO users (public_id, email, role)
                VALUES (?, ?, 'ROLE_USER')
                """, userPublicId, userPublicId + "@example.test");
    }

    @Test
    void flywaySeedsCurrencies() {
        Integer currencyCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM currencies", Integer.class);

        assertThat(currencyCount).isEqualTo(4);
    }

    @Test
    void consumesSingleUseTokensAndRotatesRefreshTokens() {
        Instant now = Instant.now();
        authTokenRepository.saveEmailVerificationToken(userPublicId, "a".repeat(64),
                now.plusSeconds(60));
        authTokenRepository.savePasswordResetToken(userPublicId, "b".repeat(64),
                now.plusSeconds(60));
        authTokenRepository.saveRefreshToken(userPublicId, "c".repeat(64), now.plusSeconds(60));
        authTokenRepository.saveOAuthExchangeCode(userPublicId, "f".repeat(64),
                now.plusSeconds(60));

        assertThat(authTokenRepository.consumeEmailVerificationToken("a".repeat(64), now))
                .contains(userPublicId);
        assertThat(authTokenRepository.consumeEmailVerificationToken("a".repeat(64), now)).isEmpty();
        assertThat(authTokenRepository.consumePasswordResetToken("b".repeat(64), now))
                .contains(userPublicId);
        assertThat(authTokenRepository.rotateRefreshToken("c".repeat(64), "d".repeat(64),
                now.plusSeconds(120), now)).contains(userPublicId);
        assertThat(authTokenRepository.rotateRefreshToken("c".repeat(64), "e".repeat(64),
                now.plusSeconds(120), now)).isEmpty();
        assertThat(authTokenRepository.consumeOAuthExchangeCode("f".repeat(64), now))
                .contains(userPublicId);
        assertThat(authTokenRepository.consumeOAuthExchangeCode("f".repeat(64), now)).isEmpty();
    }

    @Test
    void savesAndLoadsCharacterAggregate() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));

        Character saved = repository.save(source);
        entityManager.clear();
        Character loaded = repository.findById(saved.id()).orElseThrow();

        assertThat(repository.countByUserId(userPublicId)).isEqualTo(1);
        assertThat(loaded.id()).isEqualTo(source.id());
        assertThat(loaded.name()).isEqualTo("Arden");
        assertThat(loaded.info().level()).isEqualTo(1);
        assertThat(loaded.skills()).hasSize(30);
        assertThat(loaded.inventory().slots()).hasSize(10);
        assertThat(loaded.money().amountBase()).isEqualByComparingTo("0.00");
    }

    @Test
    void replacesExistingCharacterSnapshotOnRepeatedSave() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));

        repository.save(source);
        repository.save(source);

        assertThat(repository.countByUserId(userPublicId)).isEqualTo(1);
    }

    @Test
    void savesAndLoadsUpdatedCharacterSheetSections() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));
        CharacterCondition condition = CharacterCondition.initial();
        condition.updateHp(BodyPart.HEAD, new BodyPartHealth(60, 30));
        source.updateInfo("Bryn", new CharacterInfo(3, "North", "Mercenary", "Warrior", "Berserker"));
        source.updateImage("/media/portrait.jpg");
        source.updateStats(new CharacterStats(Map.of(
                StatGroup.STRENGTH, 4, StatGroup.DEXTERITY, 3, StatGroup.STAMINA, 2,
                StatGroup.INTELLIGENCE, 1, StatGroup.CHARISMA, 0, StatGroup.LUCK, 1,
                StatGroup.MIND, 2)));
        source.updateSkills(Map.of(SkillName.ATHLETICS, 2));
        source.updateCondition(condition);
        source.updateBlessings(new BlessingInspiration(1, 2));
        source.updateAdditionalInfo(new AdditionalInfo("Scar", "North", "Guild", "A", "B"));
        Item helmet = new Item(UUID.randomUUID(), ItemType.EQUIPMENT, "Helmet", "helmet.jpg",
                BigDecimal.ONE, "", EquipmentType.HEAD, null);
        source.inventory().add(helmet, 0);
        source.equip(EquipmentSlotCode.HEAD, helmet.id());
        source.selectDisplayCurrency(Currency.CURRENCY_3);
        Spell spell = new Spell(UUID.randomUUID(), "Flame", SpellType.SPELL, SpellClass.PRIEST,
                "flame.jpg", "Level 2", "");
        source.addSpell(spell);

        repository.save(source);
        entityManager.clear();
        Character loaded = repository.findById(source.id()).orElseThrow();

        assertThat(loaded.name()).isEqualTo("Bryn");
        assertThat(loaded.image()).isEqualTo("/media/portrait.jpg");
        assertThat(loaded.info().level()).isEqualTo(3);
        assertThat(loaded.stats().level(StatGroup.STRENGTH)).isEqualTo(4);
        assertThat(loaded.skills().get(SkillName.ATHLETICS).level()).isEqualTo(2);
        assertThat(loaded.condition().healthResult().globalHealthPercent()).isEqualByComparingTo("50.00");
        assertThat(loaded.blessings()).isEqualTo(new BlessingInspiration(1, 2));
        assertThat(loaded.additionalInfo().allies()).isEqualTo("Guild");
        assertThat(loaded.equipment().get(EquipmentSlotCode.HEAD).itemId()).isEqualTo(helmet.id());
        assertThat(loaded.money().displayCurrency()).isEqualTo(Currency.CURRENCY_3);
        assertThat(loaded.spells()).containsKey(spell.id());
    }

    @Test
    void databaseAllowsOverweightSnapshot() {
        Long characterId = jdbcTemplate.queryForObject("""
                INSERT INTO characters (public_id, user_id, name)
                SELECT ?, id, 'Overweight' FROM users WHERE public_id = ?
                RETURNING id
                """, Long.class, UUID.randomUUID(), userPublicId);

        assertThatNoException().isThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO character_condition (
                    character_id, max_carry_weight, current_carry_weight
                ) VALUES (?, 10.00, 12.50)
                """, characterId));
    }

    @Test
    void databaseRejectsSalePriceForRegularItem() {
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> jdbcTemplate.update("""
                INSERT INTO items (
                    public_id, title, item_type, image_url, weight, sell_price_base_currency
                ) VALUES (?, 'Regular item', 'ITEM', 'item.jpg', 0, 10.00)
                """, UUID.randomUUID()))).isNotNull();
    }

    @Test
    void listsCharacterCardsAndDeletesAggregateItems() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));
        Item item = new Item(UUID.randomUUID(), ItemType.ITEM, "Rope", "rope.jpg",
                BigDecimal.ONE, "", null, null);
        source.inventory().add(item, 0);
        repository.save(source);

        assertThat(repository.findCardsByUserId(userPublicId)).singleElement()
                .satisfies(card -> assertThat(card.name()).isEqualTo("Arden"));

        repository.deleteById(source.id());

        assertThat(repository.findById(source.id())).isEmpty();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Integer.class)).isZero();
    }

    @Test
    void savesOauthUserAndFindsByIdentity() {
        User user = new User(UUID.randomUUID(), "oauth@example.test", null, Role.ROLE_USER, true,
                Instant.now(), List.of(new OAuthIdentity("google", "subject-1")));

        userRepository.save(user);
        userRepository.save(user);
        entityManager.clear();

        assertThat(userRepository.findByOAuthIdentity("google", "subject-1")).get()
                .satisfies(loaded -> {
                    assertThat(loaded.id()).isEqualTo(user.id());
                    assertThat(loaded.email()).isEqualTo(user.email());
                    assertThat(loaded.oauthIdentities()).containsExactly(new OAuthIdentity("google", "subject-1"));
                });
    }

    @Test
    void updatingUserVerificationStatePreservesCharacters() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));
        repository.save(source);
        User user = new User(userPublicId, userPublicId + "@example.test", "password-hash",
                Role.ROLE_USER, false, Instant.now(), List.of());

        userRepository.save(user.verified());

        assertThat(userRepository.findById(userPublicId)).get()
                .satisfies(loaded -> assertThat(loaded.emailVerified()).isTrue());
        assertThat(repository.findById(source.id())).isPresent();
    }

    @Test
    void persistsLoreAndCascadesRuleArticlesWithCategory() {
        LoreArticle lore = contentRepository.saveLoreArticle(
                new LoreArticle(0, "North", "Lore content", null, userPublicId, Instant.now()));
        RuleCategory category = contentRepository.saveRuleCategory(new RuleCategory(0, "Basics", ""));
        contentRepository.saveRuleBookArticle(new RuleBookArticle(0, category.id(), "Rolls", "Rules"));

        assertThat(contentRepository.findLoreArticle(lore.id())).get()
                .satisfies(loaded -> assertThat(loaded.title()).isEqualTo("North"));
        assertThat(contentRepository.findRuleBookArticlesByCategory(category.id())).hasSize(1);

        contentRepository.deleteRuleCategory(category.id());

        assertThat(contentRepository.findRuleBookArticlesByCategory(category.id())).isEmpty();
    }

    @Test
    void databaseCascadeDeletesCharacterItemsWhenUserIsDeleted() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));
        source.inventory().add(new Item(UUID.randomUUID(), ItemType.ITEM, "Rope", "rope.jpg",
                BigDecimal.ONE, "", null, null), 0);
        repository.save(source);

        jdbcTemplate.update("DELETE FROM users WHERE public_id = ?", userPublicId);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM characters", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Integer.class)).isZero();
    }

    @Test
    void deletingCharacterRemovesAggregateRows() {
        Character source = Character.createNew("Arden", Ownership.authenticated(userPublicId));
        source.inventory().add(new Item(UUID.randomUUID(), ItemType.ITEM, "Rope", "rope.jpg",
                BigDecimal.ONE, "", null, null), 0);
        repository.save(source);

        repository.deleteById(source.id());

        assertThat(repository.findById(source.id())).isEmpty();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Integer.class)).isZero();
    }

    @Test
    void deletingUserCascadesOauthIdentityAndClearsLoreEditor() {
        User user = new User(UUID.randomUUID(), "admin@example.test", null, Role.ROLE_ADMIN, true,
                Instant.now(), List.of(new OAuthIdentity("google", "admin-subject")));
        userRepository.save(user);
        LoreArticle lore = contentRepository.saveLoreArticle(
                new LoreArticle(0, "North", "Lore content", null, user.id(), Instant.now()));

        userRepository.deleteById(user.id());

        assertThat(userRepository.findByOAuthIdentity("google", "admin-subject")).isEmpty();
        assertThat(contentRepository.findLoreArticle(lore.id())).get()
                .satisfies(loaded -> assertThat(loaded.updatedBy()).isNull());
    }
}
