import { useGameStore } from "../store/gameStore";
import { Scoreboard } from "../components/Scoreboard";
import { RoundHistoryTable } from "../components/RoundHistoryTable";

export default function Reveal() {
  const state = useGameStore((s) => s.state);

  if (!state) return null;

  return (
    <div
      style={{
        padding: 20,
        display: "flex",
        gap: 20,
        alignItems: "flex-start",
      }}
    >
      <div style={{ flex: 2 }}>
        <h1>Round {state.currentRoundNumber} Result</h1>
        {state.lastRoundGuessName && (
          <p>Ramudu guessed: {state.lastRoundGuessName}</p>
        )}

        <p>Scores updated. Check the scoreboard on the right.</p>
      </div>

      <div style={{ flex: 1, minWidth: 260 }}>
        <Scoreboard />
        <RoundHistoryTable />
      </div>
    </div>
  );
}
