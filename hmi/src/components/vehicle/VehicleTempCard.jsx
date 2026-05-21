import React from "react";
import { motion } from "framer-motion";

export function VehicleTempCard({ label, value, min = 40, max = 120, warning = 100, unit = "°C", icon, subtext, stale = false, simulated = false }) {
  const hasValue = Number.isFinite(value);
  const percent = hasValue ? Math.min(100, Math.max(0, ((value - min) / (max - min || 1)) * 100)) : 0;
  const hot = hasValue && value >= warning;
  return (
    <div className={`relative overflow-hidden border bg-slate-950/80 p-4 backdrop-blur-xl [clip-path:polygon(8%_0,92%_0,100%_18%,100%_82%,92%_100%,8%_100%,0_82%,0_18%)] ${hot ? "border-red-400/45 shadow-red-500/15" : stale ? "border-amber-300/35 shadow-amber-500/10" : "border-cyan-300/40 shadow-cyan-500/20"} shadow-xl`}>
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(135deg,rgba(34,211,238,0.10),transparent_45%)]" />
      <div className="relative flex items-center justify-between">
        <div className="text-sm font-black uppercase tracking-[0.16em] text-cyan-200">{label}</div>
        <div className="text-3xl text-cyan-200">{icon}</div>
      </div>
      <div className={`relative mt-2 text-center text-5xl font-black ${hasValue ? "text-white" : "text-slate-500"}`}>{hasValue ? Math.round(value) : "--"} <span className="text-2xl text-slate-200">{unit}</span></div>
      <div className="relative mt-3 h-3 overflow-hidden rounded-full bg-white/10">
        <motion.div className={`h-full rounded-full ${hot ? "bg-gradient-to-r from-cyan-400 via-amber-300 to-red-500" : "bg-gradient-to-r from-sky-500 to-cyan-300"}`} animate={{ width: `${percent}%` }} transition={{ type: "spring", stiffness: 120, damping: 20 }} />
      </div>
      <div className="relative mt-2 flex justify-between text-[10px] font-bold text-slate-400"><span>{min}</span><span>{Math.round((min + max) / 2)}</span><span>{max}</span></div>
      {(subtext || simulated) && <div className="relative mt-1 text-center text-[10px] font-bold uppercase tracking-[0.12em] text-slate-500">{simulated ? "simulated" : subtext}</div>}
    </div>
  );
}
