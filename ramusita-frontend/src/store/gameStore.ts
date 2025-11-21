import { create } from "zustand";
import { http } from "../api/http";
import { createStompClient } from "../api/ws";

const STORAGE_KEY = "ramusita-session";

interface RoundHistoryEntry {
    roundNumber: number;
    deltas: Record<string, number>;  // playerId -> delta for that round
    totals: Record<string, number>;  // playerId -> total after that round
}

interface GameState {
    gameId: string | null;
    gameCode: string | null;
    me: any | null;
    state: any | null;
    stomp: any | null;
    roundHistory: RoundHistoryEntry[];

    createGame: (playerName: string, totalRounds: number) => Promise<void>;
    joinGame: (code: string, playerName: string) => Promise<void>;
    startGame: () => Promise<void>;
    loadMe: () => Promise<void>;
    guess: (playerId: string) => Promise<void>;
    restoreFromStorage: () => Promise<void>;
    leaveGame: () => void;
}

export const useGameStore = create<GameState>((set, get) => {
    // helper: overlay /me onto current state
    const refreshMyState = async () => {
        const gameId = get().gameId;
        if (!gameId) return;

        const res = await http.get(`/games/${gameId}/me`);
        const my = res.data;

        set((current) => ({
            me: my.me,
            // merge so we keep public fields (like WebSocket gameStatus, etc.)
            state: {
                ...(current.state || {}),
                ...my,
            },
        }));
    };

    const attachWebSocket = (gameId: string) => {
        const stomp = createStompClient(gameId, async (publicState: any) => {
            // 1) Update state + round history based on *public* state
            set((current) => {
                const roundHistory = [...(current.roundHistory || [])];

                const isReveal = publicState.gameStatus === "REVEAL";
                const hasDelta = publicState.lastRoundScoreDelta != null;
                const roundNumber = publicState.currentRoundNumber;

                if (isReveal && hasDelta && roundNumber != null) {
                    const alreadyRecorded = roundHistory.some(
                        (entry) => entry.roundNumber === roundNumber
                    );

                    if (!alreadyRecorded) {
                        const deltas: Record<string, number> = publicState.lastRoundScoreDelta;
                        const totals: Record<string, number> = {};

                        (publicState.players || []).forEach((p: any) => {
                            totals[p.id] = p.totalScore;
                        });

                        roundHistory.push({
                            roundNumber,
                            deltas,
                            totals,
                        });
                    }
                }

                return {
                    ...current,
                    state: publicState,
                    roundHistory,
                };
            });

            // 2) immediately overlay private view (/me) to restore myChit etc.
            try {
                await refreshMyState();
            } catch (e) {
                console.warn("Failed to overlay /me on WS state", e);
            }
        });

        return stomp;
    };

    return {
        gameId: null,
        gameCode: null,
        me: null,
        state: null,
        stomp: null,
        roundHistory: [],

        async createGame(playerName, totalRounds) {
            const res = await http.post("/games", { playerName, totalRounds });
            const { gameId, gameCode } = res.data;

            const stomp = attachWebSocket(gameId);

            // initial /me for first view
            const meRes = await http.get(`/games/${gameId}/me`);
            const my = meRes.data;

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify({ gameId, gameCode })
            );

            set({
                gameId,
                gameCode,
                me: my.me,
                state: my,
                stomp,
                roundHistory: [], // NEW game, empty history
            });
        },

        async joinGame(code, playerName) {
            const res = await http.post("/games/join", { code, playerName });
            const { gameId, gameCode } = res.data;

            const stomp = attachWebSocket(gameId);

            const meRes = await http.get(`/games/${gameId}/me`);
            const my = meRes.data;

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify({ gameId, gameCode })
            );

            set({
                gameId,
                gameCode,
                me: my.me,
                state: my,
                stomp,
                roundHistory: [], // or keep existing if you want, but fresh is simpler
            });
        },

        async startGame() {
            const { gameId } = get();
            if (!gameId) return;
            await http.post(`/games/${gameId}/start`);
            // WS + refreshMyState will handle UI updates
        },

        async leaveGame() {
            if (typeof window !== "undefined") {
                localStorage.removeItem(STORAGE_KEY);
            }

            const stomp = get().stomp;
            if (stomp) {
                try {
                    stomp.disconnect();
                } catch (e) {
                    console.warn("Error disconnecting stomp:", e);
                }
            }

            set({
                gameId: null,
                gameCode: null,
                me: null,
                state: null,
                stomp: null,
                roundHistory: [],
            });
        },

        async loadMe() {
            await refreshMyState();
        },

        async guess(playerId) {
            const { gameId } = get();
            if (!gameId) return;
            await http.post(`/games/${gameId}/rounds/current/guess`, {
                guessedPlayerId: playerId,
            });
            // WS + refreshMyState will handle REVEAL/FINISHED
        },

        async restoreFromStorage() {
            if (typeof window === "undefined") return;
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return;

            let parsed: { gameId: string; gameCode: string };
            try {
                parsed = JSON.parse(raw);
            } catch {
                localStorage.removeItem(STORAGE_KEY);
                return;
            }

            const { gameId, gameCode } = parsed;

            try {
                const meRes = await http.get(`/games/${gameId}/me`);
                const my = meRes.data;

                const stomp = attachWebSocket(gameId);

                set({
                    gameId,
                    gameCode,
                    me: my.me,
                    state: my,
                    stomp,
                    roundHistory: [], // start fresh
                });
            } catch (e) {
                console.warn("Failed to restore /me, clearing storage", e);
                localStorage.removeItem(STORAGE_KEY);
            }
        },
    };
});
