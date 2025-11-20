import { useGameStore } from "../store/gameStore";
import { Scoreboard } from "../components/Scoreboard";
import { RoundHistoryTable } from "../components/RoundHistoryTable";

export default function Game() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);
  const guess = useGameStore((s) => s.guess);

  if (!state) return <div style={{ padding: 20 }}>Loading...</div>;

  const isRamudu = state.myChit === "RAMUDU";

  return (
    <div
      style={{
        padding: 20,
        display: "flex",
        gap: 20,
        alignItems: "flex-start",
      }}
    >
      {/* Left: main round interaction */}
      <div style={{ flex: 2 }}>
        <h1>Round {state.currentRoundNumber}</h1>
        <h2>Your chit: {state.myChit}</h2>

        {isRamudu ? (
          <>
            <h3>Guess who is SITA</h3>
            {state.players
              .filter((p: any) => p.id !== me.id)
              .map((p: any) => (
                <button
                  key={p.id}
                  onClick={() => guess(p.id)}
                  style={{ display: "block", margin: "8px 0", padding: "8px 12px" }}
                >
                  {p.name}
                </button>
              ))}
          </>
        ) : (
          <p>Waiting for Ramudu to make a guess…</p>
        )}
      </div>

      {/* Right: live scores + history */}
      <div style={{ flex: 1, minWidth: 260 }}>
        <Scoreboard />
        <RoundHistoryTable />
      </div>
    </div>
  );
}
