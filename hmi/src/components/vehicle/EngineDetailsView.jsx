import React from "react";

function estimateGear(speedKph, rpm) {
  if (!Number.isFinite(speedKph) || !Number.isFinite(rpm) || speedKph < 2 || rpm < 600) return "N/P";
  const ratio = speedKph / rpm;
  if (ratio < 0.015) return "D1";
  if (ratio < 0.024) return "D2";
  if (ratio < 0.034) return "D3";
  if (ratio < 0.045) return "D4";
  if (ratio < 0.058) return "D5";
  if (ratio < 0.075) return "D6";
  return "D?";
}

function health(telemetry, stale, errorCount) {
  const coolant = telemetry.coolantTempC;
  const oil = telemetry.oilTempC;
  const voltage = telemetry.moduleVoltage;
  if (stale || coolant > 105 || oil > 125 || (telemetry.rpm > 600 && voltage < 11.8)) return ["WARNING", "rose"];
  if ((coolant >= 95 && coolant <= 105) || (oil >= 110 && oil <= 125) || errorCount > 5 || (telemetry.rpm > 600 && (voltage < 13.2 || voltage > 15))) return ["CAUTION", "amber"];
  return ["GOOD", "emerald"];
}

export function EngineDetailsView({ snapshot }) {
  const telemetry = snapshot?.telemetry || {};
  const errorCount = Object.values(snapshot?.pidErrorCounts || {}).reduce((sum, value) => sum + value, 0);
  const [healthLabel, tone] = health(telemetry, snapshot?.stale, errorCount);
  const toneClass = tone === "rose" ? "border-rose-300/40 text-rose-100 bg-rose-400/10" : tone === "amber" ? "border-amber-300/40 text-amber-100 bg-amber-400/10" : "border-emerald-300/40 text-emerald-100 bg-emerald-400/10";
  const details = [
    ["Boost / Turbo", telemetry.boostBar, "bar"],
    ["DPF Status", telemetry.dpfSootPercent, "% soot"],
    ["EGT Temperature", telemetry.egtTempC, "°C"],
    ["Engine Load", telemetry.engineLoadPercent ?? telemetry.actualTorquePercent, "%"],
    ["MAF", telemetry.mafGps, "g/s"],
    ["Throttle Position", telemetry.throttlePercent, "%"],
    ["Intake Air Temp", telemetry.intakeTempC, "°C"],
    ["Ambient Temp", telemetry.outsideTempC ?? telemetry.ambientTempC, "°C"],
    ["Module Voltage", telemetry.moduleVoltage, "V"],
    ["Gear", estimateGear(telemetry.speedKph, telemetry.rpm), "estimated"],
  ];
  return (
    <div className="grid h-full grid-cols-[1fr_260px] gap-4">
      <div className="grid grid-cols-2 gap-3">
        {details.map(([label, value, unit]) => {
          const available = value !== null && value !== undefined;
          return (
            <div key={label} className="border border-cyan-300/20 bg-slate-950/75 p-4 shadow-lg shadow-cyan-500/10 [clip-path:polygon(6%_0,100%_0,100%_76%,92%_100%,0_100%,0_18%)]">
              <div className="text-xs font-black uppercase tracking-[0.16em] text-cyan-300">{label}</div>
              <div className={`mt-3 text-3xl font-black ${available ? "text-white" : "text-slate-500"}`}>{available ? value : "--"} <span className="text-sm text-slate-400">{available ? unit : "Not available"}</span></div>
            </div>
          );
        })}
      </div>
      <div className={`border p-5 shadow-xl [clip-path:polygon(10%_0,100%_0,100%_90%,90%_100%,0_100%,0_10%)] ${toneClass}`}>
        <div className="text-xs font-black uppercase tracking-[0.18em] opacity-75">Motor Health</div>
        <div className="mt-3 text-5xl font-black">{healthLabel}</div>
        <div className="mt-5 space-y-3 text-sm">
          <div>Coolant: {Number.isFinite(telemetry.coolantTempC) ? `${telemetry.coolantTempC} °C` : "--"}</div>
          <div>Oil: {Number.isFinite(telemetry.oilTempC) ? `${telemetry.oilTempC} °C` : "Not available"}</div>
          <div>Voltage: {Number.isFinite(telemetry.moduleVoltage) ? `${telemetry.moduleVoltage} V` : "--"}</div>
          <div>PID errors: {errorCount}</div>
          <div>Gear: {estimateGear(telemetry.speedKph, telemetry.rpm)} estimated</div>
        </div>
      </div>
    </div>
  );
}
