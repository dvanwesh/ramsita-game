import { AnimatePresence, motion } from "framer-motion";

interface Props {
  open: boolean;
  onClose: () => void;
}

export function HowToPlayModal({ open, onClose }: Props) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 px-4"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <motion.div
            className="max-w-lg rounded-2xl bg-white p-5 shadow-xl"
            initial={{ scale: 0.9, opacity: 0, y: 8 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.9, opacity: 0, y: 8 }}
            transition={{ duration: 0.18 }}
          >
            <div className="mb-3 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-800">
                How to Play
              </h2>
              <button
                onClick={onClose}
                className="rounded-full p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2 text-sm text-slate-600">
              <p>
                Rama–Sita is a fun party game where everyone receives a random
                chit (role). <strong>Rama</strong> must figure out who{" "}
                <strong>Sita</strong> is by talking to the other players.
              </p>

              <ol className="list-decimal space-y-1 pl-5">
                <li>Create a game and share the code with your friends.</li>
                <li>Everyone joins on their phone and gets a secret chit.</li>
                <li>
                  One player becomes <strong>Rama</strong>, one becomes{" "}
                  <strong>Sita</strong>, and others play as Bharata,
                  Shatrughna, or Hanuman.
                </li>
                <li>
                  Rama talks to everyone and tries to{" "}
                  <strong>guess who Sita is</strong> based on their behavior.
                </li>
                <li>
                  If Rama guesses correctly, Rama earns 5000 points. If the
                  guess is wrong, <strong>Sita wins the round</strong> and
                  receives 5000 points.
                </li>
                <li>
                  The scoreboard keeps track of points across all rounds, so
                  everyone can see who is leading.
                </li>
              </ol>

              <p className="text-xs text-slate-500">
                Tip: Play on a video call or in the same room so you can talk,
                bluff, and recreate the classic childhood experience.
              </p>
            </div>

            <div className="mt-4 flex justify-end">
              <button
                onClick={onClose}
                className="inline-flex items-center rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm hover:bg-indigo-500"
              >
                Got it, let’s play!
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
