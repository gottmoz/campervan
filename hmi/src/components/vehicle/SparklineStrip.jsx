import React from "react";

export function SparklineStrip({ label, values = [], accent = "#22d3ee", stale = false }) {
  const width = 180;
  const height = 44;
  const clean = values.filter(Number.isFinite);
  const min = clean.length ? Math.min(...clean) : 0;
  const max = clean.length ? Math.max(...clean) : 1;
  const points = values.map((value, index) => {
    const x = values.length <= 1 ? 0 : (index / (values.length - 1)) * width;
    const y = height - (((Number.isFinite(value) ? value : min) - min) / (max - min || 1)) * height;
    return `${x},${y}`;
  }).join(" ");
  return (
    <div className={`rounded-2xl border bg-black/25 p-3 ${stale ? "border-amber-200/30" : "border-white/10"}`}>
      <div className="mb-2 text-[10px] font-black uppercase tracking-[0.16em] text-slate-400">{label}</div>
      <svg viewBox={`0 0 ${width} ${height}`} className="h-11 w-full">
        <polyline fill="none" stroke={accent} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" points={points} />
      </svg>
    </div>
  );
}
