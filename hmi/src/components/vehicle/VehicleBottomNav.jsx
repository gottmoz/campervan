import React from "react";
import { Gauge, SlidersHorizontal, MoreHorizontal, Settings, Route, Cpu } from "lucide-react";

const items = [
  ["dashboard", "Dashboard", Gauge],
  ["engine", "Engine Details", Cpu],
  ["trip", "Trip", Route],
  ["systems", "Systems", Settings],
  ["settings", "Settings", SlidersHorizontal],
  ["more", "More", MoreHorizontal],
];

export function VehicleBottomNav({ active, setActive }) {
  return (
    <div className="grid h-[70px] grid-cols-6 overflow-hidden border border-cyan-300/35 bg-slate-950/85 shadow-xl shadow-cyan-500/15 backdrop-blur-xl [clip-path:polygon(3%_0,97%_0,100%_22%,100%_100%,0_100%,0_22%)]">
      {items.map(([id, label, Icon]) => {
        const selected = active === id;
        return (
          <button key={id} onClick={() => setActive(id)} className={`relative flex flex-col items-center justify-center gap-1 border-r border-cyan-300/10 text-xs font-black uppercase tracking-[0.12em] ${selected ? "text-cyan-200" : "text-slate-400"}`}>
            {selected && <div className="pointer-events-none absolute inset-0 bg-cyan-400/12 shadow-inner shadow-cyan-400/30" />}
            <Icon className="relative" size={24} />
            <span className="relative">{label}</span>
          </button>
        );
      })}
    </div>
  );
}
