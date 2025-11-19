package com.game.ramudu_sita.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void getCurrentRound_returnsRoundMatchingNumber() {
        GameState game = new GameState("game-id", "ABC123", 5);
        assertNull(game.getCurrentRound());

        RoundState r1 = new RoundState(1);
        RoundState r2 = new RoundState(2);

        game.getRounds().put(1, r1);
        game.getRounds().put(2, r2);

        game.setCurrentRoundNumber(1);
        assertSame(r1, game.getCurrentRound());

        game.setCurrentRoundNumber(2);
        assertSame(r2, game.getCurrentRound());

        game.setCurrentRoundNumber(3);
        assertNull(game.getCurrentRound());
    }
}

