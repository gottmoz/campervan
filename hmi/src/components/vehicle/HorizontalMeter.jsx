import React from "react";
import { motion } from "framer-motion";

export function HorizontalMeter({ label, value, min = 0, max = 100, unit = "%", accent = "#38bdf8", stale = false }) {
  const hasValue = Number.isFinite(value);
  const percent = hasValue ? Math.min(100, Math.max(0, ((value - min) / (max - min || 1)) * 100)) : 0;
  return (
    <div className={`rounded-2xl border bg-white/[0.055] p-3 ${stale ? "border-amber-200/30" : "border-white/10"}`}>
      <div className="mb-2 flex items-center justify-between">
        <div className="text-xs font-black uppercase tracking-[0.16em] text-slate-400">{label}</div>
        <div className={hasValue ? "text-sm font-black text-white" : "text-sm font-black text-slate-500"}>{hasValue ? `${Math.round(value * 10) / 10}${unit}` : "--"}</div>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-white/10">
        <motion.div className="h-full rounded-full" style={{ background: accent }} animate={{ width: `${percent}%` }} transition={{ type: "spring", stiffness: 120, damping: 22 }} />
      </div>
    </div>
  );
}
