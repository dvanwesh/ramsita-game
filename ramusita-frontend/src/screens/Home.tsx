import { useState } from "react";
import { useGameStore } from "../store/gameStore";

export default function Home() {
  const [name, setName] = useState("");
  const [rounds, setRounds] = useState(1);
  const [code, setCode] = useState("");

  const create = useGameStore((s) => s.createGame);
  const join = useGameStore((s) => s.joinGame);

  return (
    <div style={{ padding: 20 }}>
      <h1>Ramudu-Sita Chits</h1>

      <h2>Create Game</h2>
      <input placeholder="Your Name" value={name} onChange={(e) => setName(e.target.value)} />
      <input type="number" value={rounds} onChange={(e) => setRounds(Number(e.target.value))} />
      <button onClick={() => create(name, rounds)}>Create</button>

      <h2>Join Game</h2>
      <input placeholder="Game Code" value={code} onChange={(e) => setCode(e.target.value)} />
      <input placeholder="Your Name" value={name} onChange={(e) => setName(e.target.value)} />
      <button onClick={() => join(code, name)}>Join</button>
    </div>
  );
}
