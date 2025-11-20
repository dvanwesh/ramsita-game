import { useEffect } from "react";
import { useGameStore } from "./store/gameStore";
import Home from "./screens/Home";
import Lobby from "./screens/Lobby";
import Game from "./screens/Game";
import Reveal from "./screens/Reveal";
import Final from "./screens/Final";

export default function App() {
  const state = useGameStore((s) => s.state);
  const restore = useGameStore((s) => s.restoreFromStorage);

  useEffect(() => {
    restore();
  }, [restore]);

  let content: JSX.Element;
  if (!state) content = <Home />;
  else if (state.gameStatus === "LOBBY") content = <Lobby />;
  else if (state.gameStatus === "IN_ROUND") content = <Game />;
  else if (state.gameStatus === "REVEAL") content = <Reveal />;
  else if (state.gameStatus === "FINISHED") content = <Final />;
  else content = <Home />;

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <header className="w-full border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-4xl px-4 py-3 flex items-center justify-between">
          <div className="flex items-baseline gap-2">
            <span className="text-lg font-semibold tracking-tight">
              Ramudu–Sita
            </span>
            <span className="text-xs uppercase tracking-wide text-slate-400">
              chits
            </span>
          </div>
        </div>
      </header>

      <main className="flex-1 w-full">
        <div className="mx-auto max-w-4xl px-4 py-4 sm:py-6">{content}</div>
      </main>
    </div>
  );
}
