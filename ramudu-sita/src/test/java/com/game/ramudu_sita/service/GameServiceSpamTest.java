package com.game.ramudu_sita.service;

import com.game.ramudu_sita.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameServiceSpamTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    private GameService service;
    @BeforeEach
    void setUp() {
        // we don't care about WS interactions in this class, just core logic
        service = new GameService(messagingTemplate);
    }

    @Test
    void shouldAllowUpToMaxActiveGamesPerCreator() {
        String ip = "1.2.3.4";

        // assume limit = 5 (your chosen number)
        for (int i = 0; i < 5; i++) {
            var result = service.createGame("Host-" + i, 3, ip);
            assertNotNull(result);
        }
    }

    @Test
    void shouldRejectWhenCreatorExceedsMaxActiveGames() {
        String ip = "9.9.9.9";

        for (int i = 0; i < 5; i++) {
            service.createGame("Host-" + i, 3, ip);
        }

        assertThrows(
                IllegalStateException.class,
                () -> service.createGame("Overflow", 3, ip)
        );
    }

    @Test
    void finishedGamesShouldNotCountAgainstLimit() {
        String ip = "5.5.5.5";

        var r1 = service.createGame("A", 3, ip);
        var r2 = service.createGame("B", 3, ip);

        var g1 = service.getGame(r1.gameId());
        g1.setStatus(GameStatus.FINISHED);

        assertDoesNotThrow(() -> service.createGame("C", 3, ip));
    }
}
