import React from "react";

export function VehicleStatusCard({ label, value, sub, tone = "cyan" }) {
  const toneClass = tone === "amber" ? "text-amber-100 border-amber-200/20 bg-amber-300/10" : tone === "rose" ? "text-rose-100 border-rose-200/20 bg-rose-300/10" : "text-cyan-100 border-cyan-200/15 bg-cyan-300/10";
  return (
    <div className={`rounded-2xl border p-3 ${toneClass}`}>
      <div className="text-[10px] font-black uppercase tracking-[0.16em] opacity-70">{label}</div>
      <div className="mt-1 truncate text-sm font-black">{value}</div>
      {sub && <div className="mt-1 truncate text-[11px] opacity-75">{sub}</div>}
    </div>
  );
}
