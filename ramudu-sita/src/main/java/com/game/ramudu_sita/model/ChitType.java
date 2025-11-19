package com.game.ramudu_sita.model;

public enum ChitType {
    RAMUDU(500, true, false),
    SITA(0, false, true),
    BHARATA(200, false, false),
    SHATRUGHNA(100, false, false),
    HANUMAN(400, false, false);

    private final int basePoints;
    private final boolean ramudu;
    private final boolean sita;

    ChitType(int basePoints, boolean ramudu, boolean sita) {
        this.basePoints = basePoints;
        this.ramudu = ramudu;
        this.sita = sita;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public boolean isRamudu() {
        return ramudu;
    }

    public boolean isSita() {
        return sita;
    }
}
