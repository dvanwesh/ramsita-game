import { create } from "zustand";
import { http } from "../api/http";
import { createStompClient } from "../api/ws";

const STORAGE_KEY = "ramusita-session";

interface GameState {
  gameId: string | null;
  gameCode: string | null;
  me: any | null;
  state: any | null;
  stomp: any | null;

  createGame: (playerName: string, totalRounds: number) => Promise<void>;
  joinGame: (code: string, playerName: string) => Promise<void>;
  startGame: () => Promise<void>;
  loadMe: () => Promise<void>;
  guess: (playerId: string) => Promise<void>;
  restoreFromStorage: () => Promise<void>;
}

export const useGameStore = create<GameState>((set, get) => ({
  gameId: null,
  gameCode: null,
  me: null,
  state: null,
  stomp: null,

  async createGame(playerName, totalRounds) {
    const res = await http.post("/api/games", { playerName, totalRounds });
    const { gameId, gameCode } = res.data;

    // Start WS client to receive future updates
    const stomp = createStompClient(gameId, (s) => set({ state: s }));

    // Immediately fetch my state so UI can leave Home
    const meRes = await http.get(`/api/games/${gameId}/me`);

    // Persist session for refresh
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ gameId, gameCode })
    );

    set({
      gameId,
      gameCode,
      me: meRes.data.me,
      state: meRes.data,
      stomp,
    });
  },

  async joinGame(code, playerName) {
    const res = await http.post("/api/games/join", { code, playerName });
    const { gameId, gameCode } = res.data;

    const stomp = createStompClient(gameId, (s) => set({ state: s }));
    const meRes = await http.get(`/api/games/${gameId}/me`);

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ gameId, gameCode })
    );

    set({
      gameId,
      gameCode,
      me: meRes.data.me,
      state: meRes.data,
      stomp,
    });
  },

  async startGame() {
    const { gameId } = get();
    if (!gameId) return;
    await http.post(`/api/games/${gameId}/start`);
    // WS will push updated state
  },

  async loadMe() {
    const { gameId } = get();
    if (!gameId) return;
    const res = await http.get(`/api/games/${gameId}/me`);
    set({ me: res.data.me, state: res.data });
  },

  async guess(playerId) {
    const { gameId } = get();
    if (!gameId) return;
    await http.post(`/api/games/${gameId}/rounds/current/guess`, {
      guessedPlayerId: playerId,
    });
    // WS will push REVEAL/FINISHED
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
      // This uses the PLAYER_TOKEN cookie to identify the player in this browser
      const meRes = await http.get(`/api/games/${gameId}/me`);

      const stomp = createStompClient(gameId, (s) => set({ state: s }));

      set({
        gameId,
        gameCode,
        me: meRes.data.me,
        state: meRes.data,
        stomp,
      });
    } catch (e) {
      // game may be gone or session invalid → clear stale storage
      console.warn("Failed to restore session, clearing local storage", e);
      localStorage.removeItem(STORAGE_KEY);
    }
  },
}));
