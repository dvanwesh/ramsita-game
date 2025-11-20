import { useGameStore } from "../store/gameStore";

export function Scoreboard() {
  const state = useGameStore((s) => s.state);

  if (!state || !state.players) return null;

  const players = [...state.players].sort(
    (a: any, b: any) => b.totalScore - a.totalScore
  );

  return (
    <div
      style={{
        border: "1px solid #ddd",
        borderRadius: 8,
        padding: 12,
        marginBottom: 16,
        background: "#fafafa",
      }}
    >
      <h3 style={{ marginTop: 0 }}>Current Scores</h3>
      <ol style={{ paddingLeft: 20, margin: 0 }}>
        {players.map((p: any, idx: number) => (
          <li key={p.id} style={{ marginBottom: 4 }}>
            <strong>
              #{idx + 1} {p.name}
            </strong>{" "}
            — {p.totalScore} pts
          </li>
        ))}
      </ol>
    </div>
  );
}
