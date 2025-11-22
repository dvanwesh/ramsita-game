import HowToPlayContent from "../components/HowToPlayContent";
import { Link } from "react-router-dom";

export default function HowToPlayPage() {
  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      <h1 className="text-2xl font-bold text-slate-900 mb-4">
        How to Play Ramudu–Sita (Rama–Sita) Chits Game
      </h1>

      <HowToPlayContent />

      <div className="mt-6">
        <Link
          to="/"
          className="text-indigo-600 hover:text-indigo-500 text-sm font-medium"
        >
          ← Back to Home
        </Link>
      </div>
    </div>
  );
}
