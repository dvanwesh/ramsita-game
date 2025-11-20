import { useEffect } from "react";
import { useGameStore } from "../store/gameStore";
import { Scoreboard } from "../components/Scoreboard";
import { RoundHistoryTable } from "../components/RoundHistoryTable";
import { motion } from "framer-motion";
import confetti from "canvas-confetti";

export default function Final() {
  const state = useGameStore((s) => s.state);
  const leaveGame = useGameStore((s) => s.leaveGame);

  // Fire confetti once when Final mounts
  useEffect(() => {
    // Small delay so the layout is visible first
    const timer = setTimeout(() => {
      confetti({
        particleCount: 160,
        spread: 70,
        origin: { y: 0.6 },
        scalar: 1,
      });
      // a second burst from the side
      confetti({
        particleCount: 80,
        angle: 120,
        spread: 60,
        origin: { x: 0, y: 0.6 },
        scalar: 0.9,
      });
      confetti({
        particleCount: 80,
        angle: 60,
        spread: 60,
        origin: { x: 1, y: 0.6 },
        scalar: 0.9,
      });
    }, 300);

    return () => clearTimeout(timer);
  }, []);

  // Fallback if someone reloads on final & state is gone
  if (!state) {
    return (
      <div className="flex items-center justify-center">
        <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm">
          <h1 className="mb-3 text-lg sm:text-xl font-semibold text-slate-800">
            Game finished
          </h1>
          <p className="mb-4 text-sm text-slate-500">
            The game data is no longer available. You can start a fresh game.
          </p>
          <button
            onClick={leaveGame}
            className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-500"
          >
            Back to home
          </button>
        </div>
      </div>
    );
  }

  return (
    <motion.div
      className="space-y-4"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -8 }}
      transition={{ duration: 0.2 }}
    >
      <div className="mx-auto w-full max-w-3xl rounded-2xl border border-slate-200 bg-white p-5 sm:p-6 shadow-sm">
        <div className="mb-3 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-lg sm:text-xl font-semibold text-slate-800">
              🏁 Game finished
            </h1>
            <p className="text-xs sm:text-sm text-slate-500">
              Thanks for playing! Here’s how everyone did.
            </p>
          </div>
          <div className="text-xs text-slate-500">
            Rounds played:{" "}
            <span className="font-semibold text-slate-800">
              {state.currentRoundNumber} / {state.totalRounds}
            </span>
          </div>
        </div>

        <div className="space-y-3">
          <Scoreboard />
          <RoundHistoryTable />
        </div>

        <div className="mt-4 flex flex-col items-stretch justify-between gap-3 sm:flex-row sm:items-center">
          <button
            onClick={leaveGame}
            className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-500"
          >
            🔄 Start a new game
          </button>
          <p className="text-center text-[11px] text-slate-400 sm:text-right">
            Share the scores with your friends and start another match with a
            new game code.
          </p>
        </div>
      </div>
    </motion.div>
  );
}
