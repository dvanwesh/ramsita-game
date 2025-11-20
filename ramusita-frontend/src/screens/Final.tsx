import { useGameStore } from "../store/gameStore";
import { Scoreboard } from "../components/Scoreboard";
import { RoundHistoryTable } from "../components/RoundHistoryTable";

export default function Final() {
  const state = useGameStore((s) => s.state);
  const leaveGame = useGameStore((s) => s.leaveGame);

  if (!state) {
    return (
      <div className="rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm">
        <h1 className="mb-3 text-xl font-semibold text-slate-800">
          Game finished
        </h1>
        <button
          onClick={leaveGame}
          className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-500"
        >
          Back to home
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h1 className="mb-2 text-xl font-semibold text-slate-800">
          🏁 Game finished
        </h1>
        <p className="mb-4 text-sm text-slate-500">
          Thanks for playing! Here’s how everyone did.
        </p>
        <Scoreboard />
        <RoundHistoryTable />
        <button
          onClick={leaveGame}
          className="mt-3 inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-500"
        >
          Start a new game
        </button>
      </div>
    </div>
  );
}
