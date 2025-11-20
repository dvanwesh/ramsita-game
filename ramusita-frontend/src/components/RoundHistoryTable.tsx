import { useGameStore } from "../store/gameStore";
import { getPlayerColor } from "../utils/playerColors";

export function RoundHistoryTable() {
  const state = useGameStore((s) => s.state);
  const roundHistory = useGameStore((s) => s.roundHistory);

  if (!state || !state.players || roundHistory.length === 0) return null;

  const players = [...state.players].sort(
    (a: any, b: any) => b.totalScore - a.totalScore
  );

  const sortedHistory = roundHistory
    .slice()
    .sort((a, b) => a.roundNumber - b.roundNumber);

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <h3 className="mb-2 text-sm font-semibold text-slate-700">
        Score by round
      </h3>

      {/* Desktop / tablet: table */}
      <div className="hidden overflow-x-auto text-xs sm:block">
        <table className="min-w-full border-collapse text-[11px]">
          <thead>
            <tr>
              <th className="whitespace-nowrap border border-slate-200 bg-slate-100 px-2 py-1 text-left font-semibold">
                Round
              </th>
              {players.map((p: any) => {
                const color = getPlayerColor(p.id);
                return (
                  <th
                    key={p.id}
                    className={`whitespace-nowrap border border-slate-200 px-2 py-1 text-right font-semibold ${color.tableHeaderBg}`}
                  >
                    {p.name}
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {sortedHistory.map((round) => (
              <tr key={round.roundNumber}>
                <td className="border border-slate-200 px-2 py-1 font-medium">
                  {round.roundNumber}
                </td>
                {players.map((p: any) => {
                  const delta = round.deltas[p.id] ?? 0;
                  const total = round.totals[p.id] ?? 0;
                  return (
                    <td
                      key={p.id}
                      className="border border-slate-200 px-2 py-1 text-right font-mono"
                    >
                      <span className="mr-1 text-slate-500">
                        {delta >= 0 ? `+${delta}` : delta}
                      </span>
                      <span className="font-semibold text-slate-800">
                        ({total})
                      </span>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile: stacked cards */}
      <div className="space-y-2 text-xs sm:hidden">
        {sortedHistory.map((round) => (
          <div
            key={round.roundNumber}
            className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2"
          >
            <div className="mb-1 text-[11px] font-semibold uppercase tracking-wide text-slate-500">
              Round {round.roundNumber}
            </div>
            <div className="space-y-1">
              {players.map((p: any) => {
                const delta = round.deltas[p.id] ?? 0;
                const total = round.totals[p.id] ?? 0;
                const color = getPlayerColor(p.id);
                return (
                  <div
                    key={p.id}
                    className="flex items-baseline justify-between"
                  >
                    <span
                      className={`mr-2 rounded-full px-2 py-0.5 text-[11px] font-medium ${color.pillBg} ${color.pillText}`}
                    >
                      {p.name}
                    </span>
                    <span className="font-mono text-[11px] text-slate-700">
                      <span className="mr-1 text-slate-500">
                        {delta >= 0 ? `+${delta}` : delta}
                      </span>
                      <span className="font-semibold">({total})</span>
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
