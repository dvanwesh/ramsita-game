import { useGameStore } from "../store/gameStore";

export default function Reveal() {
  const state = useGameStore((s) => s.state);

  if (!state) return null;

  return (
    <div style={{ padding: 20 }}>
      <h1>Reveal</h1>
      <h2>Ramudu guessed: {state.lastRoundGuessName}</h2>

      <h3>Scores</h3>
      <ul>
        {state.players.map((p: any) => (
          <li key={p.id}>
            {p.name}: +{state.lastRoundScoreDelta[p.id]} (total {p.totalScore})
          </li>
        ))}
      </ul>
    </div>
  );
}
