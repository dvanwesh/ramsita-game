import { create } from "zustand";
import { http } from "../api/http";
import { createStompClient } from "../api/ws";

const STORAGE_KEY = "ramusita-session";

interface GameState {
  gameId: string | null;
  gameCode: string | null;
  me: any | null;
  state: any | null;
  stomp: any;

  createGame: (playerName: string, totalRounds: number) => Promise<void>;
  joinGame: (code: string, playerName: string) => Promise<void>;
  startGame: () => Promise<void>;
  loadMe: () => Promise<void>;
  guess: (playerId: string) => Promise<void>;
}

export const useGameStore = create<GameState>((set, get) => ({
  gameId: null,
  gameCode: null,
  me: null,
  state: null,
  stomp: null,

  async createGame(playerName, totalRounds) {
    const res = await http.post("/api/games", { playerName, totalRounds });
    const { gameId, gameCode, playerId } = res.data;

    // Start WebSocket for future updates
    const stomp = createStompClient(gameId, (s) => set({ state: s }));

    // Immediately load my state so UI can move from Home -> Lobby/Game
    const meRes = await http.get(`/api/games/${gameId}/me`);

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
    const { gameId, gameCode, playerId } = res.data;

    const stomp = createStompClient(gameId, (s) => set({ state: s }));

    const meRes = await http.get(`/api/games/${gameId}/me`);

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
    await http.post(`/api/games/${gameId}/start`);
    // after start, backend will broadcast; WS will update state
  },

  async loadMe() {
    const { gameId } = get();
    if (!gameId) return;
    const res = await http.get(`/api/games/${gameId}/me`);
    set({ me: res.data.me, state: res.data });
  },

  async guess(playerId) {
    const { gameId } = get();
    await http.post(`/api/games/${gameId}/rounds/current/guess`, {
      guessedPlayerId: playerId,
    });
    // backend broadcasts REVEAL/FINISHED; WS updates state
  },
}));
