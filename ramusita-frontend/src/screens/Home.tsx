import { useState } from "react";
import { useGameStore } from "../store/gameStore";
import { Link } from "react-router-dom";

const MIN_ROUNDS = 1;
const MAX_ROUNDS = 10;

function clampRounds(value: number) {
  if (Number.isNaN(value)) return MIN_ROUNDS;
  return Math.max(MIN_ROUNDS, Math.min(MAX_ROUNDS, value));
}

export default function Home() {
  const [name, setName] = useState("");
  const [rounds, setRounds] = useState<number>(1);
  const [code, setCode] = useState("");

  const create = useGameStore((s) => s.createGame);
  const join = useGameStore((s) => s.joinGame);

  const canCreate =
    name.trim().length > 0 &&
    rounds >= MIN_ROUNDS &&
    rounds <= MAX_ROUNDS;
  const canJoin = name.trim().length > 0 && code.trim().length > 0;

  const handleRoundsChange: React.ChangeEventHandler<HTMLInputElement> = (e) => {
    const raw = e.target.value;
    if (raw === "") {
      // Allow temporary empty state while typing
      setRounds(MIN_ROUNDS);
      return;
    }
    const num = Number(raw);
    setRounds(clampRounds(num));
  };

  const handleRoundsBlur: React.FocusEventHandler<HTMLInputElement> = (e) => {
    const num = Number(e.target.value);
    setRounds(clampRounds(num));
  };

  const incRounds = () => {
    setRounds((current) => clampRounds(current + 1));
  };

  const decRounds = () => {
    setRounds((current) => clampRounds(current - 1));
  };

  return (
    <div className="flex flex-col gap-6 md:flex-row">
      {/* Create */}
      <div className="flex-1 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="mb-1 text-lg font-semibold text-slate-800">
          Create a new game
        </h2>
        <p className="mb-4 text-sm text-slate-500">
          Share the game code with your friends so they can join.
        </p>

        <div className="space-y-3">
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">
              Your name
            </label>
            <input
              className="w-full rounded-lg border border-slate-300 bg-slate-50 px-3 py-2 text-sm outline-none ring-0 focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Ram, Sita…"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">
              Number of rounds
            </label>

            {/* Mobile-friendly stepper */}
            <div className="inline-flex items-center gap-2">
              <button
                type="button"
                onClick={decRounds}
                disabled={rounds <= MIN_ROUNDS}
                className="flex h-9 w-9 items-center justify-center rounded-full border border-slate-300 bg-slate-50 text-base font-semibold text-slate-700 shadow-sm hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                aria-label="Decrease rounds"
              >
                –
              </button>

              <input
                type="number"
                inputMode="numeric"
                min={MIN_ROUNDS}
                max={MAX_ROUNDS}
                className="w-20 rounded-lg border border-slate-300 bg-slate-50 px-3 py-2 text-center text-sm outline-none focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
                value={rounds}
                onChange={handleRoundsChange}
                onBlur={handleRoundsBlur}
              />

              <button
                type="button"
                onClick={incRounds}
                disabled={rounds >= MAX_ROUNDS}
                className="flex h-9 w-9 items-center justify-center rounded-full border border-slate-300 bg-slate-50 text-base font-semibold text-slate-700 shadow-sm hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                aria-label="Increase rounds"
              >
                +
              </button>
            </div>

            <p className="mt-1 text-[11px] text-slate-400">
              Choose between {MIN_ROUNDS} and {MAX_ROUNDS} rounds.
            </p>
          </div>

          <button
            disabled={!canCreate}
            onClick={() => create(name.trim(), clampRounds(rounds))}
            className="mt-2 inline-flex w-full items-center justify-center rounded-lg bg-indigo-600 px-3 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            Create game
          </button>
        </div>
      </div>

      {/* Join */}
      <div className="flex-1 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="mb-1 text-lg font-semibold text-slate-800">
          Join an existing game
        </h2>
        <p className="mb-4 text-sm text-slate-500">
          Enter the game code you got from the host.
        </p>

        <div className="space-y-3">
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">
              Game code
            </label>
            <input
              className="w-full rounded-lg border border-slate-300 bg-slate-50 px-3 py-2 text-sm uppercase tracking-[0.15em] outline-none focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              placeholder="e.g. ABC123"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-slate-600">
              Your name
            </label>
            <input
              className="w-full rounded-lg border border-slate-300 bg-slate-50 px-3 py-2 text-sm outline-none ring-0 focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Lakshman, Hanuman…"
            />
          </div>

          <button
            disabled={!canJoin}
            onClick={() => join(code.trim(), name.trim())}
            className="mt-2 inline-flex w-full items-center justify-center rounded-lg bg-emerald-600 px-3 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            Join game
          </button>
        </div>
      </div>
    </div>
  );
}
