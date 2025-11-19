import { useGameStore } from "./store/gameStore";
import Home from "./screens/Home";
import Lobby from "./screens/Lobby";
import Game from "./screens/Game";
import Reveal from "./screens/Reveal";
import Final from "./screens/Final";

export default function App() {
  const state = useGameStore((s) => s.state);

  if (!state) return <Home />;

  if (state.gameStatus === "LOBBY") return <Lobby />;
  if (state.gameStatus === "IN_ROUND") return <Game />;
  if (state.gameStatus === "REVEAL") return <Reveal />;
  if (state.gameStatus === "FINISHED") return <Final />;

  return <Home />;
}
