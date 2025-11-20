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
                How to play
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
                Ramudu–Sita is a Telugu party game where everyone secretly
                gets a chit (role) and only <strong>Ramudu</strong> knows who
                they are.
              </p>
              <ol className="list-decimal space-y-1 pl-5">
                <li>Create a game and share the code with friends.</li>
                <li>Everyone joins on their phone and gets a secret chit.</li>
                <li>
                  One player is <strong>Ramudu</strong>, one is{" "}
                  <strong>Sita</strong>, others are supporting characters.
                </li>
                <li>
                  Ramudu chats with everyone and tries to{" "}
                  <strong>guess who is Sita</strong> based on how they talk.
                </li>
                <li>
                  If Ramudu guesses correctly, both Ramudu and Sita get more
                  points. Otherwise, Sita wins that round.
                </li>
                <li>The scoreboard tracks points across all rounds.</li>
              </ol>
              <p className="text-xs text-slate-500">
                Tip: Play on a video call or in the same room so you can talk
                and bluff like the original childhood game.
              </p>
            </div>

            <div className="mt-4 flex justify-end">
              <button
                onClick={onClose}
                className="inline-flex items-center rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm hover:bg-indigo-500"
              >
                Got it, let’s play
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
