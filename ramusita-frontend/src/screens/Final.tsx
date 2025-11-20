import { useGameStore } from "../store/gameStore";

export default function Final() {
  const state = useGameStore((s) => s.state);
  const roundHistory = useGameStore((s) => s.roundHistory);
  const leaveGame = useGameStore((s) => s.leaveGame);

  if (!state) {
    return (
      <div style={{ padding: 20 }}>
        <h1>Game Finished</h1>
        <button onClick={leaveGame}>Back to Home</button>
      </div>
    );
  }

  const players = [...state.players].sort(
    (a: any, b: any) => b.totalScore - a.totalScore
  );

  return (
    <div style={{ padding: 20 }}>
      <h1>🏁 Game Finished</h1>

      <h2>Final Scores</h2>
      <ol>
        {players.map((p: any, idx: number) => (
          <li key={p.id} style={{ marginBottom: 8 }}>
            <strong>
              #{idx + 1} {p.name}
            </strong>{" "}
            — {p.totalScore} points
          </li>
        ))}
      </ol>

      {roundHistory.length > 0 && (
        <>
          <h2 style={{ marginTop: 24 }}>Score by Round</h2>
          <table
            style={{
              borderCollapse: "collapse",
              marginTop: 8,
              minWidth: "300px",
            }}
          >
            <thead>
              <tr>
                <th style={{ border: "1px solid #ccc", padding: "4px 8px" }}>
                  Round
                </th>
                {players.map((p: any) => (
                  <th
                    key={p.id}
                    style={{ border: "1px solid #ccc", padding: "4px 8px" }}
                  >
                    {p.name}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {roundHistory
                .slice()
                .sort((a, b) => a.roundNumber - b.roundNumber)
                .map((round) => (
                  <tr key={round.roundNumber}>
                    <td
                      style={{
                        border: "1px solid #ccc",
                        padding: "4px 8px",
                        fontWeight: "bold",
                      }}
                    >
                      {round.roundNumber}
                    </td>
                    {players.map((p: any) => {
                      const delta = round.deltas[p.id] ?? 0;
                      const total = round.totals[p.id] ?? 0;
                      return (
                        <td
                          key={p.id}
                          style={{
                            border: "1px solid #ccc",
                            padding: "4px 8px",
                            textAlign: "right",
                            whiteSpace: "nowrap",
                          }}
                        >
                          <span style={{ opacity: 0.7 }}>+{delta}</span>{" "}
                          <strong>({total})</strong>
                        </td>
                      );
                    })}
                  </tr>
                ))}
            </tbody>
          </table>
        </>
      )}

      <button
        onClick={leaveGame}
        style={{
          marginTop: 24,
          padding: "10px 20px",
          fontSize: 16,
          cursor: "pointer",
        }}
      >
        🔄 Start New Game
      </button>
    </div>
  );
}
