import { useGameStore } from "../store/gameStore";

export default function Lobby() {
  const state = useGameStore((s) => s.state);
  const me = useGameStore((s) => s.me);
  const start = useGameStore((s) => s.startGame);

  if (!state) return <div>Loading...</div>;

  const isHost = me?.host === true;

  return (
    <div style={{ padding: 20 }}>
      <h1>Lobby — Code: {state.gameCode}</h1>
      <h2>Players</h2>

      <ul>
        {state.players.map((p: any) => (
          <li key={p.id}>
            {p.name} {p.host ? "(Host)" : ""}
          </li>
        ))}
      </ul>

      {isHost && (
        <button
          onClick={start}
          disabled={state.players.length < 3}
        >
          Start Game
        </button>
      )}
    </div>
  );
}
