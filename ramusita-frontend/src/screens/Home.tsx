import { useState } from "react";
import { useGameStore } from "../store/gameStore";

export default function Home() {
  const [name, setName] = useState("");
  const [rounds, setRounds] = useState(1);
  const [code, setCode] = useState("");

  const create = useGameStore((s) => s.createGame);
  const join = useGameStore((s) => s.joinGame);

  const canCreate = name.trim().length > 0 && rounds > 0;
  const canJoin = name.trim().length > 0 && code.trim().length > 0;

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
            <input
              type="number"
              min={1}
              max={10}
              className="w-24 rounded-lg border border-slate-300 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
              value={rounds}
              onChange={(e) => setRounds(Number(e.target.value) || 1)}
            />
          </div>

          <button
            disabled={!canCreate}
            onClick={() => create(name.trim(), rounds)}
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
