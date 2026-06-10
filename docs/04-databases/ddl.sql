CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_GUEST', 'ROLE_USER', 'ROLE_ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE character_info (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    level SMALLINT NOT NULL DEFAULT 1 CHECK (level > 0),
    origin VARCHAR(120) NOT NULL,
    background VARCHAR(120) NOT NULL,
    class_name VARCHAR(120) NOT NULL,
    portrait_image_url VARCHAR(500),
    specialization VARCHAR(120),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE character_stats (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    strength SMALLINT NOT NULL DEFAULT 0 CHECK (strength >= 0),
    dexterity SMALLINT NOT NULL DEFAULT 0 CHECK (dexterity >= 0),
    stamina SMALLINT NOT NULL DEFAULT 0 CHECK (stamina >= 0),
    intelligence SMALLINT NOT NULL DEFAULT 0 CHECK (intelligence >= 0),
    charisma SMALLINT NOT NULL DEFAULT 0 CHECK (charisma >= 0),
    mind SMALLINT NOT NULL DEFAULT 0 CHECK (mind >= 0),
    luck SMALLINT NOT NULL DEFAULT 0 CHECK (luck >= 0),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE character_skills (
    id BIGSERIAL PRIMARY KEY,
    stats_id BIGINT NOT NULL,
    stat_group VARCHAR(20) NOT NULL CHECK (stat_group IN (
        'STRENGTH', 'DEXTERITY', 'STAMINA', 'INTELLIGENCE', 'CHARISMA', 'MIND', 'LUCK'
    )),
    skill_name VARCHAR(120) NOT NULL,
    skill_level SMALLINT NOT NULL DEFAULT 0 CHECK (skill_level >= 0),
    FOREIGN KEY (stats_id) REFERENCES character_stats(id) ON DELETE CASCADE,
    UNIQUE (stats_id, skill_name)
);

CREATE TABLE character_condition (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,

    global_health_percent DECIMAL(5,2) NOT NULL DEFAULT 100.00
        CHECK (global_health_percent BETWEEN 0 AND 100),

    head_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (head_max_hp >= 0),
    head_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (head_current_hp >= 0),

    neck_max_hp INTEGER NOT NULL DEFAULT 40 CHECK (neck_max_hp >= 0),
    neck_current_hp INTEGER NOT NULL DEFAULT 40 CHECK (neck_current_hp >= 0),

    torso_max_hp INTEGER NOT NULL DEFAULT 100 CHECK (torso_max_hp >= 0),
    torso_current_hp INTEGER NOT NULL DEFAULT 100 CHECK (torso_current_hp >= 0),

    left_arm_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_arm_max_hp >= 0),
    left_arm_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_arm_current_hp >= 0),

    right_arm_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_arm_max_hp >= 0),
    right_arm_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_arm_current_hp >= 0),

    left_leg_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_leg_max_hp >= 0),
    left_leg_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (left_leg_current_hp >= 0),

    right_leg_max_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_leg_max_hp >= 0),
    right_leg_current_hp INTEGER NOT NULL DEFAULT 60 CHECK (right_leg_current_hp >= 0),

    passive_defense INTEGER NOT NULL DEFAULT 0 CHECK (passive_defense >= 0),
    passive_dodge INTEGER NOT NULL DEFAULT 0 CHECK (passive_dodge >= 0),

    movement_speed INTEGER NOT NULL DEFAULT 0 CHECK (movement_speed >= 0),

    max_carry_weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (max_carry_weight >= 0),
    current_carry_weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (current_carry_weight >= 0),

    CHECK (head_current_hp <= head_max_hp),
    CHECK (neck_current_hp <= neck_max_hp),
    CHECK (torso_current_hp <= torso_max_hp),
    CHECK (left_arm_current_hp <= left_arm_max_hp),
    CHECK (right_arm_current_hp <= right_arm_max_hp),
    CHECK (left_leg_current_hp <= left_leg_max_hp),
    CHECK (right_leg_current_hp <= right_leg_max_hp),
    CHECK (current_carry_weight <= max_carry_weight),

    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE blessing_inspiration (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    blessings INTEGER NOT NULL DEFAULT 0 CHECK (blessings >= 0),
    inspirations INTEGER NOT NULL DEFAULT 0 CHECK (inspirations >= 0),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE additional_info (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    appearance TEXT NOT NULL,
    detailed_origin TEXT NOT NULL,
    allies_and_organizations TEXT NOT NULL,
    notes_primary TEXT NOT NULL,
    notes_secondary TEXT NOT NULL,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    item_type VARCHAR(20) NOT NULL CHECK (item_type IN ('ITEM', 'EQUIPMENT', 'TRADE')),
    equipment_slot_type VARCHAR(30) CHECK (
        equipment_slot_type IN (
            'HEAD',
            'NECK',
            'TORSO',
            'ARMS',
            'LEGS',
            'WEAPON',
            'TALISMAN'
        )
    ),
    description TEXT,
    image_url VARCHAR(500) NOT NULL,
    weight DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (weight >= 0),
    sell_price_base_currency DECIMAL(12,2) CHECK (sell_price_base_currency >= 0),
    CHECK (
        (item_type = 'TRADE' AND sell_price_base_currency IS NOT NULL)
        OR
        (item_type IN ('ITEM', 'EQUIPMENT'))
    ),
    CHECK (
        (item_type = 'EQUIPMENT' AND equipment_slot_type IS NOT NULL)
        OR
        (item_type IN ('ITEM', 'TRADE') AND equipment_slot_type IS NULL)
    )
);

CREATE TABLE inventory_slots (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    slot_index INTEGER NOT NULL CHECK (slot_index >= 0),
    item_id BIGINT UNIQUE,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE SET NULL,
    UNIQUE (inventory_id, slot_index)
);

CREATE TABLE equipment_slots (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    slot_code VARCHAR(30) NOT NULL CHECK (
        slot_code IN (
            'HEAD',
            'NECK',
            'TORSO',
            'ARMS',
            'LEGS',
            'WEAPON_1',
            'WEAPON_2',
            'TALISMAN_1',
            'TALISMAN_2',
            'TALISMAN_3',
            'TALISMAN_4'
        )
    ),
    item_id BIGINT UNIQUE,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE SET NULL,
    UNIQUE (character_id, slot_code)
);

CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    rate_to_base DECIMAL(14,6) NOT NULL CHECK (rate_to_base > 0)
);

CREATE TABLE money (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    currency_id BIGINT NOT NULL,
    amount DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (currency_id) REFERENCES currencies(id) ON DELETE RESTRICT
);

CREATE TABLE spells (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    spell_name VARCHAR(150) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    spell_type VARCHAR(30) NOT NULL CHECK (spell_type IN ('SPELL', 'CANTRIP', 'RITUAL')),
    spell_class VARCHAR(30) NOT NULL CHECK (spell_class IN (
        'PRIEST',
        'SPELLCASTER',
        'WARLOCK',
        'DRUID',
        'ARTIST',
        'INQUISITOR',
        'SAVAGE'
    )),
    requirements TEXT NOT NULL,
    description TEXT,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

CREATE TABLE lore_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE rule_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE rule_book_articles (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES rule_categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_characters_user_id ON characters(user_id);
CREATE INDEX idx_character_skills_stats_id ON character_skills(stats_id);
CREATE INDEX idx_inventory_slots_inventory_id ON inventory_slots(inventory_id);
CREATE INDEX idx_equipment_slots_character_id ON equipment_slots(character_id);
CREATE INDEX idx_money_currency_id ON money(currency_id);
CREATE INDEX idx_spells_character_id ON spells(character_id);
CREATE INDEX idx_rule_book_articles_category_id ON rule_book_articles(category_id);
CREATE INDEX idx_lore_articles_updated_by ON lore_articles(updated_by);
CREATE INDEX idx_items_item_type ON items(item_type);
CREATE INDEX idx_items_equipment_slot_type ON items(equipment_slot_type);