import React from "react";
import { motion } from "framer-motion";

export function CircularGauge({ label, value, min = 0, max = 100, unit = "", warningThreshold, dangerThreshold, accent = "#22d3ee", stale = false }) {
  const hasValue = Number.isFinite(value);
  const clamped = hasValue ? Math.min(max, Math.max(min, value)) : min;
  const percent = (clamped - min) / (max - min || 1);
  const radius = 52;
  const circumference = 2 * Math.PI * radius;
  const color = hasValue && dangerThreshold != null && value >= dangerThreshold ? "#fb7185" : hasValue && warningThreshold != null && value >= warningThreshold ? "#fbbf24" : accent;
  return (
    <div className={`rounded-3xl border bg-white/[0.055] p-4 ${stale ? "border-amber-200/30" : "border-white/10"}`}>
      <div className="relative mx-auto h-36 w-36">
        <svg className="h-36 w-36 -rotate-90">
          <circle cx="72" cy="72" r={radius} stroke="rgba(255,255,255,0.10)" strokeWidth="12" fill="none" />
          <motion.circle cx="72" cy="72" r={radius} stroke={color} strokeWidth="12" strokeLinecap="round" fill="none" strokeDasharray={circumference} animate={{ strokeDashoffset: circumference - percent * circumference }} transition={{ type: "spring", stiffness: 90, damping: 22 }} />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <div className={`text-3xl font-black ${hasValue ? "text-white" : "text-slate-500"}`}>{hasValue ? Math.round(value) : "--"}</div>
          <div className="text-xs text-slate-400">{unit}</div>
        </div>
      </div>
      <div className="mt-2 text-center text-xs font-black uppercase tracking-[0.18em] text-slate-300">{label}</div>
    </div>
  );
}
