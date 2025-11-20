import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import {
  CHARACTER_DISPLAY_NAME,
  CHARACTER_IMAGE,
  DEFAULT_CHIT_IMAGE,
} from "../utils/characterAssets";

interface ChitCardProps {
  chitType: string | null;         // e.g. "RAMUDU", "SITA"
  roundNumber: number | null;      // change → replay animation
}

export function ChitCard({ chitType, roundNumber }: ChitCardProps) {
  const [showFace, setShowFace] = useState(false);

  // When chit changes (or new round), replay rolling animation:
  useEffect(() => {
    if (!chitType) return;
    setShowFace(false);
    const t = setTimeout(() => setShowFace(true), 900); // delay reveal
    return () => clearTimeout(t);
  }, [chitType, roundNumber]);

  const displayName =
    (chitType && CHARACTER_DISPLAY_NAME[chitType]) || chitType || "Unknown";

  const faceImage =
    (chitType && CHARACTER_IMAGE[chitType]) || DEFAULT_CHIT_IMAGE;

  return (
    <motion.div
      key={`${roundNumber}-${chitType || "none"}`}
      initial={{ opacity: 0, y: 10, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.25 }}
      className="mx-auto flex w-full max-w-xs flex-col items-center"
    >
      {/* Outer card */}
      <div className="relative flex h-48 w-full items-center justify-center">
        {/* Spinning / rolling container */}
        <motion.div
          initial={{ rotateY: 0 }}
          animate={{ rotateY: showFace ? 0 : 540 }} // 1.5 spins before reveal
          transition={{ duration: 0.9, ease: "easeInOut" }}
          className="relative h-40 w-64 [transform-style:preserve-3d]"
        >
          {/* BACK side (while spinning) */}
          {!showFace && (
            <div className="absolute inset-0 flex items-center justify-center rounded-2xl border border-slate-300 bg-slate-800/90 text-slate-100 shadow-md">
              <div className="flex flex-col items-center">
                <div className="mb-2 h-16 w-16 rounded-full border border-slate-500 bg-slate-700" />
                <span className="text-xs tracking-wide text-slate-300">
                  Rolling chits…
                </span>
              </div>
            </div>
          )}

          {/* FRONT side (final chit face) */}
          {showFace && (
            <div className="absolute inset-0 flex items-center justify-center rounded-2xl border border-slate-300 bg-white shadow-md">
              <div className="flex flex-col items-center">
                <div className="mb-3 h-20 w-20 overflow-hidden rounded-full border border-slate-200 bg-slate-100">
                  <img
                    src={faceImage}
                    alt={displayName}
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="text-center">
                  <div className="text-xs uppercase tracking-wide text-slate-500">
                    Your chit
                  </div>
                  <div className="text-lg font-semibold text-slate-900">
                    {displayName}
                  </div>
                  {chitType === "RAMUDU" && (
                    <div className="mt-1 rounded-full bg-indigo-100 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-indigo-700">
                      Ramudu
                    </div>
                  )}
                  {chitType === "SITA" && (
                    <div className="mt-1 rounded-full bg-rose-100 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-rose-700">
                      Sita
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </motion.div>
      </div>
    </motion.div>
  );
}
