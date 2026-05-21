import React from "react";
import { motion } from "framer-motion";

export function FuturisticGauge({ label, value, min = 0, max = 100, unit = "", marks = [], redFrom, accent = "#00d5ff", stale = false, formatter }) {
  const hasValue = Number.isFinite(value);
  const clamped = hasValue ? Math.min(max, Math.max(min, value)) : min;
  const percent = (clamped - min) / (max - min || 1);
  const startAngle = 135;
  const sweepAngle = 270;
  const angle = startAngle + percent * sweepAngle;
  const angleRad = angle * Math.PI / 180;
  const needleOuter = 124;
  const needleTail = 18;
  const needleTip = {
    x: 180 + Math.cos(angleRad) * needleOuter,
    y: 180 + Math.sin(angleRad) * needleOuter,
  };
  const needleBase = {
    x: 180 - Math.cos(angleRad) * needleTail,
    y: 180 - Math.sin(angleRad) * needleTail,
  };
  const display = hasValue ? (formatter ? formatter(value) : Math.round(value)) : "--";

  return (
    <div className={`relative h-full min-h-0 overflow-hidden ${stale ? "shadow-amber-500/10" : "shadow-cyan-500/20"}`}>
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(34,211,238,0.12),transparent_42%)]" />
      <svg viewBox="0 0 360 360" className="absolute inset-0 h-full w-full">
        <circle cx="180" cy="180" r="142" fill="none" stroke="rgba(34,211,238,0.18)" strokeWidth="2" />
        <circle cx="180" cy="180" r="118" fill="none" stroke="rgba(34,211,238,0.12)" strokeWidth="1" />
        {marks.map((mark) => {
          const markPercent = (mark - min) / (max - min || 1);
          const markAngle = (startAngle + markPercent * sweepAngle) * Math.PI / 180;
          const major = marks.length <= 7 || mark % 40 === 0 || mark === 0;
          const outer = 154;
          const inner = major ? 135 : 144;
          const x1 = 180 + Math.cos(markAngle) * inner;
          const y1 = 180 + Math.sin(markAngle) * inner;
          const x2 = 180 + Math.cos(markAngle) * outer;
          const y2 = 180 + Math.sin(markAngle) * outer;
          const lx = 180 + Math.cos(markAngle) * 112;
          const ly = 180 + Math.sin(markAngle) * 112;
          const red = redFrom != null && mark >= redFrom;
          return (
            <g key={mark}>
              <line x1={x1} y1={y1} x2={x2} y2={y2} stroke={red ? "#ef4444" : "rgba(224,242,254,0.9)"} strokeWidth={major ? 4 : 2} strokeLinecap="round" />
              {major && <text x={lx} y={ly + 7} textAnchor="middle" fill={red ? "#ef4444" : "rgba(255,255,255,0.9)"} fontSize="24" fontWeight="800">{mark}</text>}
            </g>
          );
        })}
        <motion.line
          x1={needleBase.x}
          y1={needleBase.y}
          x2={needleTip.x}
          y2={needleTip.y}
          stroke={hasValue ? accent : "rgba(148,163,184,0.35)"}
          strokeWidth="6"
          strokeLinecap="round"
          animate={{
            x1: needleBase.x,
            y1: needleBase.y,
            x2: needleTip.x,
            y2: needleTip.y,
          }}
          transition={{ type: "spring", stiffness: 70, damping: 20 }}
          style={{ filter: "drop-shadow(0 0 12px rgba(56,189,248,0.9))" }}
        />
        <circle cx="180" cy="180" r="16" fill="#020617" stroke={accent} strokeWidth="2" />
      </svg>
      <div className="absolute inset-x-0 bottom-3 text-center">
        <div className={`text-[2.7rem] font-black leading-none tracking-tight ${hasValue ? "text-white" : "text-slate-500"}`}>{display}</div>
        <div className="mt-0 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-300">{unit}</div>
      </div>
      <div className="absolute inset-x-0 top-[55%] text-center">
        <div className="text-sm font-black uppercase tracking-[0.14em] text-cyan-200">{label}</div>
      </div>
    </div>
  );
}
