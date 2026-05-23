CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oauth_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    UNIQUE (provider, provider_subject)
);

CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    image_url VARCHAR(500) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE character_info (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level > 0),
    origin VARCHAR(120) NOT NULL DEFAULT '',
    background VARCHAR(120) NOT NULL DEFAULT '',
    class_name VARCHAR(120) NOT NULL DEFAULT '',
    specialization VARCHAR(120) NOT NULL DEFAULT ''
);

CREATE TABLE character_stats (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    strength INTEGER NOT NULL DEFAULT 0 CHECK (strength >= 0),
    dexterity INTEGER NOT NULL DEFAULT 0 CHECK (dexterity >= 0),
    stamina INTEGER NOT NULL DEFAULT 0 CHECK (stamina >= 0),
    intelligence INTEGER NOT NULL DEFAULT 0 CHECK (intelligence >= 0),
    charisma INTEGER NOT NULL DEFAULT 0 CHECK (charisma >= 0),
    luck INTEGER NOT NULL DEFAULT 0 CHECK (luck >= 0),
    mind INTEGER NOT NULL DEFAULT 0 CHECK (mind >= 0)
);

CREATE TABLE character_skills (
    id BIGSERIAL PRIMARY KEY,
    stats_id BIGINT NOT NULL REFERENCES character_stats(id) ON DELETE CASCADE,
    stat_group VARCHAR(20) NOT NULL CHECK (stat_group IN (
        'STRENGTH', 'DEXTERITY', 'STAMINA', 'INTELLIGENCE', 'CHARISMA', 'LUCK', 'MIND'
    )),
    skill_name VARCHAR(120) NOT NULL,
    skill_level INTEGER NOT NULL DEFAULT 0 CHECK (skill_level >= 0),
    UNIQUE (stats_id, skill_name)
);

CREATE TABLE character_condition (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    global_health_percent DECIMAL(5,2) NOT NULL DEFAULT 100.00
        CHECK (global_health_percent BETWEEN 0 AND 100),
    head_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (head_max_hp >= 0),
    head_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (head_current_hp BETWEEN 0 AND head_max_hp),
    neck_max_hp INTEGER NOT NULL DEFAULT 40 CHECK (neck_max_hp >= 0),
    neck_current_hp INTEGER NOT NULL DEFAULT 40 CHECK (neck_current_hp BETWEEN 0 AND neck_max_hp),
    torso_max_hp INTEGER NOT NULL DEFAULT 100 CHECK (torso_max_hp >= 0),
    torso_current_hp INTEGER NOT NULL DEFAULT 100 CHECK (torso_current_hp BETWEEN 0 AND torso_max_hp),
    left_arm_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_arm_max_hp >= 0),
    left_arm_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_arm_current_hp BETWEEN 0 AND left_arm_max_hp),
    right_arm_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_arm_max_hp >= 0),
    right_arm_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_arm_current_hp BETWEEN 0 AND right_arm_max_hp),
    left_leg_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_leg_max_hp >= 0),
    left_leg_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_leg_current_hp BETWEEN 0 AND left_leg_max_hp),
    right_leg_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_leg_max_hp >= 0),
    right_leg_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_leg_current_hp BETWEEN 0 AND right_leg_max_hp),
    passive_defense INTEGER NOT NULL DEFAULT 0 CHECK (passive_defense >= 0),
    passive_dodge INTEGER NOT NULL DEFAULT 3 CHECK (passive_dodge >= 0),
    movement_speed DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (movement_speed >= 0),
    max_carry_weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (max_carry_weight >= 0),
    current_carry_weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (current_carry_weight >= 0)
);

CREATE TABLE blessing_inspiration (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    blessings INTEGER NOT NULL DEFAULT 0 CHECK (blessings >= 0),
    inspirations INTEGER NOT NULL DEFAULT 0 CHECK (inspirations >= 0)
);

CREATE TABLE additional_info (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    appearance TEXT NOT NULL DEFAULT '',
    detailed_origin TEXT NOT NULL DEFAULT '',
    allies_and_organizations TEXT NOT NULL DEFAULT '',
    notes_primary TEXT NOT NULL DEFAULT '',
    notes_secondary TEXT NOT NULL DEFAULT ''
);

CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    item_type VARCHAR(20) NOT NULL CHECK (item_type IN ('ITEM', 'EQUIPMENT', 'TRADE')),
    equipment_slot_type VARCHAR(30) CHECK (equipment_slot_type IN (
        'HEAD', 'NECK', 'TORSO', 'ARMS', 'LEGS', 'WEAPON', 'TALISMAN'
    )),
    description TEXT NOT NULL DEFAULT '',
    image_url VARCHAR(500) NOT NULL,
    weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (weight >= 0),
    sell_price_base_currency DECIMAL(14,2) CHECK (sell_price_base_currency >= 0),
    CHECK (
        (item_type = 'EQUIPMENT' AND equipment_slot_type IS NOT NULL)
        OR (item_type IN ('ITEM', 'TRADE') AND equipment_slot_type IS NULL)
    ),
    CHECK (
        (item_type = 'TRADE' AND sell_price_base_currency IS NOT NULL)
        OR (item_type IN ('ITEM', 'EQUIPMENT') AND sell_price_base_currency IS NULL)
    )
);

CREATE TABLE inventory_slots (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventories(id) ON DELETE CASCADE,
    slot_index INTEGER NOT NULL CHECK (slot_index >= 0),
    item_id BIGINT UNIQUE REFERENCES items(id) ON DELETE SET NULL,
    UNIQUE (inventory_id, slot_index)
);

CREATE TABLE equipment_slots (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    slot_code VARCHAR(30) NOT NULL CHECK (slot_code IN (
        'HEAD', 'NECK', 'TORSO', 'ARMS', 'LEGS', 'WEAPON_1', 'WEAPON_2',
        'TALISMAN_1', 'TALISMAN_2', 'TALISMAN_3', 'TALISMAN_4'
    )),
    item_id BIGINT UNIQUE REFERENCES items(id) ON DELETE SET NULL,
    UNIQUE (character_id, slot_code)
);

CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    rate_to_base DECIMAL(14,6) NOT NULL CHECK (rate_to_base > 0)
);

CREATE TABLE money (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    currency_id BIGINT NOT NULL REFERENCES currencies(id) ON DELETE RESTRICT,
    amount DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (amount >= 0)
);

CREATE TABLE spells (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    spell_name VARCHAR(150) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    spell_type VARCHAR(30) NOT NULL CHECK (spell_type IN ('SPELL', 'CANTRIP', 'RITUAL')),
    spell_class VARCHAR(30) NOT NULL CHECK (spell_class IN (
        'PRIEST', 'SPELLCASTER', 'WARLOCK', 'DRUID', 'ARTIST', 'INQUISITOR', 'SAVAGE'
    )),
    requirements TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT ''
);

CREATE TABLE lore_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rule_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE rule_book_articles (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES rule_categories(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL
);

CREATE INDEX idx_characters_user_id ON characters(user_id);
CREATE INDEX idx_oauth_identities_user_id ON oauth_identities(user_id);
CREATE INDEX idx_character_skills_stats_id ON character_skills(stats_id);
CREATE INDEX idx_inventory_slots_inventory_id ON inventory_slots(inventory_id);
CREATE INDEX idx_equipment_slots_character_id ON equipment_slots(character_id);
CREATE INDEX idx_money_currency_id ON money(currency_id);
CREATE INDEX idx_spells_character_id ON spells(character_id);
CREATE INDEX idx_rule_book_articles_category_id ON rule_book_articles(category_id);
CREATE INDEX idx_lore_articles_updated_by ON lore_articles(updated_by);
CREATE INDEX idx_items_item_type ON items(item_type);
CREATE INDEX idx_items_equipment_slot_type ON items(equipment_slot_type);

INSERT INTO currencies (code, name, symbol, rate_to_base) VALUES
    ('CURRENCY_1', 'Валюта 1', 'В1', 1),
    ('CURRENCY_2', 'Валюта 2', 'В2', 10),
    ('CURRENCY_3', 'Валюта 3', 'В3', 100),
    ('CURRENCY_4', 'Валюта 4', 'В4', 1000);
