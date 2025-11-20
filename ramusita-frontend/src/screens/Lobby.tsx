import { useGameStore } from "../store/gameStore";
import { getPlayerColor } from "../utils/playerColors";
import { motion, AnimatePresence } from "framer-motion";

export default function Lobby() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);
  const start = useGameStore((s) => s.startGame);

  if (!state) return null;

  const isHost = me?.host === true;
  const minPlayers = 3;
  const canStart = isHost && state.players.length >= minPlayers;

  return (
    <motion.div
      className="space-y-5"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -8 }}
      transition={{ duration: 0.2 }}
    >
      <div className="flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h2 className="text-xl font-semibold text-slate-800">Lobby</h2>
          <p className="text-sm text-slate-500">
            Waiting for players to join…
          </p>
        </div>

        <div className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-mono text-slate-50 shadow-sm">
          Code:{" "}
          <span className="tracking-[0.25em] text-emerald-300">
            {state.gameCode}
          </span>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        {/* Player list */}
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">
            Players ({state.players.length})
          </h3>
          <ul className="space-y-2">
            <AnimatePresence>
              {state.players.map((p: any) => {
                const color = getPlayerColor(p.id);
                return (
                  <motion.li
                    key={p.id}
                    initial={{ opacity: 0, y: 6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -6 }}
                    transition={{ duration: 0.18 }}
                    className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-sm"
                  >
                    <div className="flex items-center gap-2">
                      <span
                        className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-semibold ${color.avatarBg} ${color.avatarText}`}
                      >
                        {p.name.charAt(0).toUpperCase()}
                      </span>
                      <span className="font-medium text-slate-800">
                        {p.name}
                      </span>
                      {p.host && (
                        <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-amber-700">
                          Host
                        </span>
                      )}
                      {p.id === me?.id && (
                        <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-emerald-700">
                          You
                        </span>
                      )}
                    </div>
                  </motion.li>
                );
              })}
            </AnimatePresence>
          </ul>
        </div>

        {/* Host controls */}
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="mb-2 text-sm font-semibold text-slate-700">
            Game settings
          </h3>
          <p className="mb-4 text-sm text-slate-500">
            Total rounds:{" "}
            <span className="font-semibold text-slate-700">
              {state.totalRounds}
            </span>
          </p>

          {isHost ? (
            <button
              disabled={!canStart}
              onClick={start}
              className="inline-flex w-full items-center justify-center rounded-lg bg-indigo-600 px-3 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              {state.players.length < minPlayers
                ? `Need ${minPlayers - state.players.length} more player(s)`
                : "Start game"}
            </button>
          ) : (
            <p className="text-xs text-slate-500">
              Waiting for host to start the game…
            </p>
          )}
        </div>
      </div>
    </motion.div>
  );
}
