package com.pfp.desktop.foundation.audio;

public enum SoundEffect {
    BUTTON_CLICK("/com/pfp/desktop/audio/sounds/button_click.wav"),
    ADD_ITEM("/com/pfp/desktop/audio/sounds/item.wav"),
    ADD_SPELL("/com/pfp/desktop/audio/sounds/spell.wav"),
    ITEM_PICK("/com/pfp/desktop/audio/sounds/item_pick.wav"),
    INVENTORY_MOVE("/com/pfp/desktop/audio/sounds/inventory_move.wav"),
    EQUIP_ARMOR("/com/pfp/desktop/audio/sounds/equip_armor.wav"),
    EQUIP_WEAPON("/com/pfp/desktop/audio/sounds/equip_weapon.wav"),
    EQUIP_TALISMAN("/com/pfp/desktop/audio/sounds/equip_talisman.wav"),
    SELL_TRADE("/com/pfp/desktop/audio/sounds/sell_trade.wav"),
    PAGE_TURN("/com/pfp/desktop/audio/sounds/page_turn.wav"),
    WRITE("/com/pfp/desktop/audio/sounds/write.wav");

    private final String resourcePath;

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String resourcePath() {
        return resourcePath;
    }
}
