import { useGameStore } from "../store/gameStore";
import "./RoundHistoryTable.css";

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
    <div className="round-history">
      <h3 className="round-history__title">Score by Round</h3>

      {/* Desktop / tablet: table */}
      <div className="round-history__table-wrapper">
        <table className="round-history__table">
          <thead>
            <tr>
              <th className="round-history__th round-history__th--round">
                Round
              </th>
              {players.map((p: any) => (
                <th key={p.id} className="round-history__th">
                  {p.name}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {sortedHistory.map((round) => (
              <tr key={round.roundNumber}>
                <td className="round-history__td round-history__td--round">
                  {round.roundNumber}
                </td>
                {players.map((p: any) => {
                  const delta = round.deltas[p.id] ?? 0;
                  const total = round.totals[p.id] ?? 0;
                  return (
                    <td key={p.id} className="round-history__td">
                      <span className="round-history__delta">
                        {delta >= 0 ? `+${delta}` : delta}
                      </span>
                      <span className="round-history__total">
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

      {/* Mobile: cards */}
      <div className="round-history__cards">
        {sortedHistory.map((round) => (
          <div key={round.roundNumber} className="round-card">
            <div className="round-card__header">
              Round {round.roundNumber}
            </div>
            {players.map((p: any) => {
              const delta = round.deltas[p.id] ?? 0;
              const total = round.totals[p.id] ?? 0;
              return (
                <div key={p.id} className="round-card__row">
                  <span className="round-card__player">{p.name}</span>
                  <span className="round-card__score">
                    <span className="round-card__delta">
                      {delta >= 0 ? `+${delta}` : delta}
                    </span>
                    <span className="round-card__total">
                      ({total})
                    </span>
                  </span>
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}
