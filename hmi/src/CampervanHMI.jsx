import React, { useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  BatteryCharging,
  Bluetooth,
  CalendarDays,
  CarFront,
  ChevronRight,
  Droplets,
  Fan,
  Flame,
  Gauge,
  Home,
  LampCeiling,
  Lock,
  MapPinned,
  Moon,
  Music,
  PlugZap,
  Power,
  Radio,
  Refrigerator,
  Settings,
  ShieldCheck,
  ShowerHead,
  Snowflake,
  SolarPanel,
  Sun,
  Thermometer,
  ToggleLeft,
  ToggleRight,
  Wifi,
  Wind,
  Wrench,
  Zap,
  X,
} from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

// Clean Ford Transit campervan background only. Do not use a full GUI mockup here.
const VAN_BACKGROUND_URL = "assets/ford-transit-clean-bg.png";

const tabs = [
  { id: "home", label: "Home", icon: Home },
  { id: "power", label: "Power", icon: Zap },
  { id: "climate", label: "Climate", icon: Thermometer },
  { id: "water", label: "Water", icon: Droplets },
  { id: "lights", label: "Lights", icon: LampCeiling },
  { id: "systems", label: "Systems", icon: Settings },
];

const scenes = [
  { id: "day", label: "Day", icon: Sun },
  { id: "night", label: "Night", icon: Moon },
  { id: "camp", label: "Camp", icon: MapPinned },
  { id: "away", label: "Away", icon: Lock },
];

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

function GlassCard({ children, className = "" }) {
  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 10, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ type: "spring", stiffness: 260, damping: 24 }}
      className={cx(
        "rounded-3xl border border-white/10 bg-white/[0.075] shadow-2xl shadow-black/20 backdrop-blur-xl",
        className
      )}
    >
      {children}
    </motion.div>
  );
}

function MiniStatus({ icon: Icon, label, value, sub, alert = false }) {
  return (
    <GlassCard className="px-4 py-3">
      <div className="flex items-center gap-3">
        <div className={cx("rounded-2xl p-2", alert ? "bg-rose-500/20 text-rose-200" : "bg-cyan-400/10 text-cyan-200")}>
          <Icon size={20} />
        </div>
        <div className="min-w-0">
          <div className="text-[11px] uppercase tracking-[0.22em] text-slate-400">{label}</div>
          <div className="truncate text-lg font-semibold text-white">{value}</div>
          {sub && <div className="text-xs text-slate-400">{sub}</div>}
        </div>
      </div>
    </GlassCard>
  );
}

function ToggleRow({ icon: Icon, label, sub, on, setOn }) {
  return (
    <button
      onClick={() => setOn(!on)}
      className="group flex w-full items-center justify-between rounded-2xl border border-white/10 bg-white/[0.055] px-4 py-3 text-left transition hover:bg-white/[0.09]"
    >
      <div className="flex items-center gap-3">
        <div className={cx("rounded-2xl p-2 transition", on ? "bg-cyan-400/20 text-cyan-100" : "bg-slate-700/60 text-slate-400")}>
          <Icon size={20} />
        </div>
        <div>
          <div className="text-sm font-semibold text-white">{label}</div>
          <div className="text-xs text-slate-400">{sub}</div>
        </div>
      </div>
      <motion.div animate={{ rotate: on ? 0 : 180 }} className={on ? "text-cyan-200" : "text-slate-500"}>
        {on ? <ToggleRight size={34} /> : <ToggleLeft size={34} />}
      </motion.div>
    </button>
  );
}

function RangeControl({ label, value, setValue, min = 0, max = 100, unit = "%", icon: Icon }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/[0.055] p-4">
      <div className="mb-3 flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm font-semibold text-white">
          {Icon && <Icon size={18} className="text-cyan-200" />}
          {label}
        </div>
        <div className="rounded-full bg-cyan-400/10 px-3 py-1 text-sm font-bold text-cyan-100">
          {value}{unit}
        </div>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        value={value}
        onChange={(e) => setValue(Number(e.target.value))}
        className="h-2 w-full cursor-pointer accent-cyan-300"
      />
    </div>
  );
}

function CircularGauge({ value, label, unit, icon: Icon }) {
  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (value / 100) * circumference;

  return (
    <div className="flex items-center gap-4">
      <div className="relative h-36 w-36">
        <svg className="h-36 w-36 -rotate-90">
          <circle cx="72" cy="72" r={radius} stroke="rgba(255,255,255,0.10)" strokeWidth="12" fill="none" />
          <motion.circle
            cx="72"
            cy="72"
            r={radius}
            stroke="url(#gaugeGradient)"
            strokeWidth="12"
            strokeLinecap="round"
            fill="none"
            strokeDasharray={circumference}
            animate={{ strokeDashoffset: offset }}
            transition={{ type: "spring", stiffness: 80, damping: 18 }}
          />
          <defs>
            <linearGradient id="gaugeGradient" x1="0" x2="1" y1="0" y2="1">
              <stop offset="0%" stopColor="#67e8f9" />
              <stop offset="100%" stopColor="#a78bfa" />
            </linearGradient>
          </defs>
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          {Icon && <Icon size={20} className="mb-1 text-cyan-200" />}
          <div className="text-3xl font-black text-white">{value}</div>
          <div className="text-xs text-slate-400">{unit}</div>
        </div>
      </div>
      <div>
        <div className="text-[11px] uppercase tracking-[0.26em] text-slate-400">{label}</div>
        <div className="mt-1 text-xl font-bold text-white">Healthy</div>
        <div className="mt-2 max-w-[170px] text-xs leading-5 text-slate-400">
          Smart charging active. Solar input and load are balanced for camping mode.
        </div>
      </div>
    </div>
  );
}

function TopBar({ activeScene, setActiveScene }) {
  const time = "21:42";
  return (
    <div className="flex h-[74px] items-center justify-between px-5">
      <div className="flex items-center gap-4">
        <motion.div
          animate={{ boxShadow: ["0 0 0 rgba(34,211,238,0)", "0 0 28px rgba(34,211,238,0.28)", "0 0 0 rgba(34,211,238,0)"] }}
          transition={{ duration: 3, repeat: Infinity }}
          className="flex h-12 w-12 items-center justify-center rounded-2xl bg-cyan-400/15 text-cyan-100"
        >
          <CarFront size={28} />
        </motion.div>
        <div>
          <div className="text-2xl font-black tracking-tight text-white">Familjen Blixts Camper</div>
          <div className="flex items-center gap-3 text-xs text-slate-400">
            <span className="flex items-center gap-1"><Wifi size={13} /> Online</span>
            <span className="flex items-center gap-1"><Bluetooth size={13} /> BT</span>
            <span className="flex items-center gap-1"><CalendarDays size={13} /> Wed 20 May</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2 rounded-full border border-white/10 bg-black/20 p-1">
        {scenes.map((scene) => {
          const Icon = scene.icon;
          const active = activeScene === scene.id;
          return (
            <button
              key={scene.id}
              onClick={() => setActiveScene(scene.id)}
              className={cx(
                "relative flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition",
                active ? "text-slate-950" : "text-slate-300 hover:text-white"
              )}
            >
              {active && (
                <motion.div layoutId="scenePill" className="absolute inset-0 rounded-full bg-cyan-200" transition={{ type: "spring", stiffness: 400, damping: 34 }} />
              )}
              <span className="relative flex items-center gap-2"><Icon size={16} /> {scene.label}</span>
            </button>
          );
        })}
      </div>

      <div className="text-right">
        <div className="text-3xl font-black text-white">{time}</div>
        <div className="text-xs text-slate-400">Interior 22.5°C</div>
      </div>
    </div>
  );
}

function SideNav({ activeTab, setActiveTab }) {
  return (
    <div className="flex h-full w-[104px] flex-col items-center gap-2 border-r border-white/10 bg-black/20 py-4">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const active = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={cx("relative flex h-[72px] w-[82px] flex-col items-center justify-center gap-1 rounded-3xl transition", active ? "text-cyan-100" : "text-slate-500 hover:bg-white/5 hover:text-slate-200")}
          >
            {active && <motion.div layoutId="navActive" className="absolute inset-0 rounded-3xl bg-cyan-400/15 ring-1 ring-cyan-300/20" />}
            <Icon size={24} className="relative" />
            <span className="relative text-[11px] font-bold uppercase tracking-wide">{tab.label}</span>
          </button>
        );
      })}
      <div className="mt-auto flex flex-col items-center gap-2">
        <button className="flex h-12 w-12 items-center justify-center rounded-2xl bg-rose-500/15 text-rose-200 ring-1 ring-rose-300/20">
          <Power size={23} />
        </button>
      </div>
    </div>
  );
}

function HomeView({ state, setters, openEnergyStats }) {
  return (
    <div className="grid h-full grid-cols-[1.15fr_0.85fr] gap-4">
      <div className="grid grid-rows-[auto_1fr] gap-4">
        <div className="grid grid-cols-4 gap-3">
          <MiniStatus icon={BatteryCharging} label="Battery" value={`${state.battery}%`} sub="13.4V / 18A" />
          <button onClick={openEnergyStats} className="text-left transition hover:scale-[1.02] active:scale-[0.99]">
            <MiniStatus icon={SolarPanel} label="Solar" value="326 W" sub="Tap for daily graph" />
          </button>
          <MiniStatus icon={Droplets} label="Fresh" value={`${state.freshWater}%`} sub="82 L left" />
          <MiniStatus icon={ShieldCheck} label="Security" value="Armed" sub="All doors OK" />
        </div>

        <GlassCard className="relative overflow-hidden p-5">
          <div className="absolute inset-0 opacity-40">
            <motion.div
              animate={{ x: [0, 80, 0], y: [0, -40, 0] }}
              transition={{ duration: 9, repeat: Infinity, ease: "easeInOut" }}
              className="absolute left-16 top-8 h-48 w-48 rounded-full bg-cyan-400/20 blur-3xl"
            />
            <motion.div
              animate={{ x: [0, -70, 0], y: [0, 30, 0] }}
              transition={{ duration: 11, repeat: Infinity, ease: "easeInOut" }}
              className="absolute right-10 bottom-5 h-56 w-56 rounded-full bg-violet-500/20 blur-3xl"
            />
          </div>
          <div className="relative flex h-full items-center justify-between">
            <div>
              <div className="text-[11px] uppercase tracking-[0.3em] text-cyan-200">Camper status</div>
              <div className="mt-2 text-5xl font-black tracking-tight text-white">Ready for camp</div>
              <div className="mt-3 max-w-[460px] text-sm leading-6 text-slate-300">
                Heating is stable, auxiliary battery is charging, fridge is cold, and grey water has capacity. One-tap scenes can prepare the van for driving, sleeping, camping, or leaving unattended.
              </div>
              <div className="mt-5 grid w-[520px] grid-cols-2 gap-3">
                <ToggleRow icon={Flame} label="Diesel heater" sub="Eco mode / 1.2 kW" on={state.heater} setOn={setters.setHeater} />
                <ToggleRow icon={Refrigerator} label="Fridge" sub="4°C target" on={state.fridge} setOn={setters.setFridge} />
                <ToggleRow icon={PlugZap} label="Inverter" sub="230V outlet" on={state.inverter} setOn={setters.setInverter} />
                <ToggleRow icon={Fan} label="Roof fan" sub="Auto humidity" on={state.fan} setOn={setters.setFan} />
              </div>
            </div>
            <motion.div
              animate={{ y: [0, -8, 0] }}
              transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
              className="relative mr-4 h-[260px] w-[260px]"
            >
              <div className="absolute inset-0 rounded-[3rem] bg-gradient-to-br from-cyan-300/20 to-violet-400/20 blur-xl" />
              <div className="absolute inset-5 rounded-[2.5rem] border border-white/10 bg-black/20 p-5">
                <div className="h-full rounded-[2rem] border border-cyan-200/20 bg-slate-950/60 p-4">
                  <div className="mb-3 flex items-center justify-between text-xs text-slate-400"><span>Layout</span><span>LIVE</span></div>
                  <div className="grid h-[175px] grid-cols-2 gap-3">
                    <div className="rounded-2xl bg-cyan-300/15 p-2 text-xs text-cyan-100">Bed</div>
                    <div className="rounded-2xl bg-white/10 p-2 text-xs text-slate-200">Kitchen</div>
                    <div className="rounded-2xl bg-white/10 p-2 text-xs text-slate-200">Garage</div>
                    <div className="rounded-2xl bg-cyan-300/15 p-2 text-xs text-cyan-100">Lounge</div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        </GlassCard>
      </div>

      <div className="grid grid-rows-[1fr_auto] gap-4">
        <GlassCard className="p-5">
          <CircularGauge value={state.battery} label="Aux battery" unit="% SOC" icon={BatteryCharging} />
          <div className="mt-5 grid grid-cols-2 gap-3">
            <MiniStatus icon={Gauge} label="Range" value="2.8 days" sub="At current load" />
            <MiniStatus icon={PlugZap} label="Load" value="184 W" sub="12V + 230V" />
          </div>
        </GlassCard>
        <GlassCard className="p-4">
          <div className="mb-3 flex items-center justify-between">
            <div>
              <div className="text-sm font-bold text-white">Media & Navigation</div>
              <div className="text-xs text-slate-400">Offline maps / cabin audio</div>
            </div>
            <Radio className="text-cyan-200" size={22} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <button onClick={openEnergyStats} className="rounded-2xl bg-cyan-300/10 p-4 text-left text-sm font-semibold text-white ring-1 ring-cyan-200/20 hover:bg-cyan-300/15"><SolarPanel className="mb-2 text-cyan-200" /> Solar Graph</button>
            <button className="rounded-2xl bg-white/[0.07] p-4 text-left text-sm font-semibold text-white hover:bg-white/[0.1]"><Music className="mb-2 text-cyan-200" /> Audio</button>
            <button className="rounded-2xl bg-white/[0.07] p-4 text-left text-sm font-semibold text-white hover:bg-white/[0.1]"><MapPinned className="mb-2 text-cyan-200" /> Map</button>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}

function PowerView({ state, setters, openEnergyStats }) {
  return (
    <div className="grid h-full grid-cols-[0.9fr_1.1fr] gap-4">
      <GlassCard className="p-5">
        <CircularGauge value={state.battery} label="Battery bank" unit="%" icon={BatteryCharging} />
        <div className="mt-5 space-y-3">
          <RangeControl label="Charge limit" value={setters.chargeLimit ?? 90} setValue={setters.setChargeLimit} min={50} max={100} unit="%" icon={BatteryCharging} />
          <ToggleRow icon={PlugZap} label="230V Inverter" sub="Pure sine / outlet group A" on={state.inverter} setOn={setters.setInverter} />
          <ToggleRow icon={SolarPanel} label="Solar priority" sub="Prefer solar before alternator" on={state.solarPriority} setOn={setters.setSolarPriority} />
        </div>
      </GlassCard>
      <div className="grid grid-rows-[auto_1fr] gap-4">
        <div className="grid grid-cols-4 gap-3">
          <MiniStatus icon={SolarPanel} label="PV Input" value="326 W" sub="24.8V" />
          <MiniStatus icon={CarFront} label="DC-DC" value="0 W" sub="Engine off" />
          <MiniStatus icon={PlugZap} label="Shore" value="No" sub="Disconnected" />
          <MiniStatus icon={Zap} label="Output" value="184 W" sub="Stable" />
        </div>
        <GlassCard className="p-5">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <div className="text-xl font-black text-white">Energy flow</div>
              <div className="text-sm text-slate-400">Animated system diagram for solar, charger, battery and loads</div>
            </div>
            <button className="rounded-full bg-cyan-300 px-4 py-2 text-sm font-black text-slate-950">Optimize</button>
          </div>
          <div className="relative h-[292px] rounded-3xl border border-white/10 bg-black/20 p-5">
            <button onClick={openEnergyStats} className="absolute left-8 top-10 z-20 text-left transition hover:scale-[1.03] active:scale-[0.99]">
              <FlowNode className="relative left-auto top-auto" icon={SolarPanel} label="Solar" value="326 W / stats" />
            </button>
            <FlowNode className="left-8 bottom-10" icon={PlugZap} label="Shore" value="0 W" muted />
            <FlowNode className="left-[310px] top-[102px]" icon={BatteryCharging} label="LiFePO₄" value={`${state.battery}%`} primary />
            <FlowNode className="right-8 top-10" icon={Refrigerator} label="Fridge" value="42 W" />
            <FlowNode className="right-8 bottom-10" icon={LampCeiling} label="Cabin" value="38 W" />
            <AnimatedLine x1="170px" y1="72px" x2="310px" y2="137px" />
            <AnimatedLine x1="170px" y1="222px" x2="310px" y2="155px" muted />
            <AnimatedLine x1="470px" y1="137px" x2="625px" y2="72px" />
            <AnimatedLine x1="470px" y1="155px" x2="625px" y2="222px" />
          </div>
        </GlassCard>
      </div>
    </div>
  );
}

function FlowNode({ icon: Icon, label, value, className, primary, muted }) {
  return (
    <div className={cx("absolute z-10 w-[150px] rounded-3xl border p-4", primary ? "border-cyan-200/30 bg-cyan-300/15" : muted ? "border-white/10 bg-white/[0.035] opacity-60" : "border-white/10 bg-white/[0.07]", className)}>
      <Icon className={primary ? "text-cyan-100" : "text-cyan-200"} size={24} />
      <div className="mt-2 text-sm font-bold text-white">{label}</div>
      <div className="text-xs text-slate-400">{value}</div>
    </div>
  );
}

function AnimatedLine({ x1, y1, x2, y2, muted }) {
  return (
    <svg className="absolute inset-0 h-full w-full">
      <motion.line
        x1={x1}
        y1={y1}
        x2={x2}
        y2={y2}
        stroke={muted ? "rgba(148,163,184,0.25)" : "rgba(103,232,249,0.62)"}
        strokeWidth="4"
        strokeLinecap="round"
        strokeDasharray="10 12"
        animate={{ strokeDashoffset: [0, -44] }}
        transition={{ duration: 1.2, repeat: Infinity, ease: "linear" }}
      />
    </svg>
  );
}

function ClimateView({ state, setters }) {
  return (
    <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
      <GlassCard className="p-5">
        <div className="flex items-start justify-between">
          <div>
            <div className="text-[11px] uppercase tracking-[0.3em] text-slate-400">Cabin climate</div>
            <div className="mt-2 text-5xl font-black text-white">{state.temp.toFixed(1)}°C</div>
            <div className="mt-1 text-sm text-slate-400">Target temperature / auto comfort</div>
          </div>
          <motion.div animate={{ rotate: state.fan ? 360 : 0 }} transition={{ duration: 2, repeat: state.fan ? Infinity : 0, ease: "linear" }} className="rounded-3xl bg-cyan-300/15 p-5 text-cyan-100">
            <Fan size={42} />
          </motion.div>
        </div>
        <div className="mt-8 space-y-4">
          <RangeControl label="Temperature" value={state.temp} setValue={setters.setTemp} min={5} max={30} unit="°C" icon={Thermometer} />
          <RangeControl label="Ventilation" value={state.vent} setValue={setters.setVent} min={0} max={100} unit="%" icon={Wind} />
          <ToggleRow icon={Flame} label="Diesel heater" sub="Silent night mode available" on={state.heater} setOn={setters.setHeater} />
        </div>
      </GlassCard>
      <div className="grid grid-rows-[auto_1fr] gap-4">
        <div className="grid grid-cols-3 gap-3">
          <MiniStatus icon={Droplets} label="Humidity" value="48%" sub="Normal" />
          <MiniStatus icon={Snowflake} label="Fridge" value="4°C" sub="Eco" />
          <MiniStatus icon={Wind} label="CO₂" value="612" sub="ppm" />
        </div>
        <GlassCard className="p-5">
          <div className="text-xl font-black text-white">Climate presets</div>
          <div className="mt-4 grid grid-cols-2 gap-3">
            <PresetButton icon={Sun} label="Morning warm-up" sub="Heat to 21°C" />
            <PresetButton icon={Moon} label="Sleep mode" sub="19°C / silent fan" />
            <PresetButton icon={Wind} label="Air refresh" sub="Fan 100% / 10 min" />
            <PresetButton icon={Snowflake} label="Food safe" sub="Fridge boost" />
          </div>
          <div className="mt-5 rounded-3xl border border-white/10 bg-black/20 p-4">
            <div className="mb-2 text-sm font-bold text-white">Safety logic</div>
            <div className="text-sm leading-6 text-slate-400">Auto shutoff on low battery, high CO₂ alert, frost protection, and heater lockout when fuel is low.</div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}

function PresetButton({ icon: Icon, label, sub }) {
  return (
    <button className="rounded-3xl border border-white/10 bg-white/[0.055] p-4 text-left transition hover:bg-cyan-300/10 hover:ring-1 hover:ring-cyan-200/20">
      <Icon size={25} className="mb-4 text-cyan-200" />
      <div className="text-sm font-bold text-white">{label}</div>
      <div className="text-xs text-slate-400">{sub}</div>
    </button>
  );
}

function WaterView({ state, setters }) {
  return (
    <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
      <GlassCard className="p-5">
        <div className="grid grid-cols-2 gap-5">
          <Tank label="Fresh water" value={state.freshWater} amount="82 L" />
          <Tank label="Grey water" value={state.greyWater} amount="34 L" grey />
        </div>
        <div className="mt-5 space-y-3">
          <ToggleRow icon={ShowerHead} label="Water pump" sub="Pressure mode" on={state.pump} setOn={setters.setPump} />
          <ToggleRow icon={Flame} label="Boiler" sub="Hot water 54°C" on={state.boiler} setOn={setters.setBoiler} />
          <RangeControl label="Boiler target" value={state.boilerTemp} setValue={setters.setBoilerTemp} min={35} max={70} unit="°C" icon={Thermometer} />
        </div>
      </GlassCard>
      <GlassCard className="p-5">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <div className="text-xl font-black text-white">Water controls</div>
            <div className="text-sm text-slate-400">Pump, tank heaters, dump valve and frost protection</div>
          </div>
          <Droplets className="text-cyan-200" size={30} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <PresetButton icon={ShowerHead} label="Shower ready" sub="Pump + boiler" />
          <PresetButton icon={Snowflake} label="Frost guard" sub="Tank heater auto" />
          <PresetButton icon={Droplets} label="Fill mode" sub="Pump disabled" />
          <PresetButton icon={Wrench} label="Service" sub="Drain system" />
        </div>
        <div className="mt-5 rounded-3xl bg-amber-300/10 p-4 text-sm text-amber-100 ring-1 ring-amber-200/20">
          Grey tank warning at 80%. Auto-disable shower pump at 95% grey level.
        </div>
      </GlassCard>
    </div>
  );
}

function Tank({ label, value, amount, grey }) {
  return (
    <div className="rounded-3xl border border-white/10 bg-black/20 p-4">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <div className="text-sm font-bold text-white">{label}</div>
          <div className="text-xs text-slate-400">{amount}</div>
        </div>
        <div className="text-2xl font-black text-white">{value}%</div>
      </div>
      <div className="relative h-[245px] overflow-hidden rounded-[2rem] border border-white/10 bg-slate-950">
        <motion.div
          animate={{ height: `${value}%` }}
          transition={{ type: "spring", stiffness: 80, damping: 18 }}
          className={cx("absolute bottom-0 left-0 right-0", grey ? "bg-slate-500/70" : "bg-cyan-300/70")}
        >
          <motion.div
            animate={{ x: [0, -90, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -top-5 left-0 h-10 w-[160%] rounded-[50%] bg-white/25"
          />
        </motion.div>
      </div>
    </div>
  );
}

function LightsView({ state, setters }) {
  return (
    <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
      <GlassCard className="p-5">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <div className="text-xl font-black text-white">Lighting zones</div>
            <div className="text-sm text-slate-400">Dimmable LED groups with scene memory</div>
          </div>
          <LampCeiling className="text-cyan-200" size={30} />
        </div>
        <div className="space-y-4">
          <RangeControl label="Main cabin" value={state.lightMain} setValue={setters.setLightMain} min={0} max={100} unit="%" icon={LampCeiling} />
          <RangeControl label="Kitchen" value={state.lightKitchen} setValue={setters.setLightKitchen} min={0} max={100} unit="%" icon={LampCeiling} />
          <RangeControl label="Bed" value={state.lightBed} setValue={setters.setLightBed} min={0} max={100} unit="%" icon={Moon} />
          <RangeControl label="Awning" value={state.lightAwning} setValue={setters.setLightAwning} min={0} max={100} unit="%" icon={Sun} />
        </div>
      </GlassCard>
      <GlassCard className="relative overflow-hidden p-5">
        <motion.div
          animate={{ opacity: [0.25, 0.5, 0.25], scale: [1, 1.08, 1] }}
          transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
          className="absolute right-4 top-4 h-56 w-56 rounded-full bg-amber-200/20 blur-3xl"
        />
        <div className="relative">
          <div className="text-xl font-black text-white">Scenes</div>
          <div className="mt-4 grid grid-cols-2 gap-3">
            <PresetButton icon={Sun} label="Bright" sub="All zones 100%" />
            <PresetButton icon={Moon} label="Night path" sub="Low floor LEDs" />
            <PresetButton icon={Music} label="Lounge" sub="Warm indirect" />
            <PresetButton icon={Lock} label="Security" sub="Awning motion" />
          </div>
          <div className="mt-6 rounded-3xl border border-white/10 bg-black/20 p-4">
            <div className="mb-3 text-sm font-bold text-white">Color temperature</div>
            <div className="grid grid-cols-4 gap-2">
              {['2700K','3200K','4000K','6500K'].map((k) => <button key={k} className="rounded-2xl bg-white/[0.07] py-3 text-sm font-bold text-white hover:bg-cyan-300/10">{k}</button>)}
            </div>
          </div>
        </div>
      </GlassCard>
    </div>
  );
}

function EnergyStatsModal({ onClose }) {
  const chargeData = [
    { day: "Mon", solar: 2.8, landline: 0.0, carCharge: 0.4, battery: 74 },
    { day: "Tue", solar: 3.4, landline: 0.0, carCharge: 0.2, battery: 81 },
    { day: "Wed", solar: 1.9, landline: 1.6, carCharge: 0.0, battery: 87 },
    { day: "Thu", solar: 4.2, landline: 0.0, carCharge: 0.6, battery: 94 },
    { day: "Fri", solar: 2.6, landline: 2.1, carCharge: 0.0, battery: 96 },
    { day: "Sat", solar: 5.1, landline: 0.0, carCharge: 0.3, battery: 100 },
    { day: "Sun", solar: 3.7, landline: 0.8, carCharge: 0.6, battery: 92 },
  ];

  const totals = chargeData.reduce(
    (acc, item) => ({
      solar: acc.solar + item.solar,
      landline: acc.landline + item.landline,
      carCharge: acc.carCharge + item.carCharge,
      total: acc.total + item.solar + item.landline + item.carCharge,
    }),
    { solar: 0, landline: 0, carCharge: 0, total: 0 }
  );

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="absolute inset-0 z-50 flex items-center justify-center bg-black/60 p-6 backdrop-blur-md"
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, y: 28, scale: 0.96 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        exit={{ opacity: 0, y: 18, scale: 0.97 }}
        transition={{ type: "spring", stiffness: 260, damping: 24 }}
        onClick={(event) => event.stopPropagation()}
        className="h-[520px] w-[900px] overflow-hidden rounded-[2rem] border border-white/10 bg-slate-950/95 shadow-2xl shadow-black"
      >
        <div className="flex items-center justify-between border-b border-white/10 px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-cyan-300/15 p-3 text-cyan-100"><SolarPanel size={26} /></div>
            <div>
              <div className="text-2xl font-black text-white">Daily Charge Statistics</div>
              <div className="text-sm text-slate-400">Solar vs landline / shore charging, last 7 days</div>
            </div>
          </div>
          <button onClick={onClose} className="rounded-2xl bg-white/[0.07] p-3 text-slate-300 transition hover:bg-white/[0.12] hover:text-white">
            <X size={22} />
          </button>
        </div>

        <div className="grid h-[448px] grid-cols-[1fr_280px] gap-4 p-5">
          <div className="grid grid-rows-[1fr_1fr] gap-4">
            <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">
              <div className="mb-3 flex items-center justify-between">
                <div>
                  <div className="text-sm font-bold text-white">Charge per day</div>
                  <div className="text-xs text-slate-400">kWh generated from solar, landline and car charge</div>
                </div>
                <div className="rounded-full bg-cyan-300/10 px-3 py-1 text-xs font-bold text-cyan-100">kWh</div>
              </div>
              <ResponsiveContainer width="100%" height={150}>
                <BarChart data={chargeData} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
                  <CartesianGrid stroke="rgba(255,255,255,0.08)" vertical={false} />
                  <XAxis dataKey="day" stroke="rgba(226,232,240,0.55)" tickLine={false} axisLine={false} />
                  <YAxis stroke="rgba(226,232,240,0.55)" tickLine={false} axisLine={false} />
                  <Tooltip contentStyle={{ background: "#020617", border: "1px solid rgba(255,255,255,0.12)", borderRadius: 16, color: "#fff" }} />
                  <Legend />
                  <Bar dataKey="solar" name="Solar" fill="#22d3ee" radius={[8, 8, 0, 0]} />
                  <Bar dataKey="landline" name="Landline" fill="#a78bfa" radius={[8, 8, 0, 0]} />
                  <Bar dataKey="carCharge" name="Car Charge" fill="#fbbf24" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">
              <div className="mb-3 flex items-center justify-between">
                <div>
                  <div className="text-sm font-bold text-white">Battery state of charge</div>
                  <div className="text-xs text-slate-400">End-of-day SOC trend</div>
                </div>
                <BatteryCharging className="text-cyan-200" size={20} />
              </div>
              <ResponsiveContainer width="100%" height={150}>
                <LineChart data={chargeData} margin={{ top: 4, right: 8, left: -18, bottom: 0 }}>
                  <CartesianGrid stroke="rgba(255,255,255,0.08)" vertical={false} />
                  <XAxis dataKey="day" stroke="rgba(226,232,240,0.55)" tickLine={false} axisLine={false} />
                  <YAxis stroke="rgba(226,232,240,0.55)" tickLine={false} axisLine={false} domain={[50, 100]} />
                  <Tooltip contentStyle={{ background: "#020617", border: "1px solid rgba(255,255,255,0.12)", borderRadius: 16, color: "#fff" }} />
                  <Line type="monotone" dataKey="battery" name="Battery %" stroke="#67e8f9" strokeWidth={4} dot={{ r: 4 }} activeDot={{ r: 7 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="grid grid-rows-[auto_1fr] gap-4">
            <div className="grid grid-cols-2 gap-3">
            <div className="rounded-3xl border border-cyan-200/20 bg-cyan-300/10 p-4">
              <div className="flex items-center gap-3 text-cyan-100"><SolarPanel size={24} /><span className="text-sm font-bold uppercase tracking-[0.18em]">Solar total</span></div>
              <div className="mt-3 text-3xl font-black text-white">{totals.solar.toFixed(1)}</div>
              <div className="text-sm text-slate-300">kWh this week</div>
            </div>
            <div className="rounded-3xl border border-violet-200/20 bg-violet-300/10 p-4">
              <div className="flex items-center gap-3 text-violet-100"><PlugZap size={24} /><span className="text-sm font-bold uppercase tracking-[0.18em]">Landline</span></div>
              <div className="mt-3 text-3xl font-black text-white">{totals.landline.toFixed(1)}</div>
              <div className="text-sm text-slate-300">kWh from shore power</div>
            </div>
            <div className="rounded-3xl border border-amber-200/20 bg-amber-300/10 p-4">
              <div className="flex items-center gap-3 text-amber-100"><CarFront size={24} /><span className="text-sm font-bold uppercase tracking-[0.18em]">Car charge</span></div>
              <div className="mt-3 text-3xl font-black text-white">{totals.carCharge.toFixed(1)}</div>
              <div className="text-sm text-slate-300">kWh from alternator</div>
            </div>
            <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">
                <div className="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Total charging</div>
                <div className="mt-3 text-3xl font-black text-white">{totals.total.toFixed(1)}</div>
                <div className="text-sm text-slate-300">kWh combined</div>
              </div>
            </div>
            <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-5">
              <div className="text-sm font-bold text-white">Smart insight</div>
              <div className="mt-2 text-sm leading-6 text-slate-400">
                Solar covered most charging this week. Landline was only needed on low-sun days and can be limited automatically when battery SOC is above 90%.
              </div>
              <button className="mt-4 w-full rounded-2xl bg-cyan-300 px-4 py-3 text-sm font-black text-slate-950">Create charging rule</button>
            </div>
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
}

function SystemsView({ state, setters }) {
  return (
    <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
      <GlassCard className="p-5">
        <div className="text-xl font-black text-white">Vehicle & security</div>
        <div className="mt-4 space-y-3">
          <ToggleRow icon={Lock} label="Door locks" sub="Cabin + rear + side" on={state.locks} setOn={setters.setLocks} />
          <ToggleRow icon={ShieldCheck} label="Alarm" sub="PIR + door sensors" on={state.alarm} setOn={setters.setAlarm} />
          <ToggleRow icon={CarFront} label="Drive mode interlock" sub="Disable pump/inverter while driving" on={state.interlock} setOn={setters.setInterlock} />
          <ToggleRow icon={Wifi} label="Remote access" sub="MQTT cloud bridge" on={state.remote} setOn={setters.setRemote} />
        </div>
        <div className="mt-5 rounded-3xl bg-emerald-300/10 p-4 text-sm text-emerald-100 ring-1 ring-emerald-200/20">
          Diagnostics OK: 23 nodes online, 0 critical faults, last sync 18 seconds ago.
        </div>
      </GlassCard>
      <GlassCard className="p-5">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <div className="text-xl font-black text-white">Settings menu</div>
            <div className="text-sm text-slate-400">Configuration pages for installation and service</div>
          </div>
          <Settings className="text-cyan-200" size={30} />
        </div>
        <div className="space-y-2">
          {[
            ["Network", "Wi‑Fi, Bluetooth, hotspot, MQTT broker"],
            ["Sensors", "Tank calibration, temperature offsets, CO₂"],
            ["Power limits", "Battery chemistry, inverter, charger profiles"],
            ["Automation", "Rules, timers, scenes, geofence actions"],
            ["Service", "Logs, firmware, backup, factory reset"],
          ].map(([title, sub]) => (
            <button key={title} className="flex w-full items-center justify-between rounded-2xl border border-white/10 bg-white/[0.055] px-4 py-3 text-left hover:bg-white/[0.09]">
              <div>
                <div className="text-sm font-bold text-white">{title}</div>
                <div className="text-xs text-slate-400">{sub}</div>
              </div>
              <ChevronRight className="text-slate-400" size={20} />
            </button>
          ))}
        </div>
      </GlassCard>
    </div>
  );
}

export default function CampervanHMI() {
  const [activeTab, setActiveTab] = useState("home");
  const [activeScene, setActiveScene] = useState("camp");
  const [showEnergyStats, setShowEnergyStats] = useState(false);

  const [battery, setBattery] = useState(87);
  const [freshWater, setFreshWater] = useState(68);
  const [greyWater, setGreyWater] = useState(38);
  const [heater, setHeater] = useState(true);
  const [fridge, setFridge] = useState(true);
  const [inverter, setInverter] = useState(false);
  const [fan, setFan] = useState(true);
  const [solarPriority, setSolarPriority] = useState(true);
  const [chargeLimit, setChargeLimit] = useState(90);
  const [temp, setTemp] = useState(22.5);
  const [vent, setVent] = useState(42);
  const [pump, setPump] = useState(true);
  const [boiler, setBoiler] = useState(false);
  const [boilerTemp, setBoilerTemp] = useState(54);
  const [lightMain, setLightMain] = useState(74);
  const [lightKitchen, setLightKitchen] = useState(52);
  const [lightBed, setLightBed] = useState(18);
  const [lightAwning, setLightAwning] = useState(0);
  const [locks, setLocks] = useState(true);
  const [alarm, setAlarm] = useState(true);
  const [interlock, setInterlock] = useState(true);
  const [remote, setRemote] = useState(true);

  const state = {
    battery,
    freshWater,
    greyWater,
    heater,
    fridge,
    inverter,
    fan,
    solarPriority,
    temp,
    vent,
    pump,
    boiler,
    boilerTemp,
    lightMain,
    lightKitchen,
    lightBed,
    lightAwning,
    locks,
    alarm,
    interlock,
    remote,
  };

  const setters = {
    setBattery,
    setFreshWater,
    setGreyWater,
    setHeater,
    setFridge,
    setInverter,
    setFan,
    setSolarPriority,
    chargeLimit,
    setChargeLimit,
    setTemp,
    setVent,
    setPump,
    setBoiler,
    setBoilerTemp,
    setLightMain,
    setLightKitchen,
    setLightBed,
    setLightAwning,
    setLocks,
    setAlarm,
    setInterlock,
    setRemote,
  };

  const view = useMemo(() => {
    switch (activeTab) {
      case "power":
        return <PowerView state={state} setters={setters} openEnergyStats={() => setShowEnergyStats(true)} />;
      case "climate":
        return <ClimateView state={state} setters={setters} />;
      case "water":
        return <WaterView state={state} setters={setters} />;
      case "lights":
        return <LightsView state={state} setters={setters} />;
      case "systems":
        return <SystemsView state={state} setters={setters} />;
      default:
        return <HomeView state={state} setters={setters} openEnergyStats={() => setShowEnergyStats(true)} />;
    }
  }, [activeTab, state, setters]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 p-4 text-slate-100">
      <div className="relative h-[600px] w-[1080px] overflow-hidden rounded-[2.25rem] border border-white/10 bg-[radial-gradient(circle_at_top_left,rgba(34,211,238,0.16),transparent_30%),radial-gradient(circle_at_bottom_right,rgba(168,85,247,0.18),transparent_35%),linear-gradient(135deg,#020617,#0f172a_45%,#020617)] shadow-2xl shadow-black">
        <div
          className="pointer-events-none absolute inset-0 bg-cover bg-center opacity-70"
          style={{ backgroundImage: `url(${VAN_BACKGROUND_URL})` }}
        />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/50 to-slate-950/80" />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-b from-slate-950/45 via-transparent to-slate-950/80" />
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_58%_28%,rgba(14,165,233,0.08),transparent_28%),radial-gradient(circle_at_18%_35%,rgba(0,0,0,0.28),transparent_34%)]" />
        <div className="relative z-10 h-full">
        <TopBar activeScene={activeScene} setActiveScene={setActiveScene} />
        <div className="flex h-[526px] border-t border-white/10">
          <SideNav activeTab={activeTab} setActiveTab={setActiveTab} />
          <main className="h-full flex-1 p-4">
            <AnimatePresence mode="wait">
              <motion.div
                key={activeTab}
                initial={{ opacity: 0, x: 18, filter: "blur(8px)" }}
                animate={{ opacity: 1, x: 0, filter: "blur(0px)" }}
                exit={{ opacity: 0, x: -18, filter: "blur(8px)" }}
                transition={{ duration: 0.22 }}
                className="h-full"
              >
                {view}
              </motion.div>
            </AnimatePresence>
          </main>
        </div>
        <AnimatePresence>{showEnergyStats && <EnergyStatsModal onClose={() => setShowEnergyStats(false)} />}</AnimatePresence>
        </div>
      </div>
    </div>
  );
}
