import React from "react";
import { motion } from "framer-motion";

export function SemiGauge({ label, value, min = 0, max = 120, unit = "C", warningThreshold = 100, dangerThreshold = 110, stale = false }) {
  const hasValue = Number.isFinite(value);
  const clamped = hasValue ? Math.min(max, Math.max(min, value)) : min;
  const percent = (clamped - min) / (max - min || 1);
  const color = hasValue && value >= dangerThreshold ? "#fb7185" : hasValue && value >= warningThreshold ? "#fbbf24" : "#34d399";
  const dash = 198;
  return (
    <div className={`rounded-3xl border bg-white/[0.055] p-4 ${stale ? "border-amber-200/30" : "border-white/10"}`}>
      <svg viewBox="0 0 160 90" className="h-24 w-full">
        <path d="M24 78 A56 56 0 0 1 136 78" fill="none" stroke="rgba(255,255,255,0.10)" strokeWidth="14" strokeLinecap="round" />
        <motion.path d="M24 78 A56 56 0 0 1 136 78" fill="none" stroke={color} strokeWidth="14" strokeLinecap="round" strokeDasharray={dash} animate={{ strokeDashoffset: dash - percent * dash }} transition={{ type: "spring", stiffness: 90, damping: 22 }} />
      </svg>
      <div className={`-mt-5 text-center text-3xl font-black ${hasValue ? "text-white" : "text-slate-500"}`}>{hasValue ? Math.round(value) : "--"}<span className="text-sm text-slate-400">{unit}</span></div>
      <div className="mt-1 text-center text-xs font-black uppercase tracking-[0.18em] text-slate-300">{label}</div>
    </div>
  );
}
