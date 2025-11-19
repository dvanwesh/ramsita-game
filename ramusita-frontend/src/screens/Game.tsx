import { useGameStore } from "../store/gameStore";

export default function Game() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);
  const guess = useGameStore((s) => s.guess);

  if (!state) return <div>Loading...</div>;

  const isRamudu = state.myChit === "RAMUDU";

  return (
    <div style={{ padding: 20 }}>
      <h1>Round {state.currentRoundNumber}</h1>
      <h2>Your chit: {state.myChit}</h2>

      {isRamudu ? (
        <>
          <h3>Guess who is SITA</h3>
          {state.players
            .filter((p: any) => p.id !== me.id)
            .map((p: any) => (
              <button key={p.id} onClick={() => guess(p.id)} style={{ display: "block", margin: 8 }}>
                {p.name}
              </button>
            ))}
        </>
      ) : (
        <p>Waiting for Ramudu to guess…</p>
      )}
    </div>
  );
}
