// Small palette of distinct colors.
// Keep class names as literal strings so Tailwind doesn't purge them.
const colorVariants = [
  {
    avatarBg: "bg-indigo-100",
    avatarText: "text-indigo-700",
    pillBg: "bg-indigo-50",
    pillText: "text-indigo-700",
    tableHeaderBg: "bg-indigo-50",
  },
  {
    avatarBg: "bg-emerald-100",
    avatarText: "text-emerald-700",
    pillBg: "bg-emerald-50",
    pillText: "text-emerald-700",
    tableHeaderBg: "bg-emerald-50",
  },
  {
    avatarBg: "bg-amber-100",
    avatarText: "text-amber-800",
    pillBg: "bg-amber-50",
    pillText: "text-amber-800",
    tableHeaderBg: "bg-amber-50",
  },
  {
    avatarBg: "bg-rose-100",
    avatarText: "text-rose-700",
    pillBg: "bg-rose-50",
    pillText: "text-rose-700",
    tableHeaderBg: "bg-rose-50",
  },
  {
    avatarBg: "bg-sky-100",
    avatarText: "text-sky-700",
    pillBg: "bg-sky-50",
    pillText: "text-sky-700",
    tableHeaderBg: "bg-sky-50",
  },
];

export type PlayerColor = (typeof colorVariants)[number];

function simpleHash(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i++) {
    h = (h * 31 + str.charCodeAt(i)) | 0;
  }
  return Math.abs(h);
}

export function getPlayerColor(playerId: string): PlayerColor {
  const idx = simpleHash(playerId) % colorVariants.length;
  return colorVariants[idx];
}
