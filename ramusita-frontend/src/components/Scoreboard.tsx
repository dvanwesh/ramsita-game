import { useGameStore } from "../store/gameStore";
import { getPlayerColor } from "../utils/playerColors";

export function Scoreboard() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);

  if (!state || !state.players) return null;

  const players = [...state.players].sort(
    (a: any, b: any) => b.totalScore - a.totalScore
  );

  const maxScore =
    players.length > 0
      ? Math.max(...players.map((p: any) => p.totalScore ?? 0))
      : 0;

  return (
    <div className="mb-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <h3 className="mb-2 text-sm font-semibold text-slate-700">
        Current scores
      </h3>
      <ol className="space-y-1 text-sm">
        {players.map((p: any, idx: number) => {
          const color = getPlayerColor(p.id);
          const isWinner = p.totalScore === maxScore && maxScore > 0;

          return (
            <li
              key={p.id}
              className={[
                "flex items-center justify-between rounded-lg px-2 py-1",
                isWinner
                  ? "bg-amber-50 border border-amber-200"
                  : "bg-slate-50 border border-slate-100",
              ].join(" ")}
            >
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-slate-400">
                  #{idx + 1}
                </span>
                <span
                  className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-semibold ${color.avatarBg} ${color.avatarText}`}
                >
                  {p.name.charAt(0).toUpperCase()}
                </span>
                <span className="font-medium text-slate-800 flex items-center gap-1">
                  {p.name}
                  {isWinner && (
                    <span
                      className="text-[13px]"
                      role="img"
                      aria-label="winner"
                    >
                      👑
                    </span>
                  )}
                </span>
                {p.id === me?.id && (
                  <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
                    You
                  </span>
                )}
              </div>
              <span className="font-medium text-slate-700">
                {p.totalScore} pts
              </span>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
