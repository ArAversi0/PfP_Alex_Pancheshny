package com.pfp.gamerules;

public record RollRepresentation(int diceCount, int dieSides, int flatBonus, Integer constant) {

    public static RollRepresentation constant(int value) {
        return new RollRepresentation(0, 0, 0, value);
    }

    public static RollRepresentation dice(int diceCount, int dieSides, int flatBonus) {
        return new RollRepresentation(diceCount, dieSides, flatBonus, null);
    }

    public String display() {
        if (constant != null) {
            return constant.toString();
        }
        String dice = diceCount + "d" + dieSides;
        return flatBonus == 0 ? dice : dice + "+" + flatBonus;
    }
}

