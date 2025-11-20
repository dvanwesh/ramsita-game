import { useGameStore } from "../store/gameStore";
import { Scoreboard } from "../components/Scoreboard";
import { RoundHistoryTable } from "../components/RoundHistoryTable";
import { ChitCard } from "../components/ChitCard";
import { motion } from "framer-motion";

export default function Game() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);
  const guess = useGameStore((s) => s.guess);

  if (!state) return null;

  const isRamudu = state.myChit === "RAMUDU";

  return (
    <motion.div
      className="flex flex-col gap-4 md:flex-row md:items-start"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -8 }}
      transition={{ duration: 0.2 }}
    >
      {/* Left: chit + actions */}
      <div className="flex-[2] space-y-4">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="mb-2 flex items-baseline justify-between">
            <div>
              <h2 className="text-xl font-semibold text-slate-800">
                Round {state.currentRoundNumber}
              </h2>
              <p className="text-xs text-slate-500">
                Total rounds: {state.totalRounds}
              </p>
            </div>
          </div>

          {/* Chit card with avatar + spin animation */}
          <ChitCard
            chitType={state.myChit}
            roundNumber={state.currentRoundNumber}
          />

          <div className="mt-4 border-t border-slate-100 pt-3 text-sm">
            {isRamudu ? (
              <>
                <p className="mb-2 text-slate-600">
                  You are <span className="font-semibold">Ramudu</span>. Guess
                  who is <span className="font-semibold">Sita</span>.
                </p>
                <div className="space-y-2">
                  {state.players
                    .filter((p: any) => p.id !== me?.id)
                    .map((p: any) => (
                      <button
                        key={p.id}
                        onClick={() => guess(p.id)}
                        className="flex w-full items-center justify-between rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-800 transition hover:border-indigo-300 hover:bg-indigo-50"
                      >
                        <span>{p.name}</span>
                        <span className="text-xs text-slate-400">
                          Tap to guess
                        </span>
                      </button>
                    ))}
                </div>
              </>
            ) : (
              <p className="text-slate-600">
                Waiting for{" "}
                <span className="font-semibold">Ramudu</span> to make a guess…
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Right: scoreboard & history */}
      <div className="flex-1 space-y-3">
        <Scoreboard />
        <RoundHistoryTable />
      </div>
    </motion.div>
  );
}
