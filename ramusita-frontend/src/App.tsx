import { useEffect, useState } from "react";
import { useGameStore } from "./store/gameStore";
import Home from "./screens/Home";
import Lobby from "./screens/Lobby";
import Game from "./screens/Game";
import Reveal from "./screens/Reveal";
import Final from "./screens/Final";
import { HowToPlayModal } from "./components/HowToPlayModal";

export default function App() {
  const state = useGameStore((s) => s.state);
  const restore = useGameStore((s) => s.restoreFromStorage);
  const [showHelp, setShowHelp] = useState(false);

  useEffect(() => {
    restore();
  }, [restore]);

  let content = <Home />;

  if (state) {
    if (state.gameStatus === "LOBBY") content = <Lobby />;
    else if (state.gameStatus === "IN_ROUND") content = <Game />;
    else if (state.gameStatus === "REVEAL") content = <Reveal />;
    else if (state.gameStatus === "FINISHED") content = <Final />;
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="mx-auto flex min-h-screen max-w-5xl flex-col px-4 py-6 sm:px-6 lg:px-8">
        <header className="mb-4 flex items-center justify-between">
          <h1 className="text-xl font-semibold tracking-tight text-slate-800">
            Ramudu–Sita Chits
          </h1>
          <button
            onClick={() => setShowHelp(true)}
            className="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-600 shadow-sm hover:bg-slate-100"
          >
            How to play
          </button>
        </header>

        <main className="flex-1">{content}</main>

        <footer className="mt-6 text-xs text-slate-400">
          Built for friends &amp; family 💙
        </footer>
      </div>

      <HowToPlayModal open={showHelp} onClose={() => setShowHelp(false)} />
    </div>
  );
}
