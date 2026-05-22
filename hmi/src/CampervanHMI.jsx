import React, { useEffect, useMemo, useState } from "react";
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
import { camperAgentBridge } from "./integrations/camperAgentBridge";
import { FuturisticGauge } from "./components/vehicle/FuturisticGauge";
import { VehicleTempCard } from "./components/vehicle/VehicleTempCard";
import { VehicleBottomNav } from "./components/vehicle/VehicleBottomNav";
import { EngineDetailsView } from "./components/vehicle/EngineDetailsView";

// Clean Ford Transit campervan background only. Do not use a full GUI mockup here.
const VAN_BACKGROUND_URL = "assets/ford-transit-clean-bg.png";
const DESIGN_WIDTH = 1080;
const DESIGN_HEIGHT = 600;
const MIN_AUTO_SCALE = 0.90;
const DISPLAY_FIT_KEY = "camper_display_fit_settings";

const DEFAULT_BMS = {
  profile: { displayName: "PUPVWMHB 12V 320Ah LiFePO4 250A BMS", capacityAh: 320, nominalVoltage: 12.8, bmsContinuousCurrentAmp: 250 },
  telemetry: { socPercent: 87, voltage: 13.2, current: -18, powerWatts: -238, remainingCapacityAh: 278.4, chargeAllowed: true, dischargeAllowed: true, warnings: [], alarms: [], source: "simulator_fallback", protocol: "PUPVWMHB discovery pending" },
};

const tabs = [
  { id: "home", label: "Home", icon: Home },
  { id: "vehicle", label: "Vehicle", icon: Gauge },
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
    <div className="flex h-full w-[104px] flex-col items-center gap-1 border-r border-white/10 bg-black/20 py-3">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const active = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={cx("relative flex h-[62px] w-[82px] flex-col items-center justify-center gap-1 rounded-3xl transition", active ? "text-cyan-100" : "text-slate-500 hover:bg-white/5 hover:text-slate-200")}
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
  const bms = state.batteryBms?.telemetry ?? {};
  const batterySoc = Math.round(bms.socPercent ?? state.battery);
  const batterySub = bms.voltage && bms.current ? `${bms.voltage.toFixed(1)}V / ${bms.current.toFixed(0)}A` : "BMS pending";
  return (
    <div className="grid h-full grid-cols-[1.15fr_0.85fr] gap-4">
      <div className="grid grid-rows-[auto_1fr] gap-4">
        <div className="grid grid-cols-4 gap-3">
          <MiniStatus icon={BatteryCharging} label="Battery" value={`${batterySoc}%`} sub={batterySub} />
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
          <CircularGauge value={batterySoc} label="PUPVWMHB LiFePO4" unit="% SOC" icon={BatteryCharging} />
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

function VehicleDashboardView({ openIntegrationSettings }) {
  const [page, setPage] = useState("dashboard");
  const [snapshot, setSnapshot] = useState(null);
  const [commands, setCommands] = useState([]);
  const [commandStatus, setCommandStatus] = useState("");
  const [sendingCommand, setSendingCommand] = useState(null);
  const [time, setTime] = useState(() => new Date());
  const data = snapshot?.telemetry || {};
  const status = snapshot?.stale ? "STALE" : snapshot?.state === "Reconnecting" ? "RECONNECTING" : snapshot?.connected && snapshot?.polling ? "LIVE" : "OFFLINE";
  const stale = Boolean(snapshot?.stale);
  const isSimulated = data.source === "simulator" || snapshot?.source === "simulator";
  const oilValue = Number.isFinite(data.oilTempC) ? data.oilTempC : (isSimulated ? 92 : null);
  const outsideValue = Number.isFinite(data.outsideTempC) ? data.outsideTempC : Number.isFinite(data.ambientTempC) ? data.ambientTempC : null;
  const acOn = Number(data.acCompressorStatus) === 1;
  const driveModes = [
    ["drive_mode_normal", "Normal", "00"],
    ["drive_mode_eco", "Eco", "06"],
    ["drive_mode_slippery", "Slippery", "05"],
    ["drive_mode_mud_ruts", "Mud & Ruts", "08"],
    ["drive_mode_tow_haul", "Tow / Haul", "03"],
  ];
  const commandById = Object.fromEntries(commands.map((command) => [command.id, command]));
  const commandReady = (id) => commandById[id]?.enabled && commandById[id]?.command;
  const commandMissingReason = (id) => {
    const command = commandById[id];
    if (!command) return `${id} command is missing`;
    if (!command.enabled) return `${command.displayName || id} command is disabled`;
    if (!command.command) return `${command.displayName || id} command is empty`;
    return "";
  };
  const openCommandSettings = (tab = "drive") => openIntegrationSettings?.({ type: "vehicleCommands", initialTab: tab });
  const runCommand = async (id) => {
    if (!commandReady(id)) {
      setCommandStatus(commandMissingReason(id));
      openCommandSettings(id.startsWith("ac_") ? "ac" : "drive");
      return;
    }
    setSendingCommand(id);
    const result = await camperAgentBridge.executeVehicleCommand(id);
    setSendingCommand(null);
    setCommandStatus(result.ok ? `${commandById[id]?.displayName || id}: ${result.data.statusVerified ? "verified" : "sent"}` : result.error);
  };

  useEffect(() => {
    let active = true;
    async function load() {
      const result = await camperAgentBridge.getVehicleTelemetrySnapshot();
      if (active && result.ok) setSnapshot(result.data);
    }
    load();
    const timer = setInterval(load, 500);
    return () => { active = false; clearInterval(timer); };
  }, []);

  useEffect(() => {
    const loadCommands = () => camperAgentBridge.getVehicleCommands().then((result) => {
      if (result.ok) setCommands(result.data.commands || []);
    });
    loadCommands();
    window.addEventListener("camper-vehicle-commands-updated", loadCommands);
    return () => window.removeEventListener("camper-vehicle-commands-updated", loadCommands);
  }, []);

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const timeText = time.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  const statusClass = status === "LIVE" ? "border-cyan-300/50 bg-cyan-400/15 text-cyan-100" : status === "STALE" || status === "RECONNECTING" ? "border-amber-300/45 bg-amber-400/15 text-amber-100" : "border-rose-300/45 bg-rose-400/15 text-rose-100";

  return (
    <div className="relative h-full overflow-hidden bg-[#020817] text-white">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_34%,rgba(14,165,233,0.15),transparent_32%),linear-gradient(180deg,rgba(2,6,23,0.3),#020617)]" />
      <div className="pointer-events-none absolute inset-x-0 bottom-0 h-44 bg-[linear-gradient(rgba(14,165,233,0.10)_1px,transparent_1px),linear-gradient(90deg,rgba(14,165,233,0.10)_1px,transparent_1px)] bg-[size:42px_24px] opacity-40 [transform:perspective(260px)_rotateX(58deg)]" />
      <div className="relative grid h-full grid-rows-[58px_1fr_76px] gap-2 p-3">
        <div className="relative z-10 grid grid-cols-[390px_1fr_270px] items-center border-b border-cyan-300/35 bg-slate-950/55 px-4 shadow-lg shadow-cyan-500/10 [clip-path:polygon(0_0,100%_0,100%_78%,84%_78%,83%_100%,17%_100%,16%_78%,0_78%)]">
          <div className="flex items-center gap-4">
            <div className="rounded-full border border-cyan-300/60 px-4 py-1 font-serif text-2xl italic text-white shadow-lg shadow-cyan-400/20">Ford</div>
            <div className="text-xl font-black uppercase tracking-[0.14em] text-white">Vehicle Dashboard</div>
            <div className={`rounded-xl border px-3 py-1 text-xs font-black ${statusClass}`}>? {status}</div>
          </div>
          <div className="text-center text-lg font-black uppercase tracking-[0.18em] text-cyan-300">Ford Transit Campervan</div>
          <div className="flex items-center justify-end gap-8">
            <div className="text-right text-xs font-bold leading-5 text-slate-300"><div>{snapshot?.adapterName || "vLinker FS"}</div><div>{snapshot?.protocol || "ISO 15765-4 CAN 11/500"}</div></div>
            <div className="min-w-[92px] text-right text-4xl font-black text-white">{timeText}</div>
          </div>
        </div>

        {page === "dashboard" && (
          <div className="grid min-h-0 grid-rows-[minmax(0,1fr)_124px] gap-3">
            <div className="grid min-h-0 grid-cols-[1fr_250px_1fr] gap-3">
              <FuturisticGauge label="RPM" value={Number.isFinite(data.rpm) ? data.rpm / 1000 : null} min={0} max={5} unit="x1000" marks={[0, 1, 2, 3, 4, 5]} redFrom={4} formatter={() => Math.round(data.rpm).toLocaleString("sv-SE")} accent="#20c8ff" stale={stale} />
              <div className="flex min-h-0 flex-col justify-center border border-cyan-300/30 bg-slate-950/70 p-3 shadow-xl shadow-cyan-500/10 [clip-path:polygon(12%_0,88%_0,100%_12%,100%_88%,88%_100%,12%_100%,0_88%,0_12%)]">
                <div className="mb-2 text-center text-xs font-black uppercase tracking-[0.18em] text-cyan-300">Driving Modes</div>
                <div className="space-y-1.5">
                  {driveModes.map(([id, label, value]) => {
                    const active = String(data.driveMode ?? "").padStart(2, "0") === value;
                    return <button key={id} onClick={() => runCommand(id)} className={cx("w-full rounded-xl border px-2 py-1.5 text-xs font-black", active ? "border-cyan-200 bg-cyan-300/20 text-cyan-100" : commandReady(id) ? "border-white/10 bg-white/[0.07] text-white" : "border-white/5 bg-white/[0.03] text-slate-500")}>{sendingCommand === id ? "Sending..." : label}</button>;
                  })}
                </div>
                <div className="mt-2 min-h-[16px] truncate text-center text-[10px] font-bold text-amber-100">{commandStatus}</div>
              </div>
              <FuturisticGauge label="km/h" value={data.speedKph} min={0} max={200} unit="km/h" marks={[0,20,40,60,80,100,120,140,160,180,200]} redFrom={170} formatter={(v) => Math.round(v)} accent="#20c8ff" stale={stale} />
            </div>
            <div className="grid min-h-0 grid-cols-5 gap-3">
              <VehicleTempCard label="Coolant Temp" value={data.coolantTempC} min={40} max={120} warning={100} icon="?" stale={stale} />
              <VehicleTempCard label="Oil Temp" value={oilValue} min={40} max={130} warning={115} icon="?" subtext={oilValue == null ? "Not available" : undefined} simulated={isSimulated && oilValue != null} stale={stale} />
              <VehicleTempCard label="Outside Temp" value={outsideValue} min={-30} max={50} warning={45} icon="?" subtext={outsideValue == null ? "Not available" : undefined} stale={stale} />
              <VehicleAcTile acOn={acOn} readyOn={commandReady("ac_on")} readyOff={commandReady("ac_off")} sending={sendingCommand === "ac_on" || sendingCommand === "ac_off"} onPress={() => runCommand(acOn ? "ac_off" : "ac_on")} onConfigure={() => openCommandSettings("ac")} />
              <VehicleChargeTile data={data} />
            </div>
          </div>
        )}

        {page === "engine" && <EngineDetailsView snapshot={snapshot} />}
        {page === "settings" && <VehicleSettingsPanel openIntegrationSettings={openIntegrationSettings} />}
        {page !== "dashboard" && page !== "engine" && page !== "settings" && <div className="flex items-center justify-center border border-cyan-300/30 bg-slate-950/70 text-2xl font-black uppercase tracking-[0.18em] text-cyan-200 [clip-path:polygon(4%_0,96%_0,100%_12%,100%_88%,96%_100%,4%_100%,0_88%,0_12%)]">{page} page pending</div>}

        <VehicleBottomNav active={page} setActive={setPage} />
      </div>
    </div>
  );
}

function VehicleAcTile({ acOn, readyOn, readyOff, sending, onPress, onConfigure }) {
  const ready = acOn ? readyOff : readyOn;
  const handleClick = () => ready ? onPress() : onConfigure?.();
  return (
    <div className="relative overflow-hidden border border-cyan-300/40 bg-slate-950/80 p-3 shadow-xl shadow-cyan-500/20 [clip-path:polygon(8%_0,92%_0,100%_18%,100%_82%,92%_100%,8%_100%,0_82%,0_18%)]">
      <div className="text-xs font-black uppercase tracking-[0.14em] text-cyan-200">AC</div>
      <div className="mt-1 text-3xl font-black text-white">{acOn ? "ON" : "OFF"}</div>
      <button onClick={handleClick} disabled={sending} className="mt-2 w-full rounded-xl bg-cyan-300 px-2 py-1.5 text-[10px] font-black text-slate-950 disabled:bg-white/[0.08] disabled:text-slate-500">
        {ready ? sending ? "Sending..." : acOn ? "AC OFF" : "AC ON" : "Configure AC command"}
      </button>
      <div className="mt-1 text-[9px] font-bold text-slate-500">PCM 22099B status</div>
    </div>
  );
}

function VehicleSettingsPanel({ openIntegrationSettings }) {
  const rows = [
    ["Vehicle Command Library", "Paste commands and enable AC or drive mode functions permanently.", { type: "vehicleCommands", initialTab: "ac" }],
    ["OBD PID Library", "Map dashboard values to Ford/standard PIDs, formulas and polling intervals.", "obdPidMapping"],
    ["Ford OBD / vLinker", "USB connection, protocol, raw tests and DTC read-only tools.", "obd"],
    ["Display / Screen Fit", "Adjust scale and vertical offset for the media unit.", "displayFit"],
  ];
  return (
    <div className="grid min-h-0 grid-cols-[1fr_1fr] gap-4 border border-cyan-300/30 bg-slate-950/70 p-5 [clip-path:polygon(4%_0,96%_0,100%_12%,100%_88%,96%_100%,4%_100%,0_88%,0_12%)]">
      <div>
        <div className="text-2xl font-black uppercase tracking-[0.16em] text-cyan-200">Vehicle Settings</div>
        <div className="mt-3 max-w-md text-sm font-semibold leading-6 text-slate-400">AC and drive mode buttons only send commands saved in the command library and permanently enabled by you.</div>
      </div>
      <div className="space-y-2">
        {rows.map(([title, sub, modal]) => (
          <button key={title} onClick={() => openIntegrationSettings?.(modal)} className="w-full rounded-2xl border border-cyan-300/20 bg-white/[0.055] px-4 py-3 text-left hover:bg-cyan-300/10">
            <div className="text-sm font-black text-white">{title}</div>
            <div className="text-xs text-slate-400">{sub}</div>
          </button>
        ))}
      </div>
    </div>
  );
}

function VehicleChargeTile({ data }) {
  const current = Number.isFinite(data.generatorCurrentA) ? `${Math.round(data.generatorCurrentA)} A` : "--";
  const voltage = Number.isFinite(data.vehicleBatteryVoltage) ? `${Number(data.vehicleBatteryVoltage).toFixed(1)} V` : "--";
  const duty = Number.isFinite(data.alternatorDutyPercent) ? `${Math.round(data.alternatorDutyPercent)}%` : "--";
  const charging = Number.isFinite(data.generatorCurrentA) ? data.generatorCurrentA > 2 ? "Charging" : "Idle" : "Unknown";
  return (
    <div className="relative overflow-hidden border border-cyan-300/40 bg-slate-950/80 p-3 shadow-xl shadow-cyan-500/20 [clip-path:polygon(8%_0,92%_0,100%_18%,100%_82%,92%_100%,8%_100%,0_82%,0_18%)]">
      <div className="text-xs font-black uppercase tracking-[0.14em] text-cyan-200">Generator</div>
      <div className="mt-1 grid grid-cols-2 gap-x-2 gap-y-0.5 text-[10px] font-bold text-slate-300">
        <span>Current</span><span className="text-right text-white">{current}</span>
        <span>Voltage</span><span className="text-right text-white">{voltage}</span>
        <span>Alt duty</span><span className="text-right text-white">{duty}</span>
      </div>
      <div className="mt-1 text-center text-[10px] font-black uppercase text-emerald-300">{charging}</div>
    </div>
  );
}
function PowerView({ state, setters, openEnergyStats }) {
  const bms = state.batteryBms?.telemetry ?? {};
  const batterySoc = Math.round(bms.socPercent ?? state.battery);
  return (
    <div className="grid h-full grid-cols-[0.9fr_1.1fr] gap-4">
      <GlassCard className="p-5">
        <CircularGauge value={batterySoc} label="LiFePO4 320Ah" unit="%" icon={BatteryCharging} />
        <div className="mt-1 text-sm text-slate-400">320Ah / 250A BMS</div>
        <div className="mt-5 space-y-3">
          <RangeControl label="Charge limit" value={setters.chargeLimit ?? 90} setValue={setters.setChargeLimit} min={50} max={100} unit="%" icon={BatteryCharging} />
          <ToggleRow icon={PlugZap} label="230V Inverter" sub="Pure sine / outlet group A" on={state.inverter} setOn={setters.setInverter} />
          <ToggleRow icon={SolarPanel} label="Solar priority" sub="Prefer solar before alternator" on={state.solarPriority} setOn={setters.setSolarPriority} />
        </div>
      </GlassCard>
      <div className="grid grid-rows-[auto_1fr] gap-4">
        <div className="grid grid-cols-4 gap-3">
          <MiniStatus icon={SolarPanel} label="PV Input" value="326 W" sub="24.8V" />
          <MiniStatus icon={CarFront} label="Renogy 40A" value="0 W" sub="Engine off" />
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
            <FlowNode className="left-[310px] top-[102px]" icon={BatteryCharging} label="LiFePO4 320Ah" value={`${batterySoc}%`} primary />
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
    { day: "Mon", solar: 2.8, landline: 0.0, renogy: 0.4, orion: 0.2, battery: 74 },
    { day: "Tue", solar: 3.4, landline: 0.0, renogy: 0.2, orion: 0.1, battery: 81 },
    { day: "Wed", solar: 1.9, landline: 1.6, renogy: 0.0, orion: 0.2, battery: 87 },
    { day: "Thu", solar: 4.2, landline: 0.0, renogy: 0.6, orion: 0.3, battery: 94 },
    { day: "Fri", solar: 2.6, landline: 2.1, renogy: 0.0, orion: 0.1, battery: 96 },
    { day: "Sat", solar: 5.1, landline: 0.0, renogy: 0.3, orion: 0.2, battery: 100 },
    { day: "Sun", solar: 3.7, landline: 0.8, renogy: 0.6, orion: 0.2, battery: 92 },
  ];

  const totals = chargeData.reduce(
    (acc, item) => ({
      solar: acc.solar + item.solar,
      landline: acc.landline + item.landline,
      renogy: acc.renogy + item.renogy,
      orion: acc.orion + item.orion,
      total: acc.total + item.solar + item.landline + item.renogy + item.orion,
    }),
    { solar: 0, landline: 0, renogy: 0, orion: 0, total: 0 }
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
                  <div className="text-xs text-slate-400">kWh generated from SmartSolar, shore, Renogy and Orion</div>
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
                  <Bar dataKey="renogy" name="Renogy 40A" fill="#fbbf24" radius={[8, 8, 0, 0]} />
                  <Bar dataKey="orion" name="Orion 18A" fill="#34d399" radius={[8, 8, 0, 0]} />
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
                  <Line type="monotone" dataKey="battery" name="BMS SOC" stroke="#67e8f9" strokeWidth={4} dot={{ r: 4 }} activeDot={{ r: 7 }} />
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
              <div className="flex items-center gap-3 text-amber-100"><CarFront size={24} /><span className="text-sm font-bold uppercase tracking-[0.18em]">Renogy 40A</span></div>
              <div className="mt-3 text-3xl font-black text-white">{totals.renogy.toFixed(1)}</div>
              <div className="text-sm text-slate-300">kWh from alternator charger</div>
            </div>
            <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">
                <div className="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Orion 18A</div>
                <div className="mt-3 text-3xl font-black text-white">{totals.orion.toFixed(1)}</div>
                <div className="text-sm text-slate-300">kWh auxiliary DC/DC</div>
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

function ModalShell({ title, subtitle, icon: Icon, onClose, children }) {
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="absolute inset-0 z-50 flex items-center justify-center bg-black/60 p-6 backdrop-blur-md" onClick={onClose}>
      <motion.div initial={{ opacity: 0, y: 28, scale: 0.96 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: 18, scale: 0.97 }} transition={{ type: "spring", stiffness: 260, damping: 24 }} onClick={(event) => event.stopPropagation()} className="h-[520px] w-[900px] overflow-hidden rounded-[2rem] border border-white/10 bg-slate-950/95 shadow-2xl shadow-black">
        <div className="flex items-center justify-between border-b border-white/10 px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-cyan-300/15 p-3 text-cyan-100"><Icon size={26} /></div>
            <div><div className="text-2xl font-black text-white">{title}</div><div className="text-sm text-slate-400">{subtitle}</div></div>
          </div>
          <button onClick={onClose} className="rounded-2xl bg-white/[0.07] p-3 text-slate-300 transition hover:bg-white/[0.12] hover:text-white"><X size={22} /></button>
        </div>
        <div className="h-[448px] overflow-hidden p-5">{children}</div>
      </motion.div>
    </motion.div>
  );
}

function ReadOnlyBadge() {
  return <span className="rounded-full border border-emerald-200/20 bg-emerald-300/10 px-3 py-1 text-xs font-black text-emerald-100">READ-ONLY LOCKED</span>;
}

function SettingField({ label, children }) {
  return <label className="block rounded-2xl border border-white/10 bg-white/[0.045] p-3"><div className="mb-2 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">{label}</div>{children}</label>;
}

function StatusBadge({ value }) {
  const color = value === "Online" ? "border-emerald-200/20 bg-emerald-300/10 text-emerald-100" : value === "Error" ? "border-rose-200/20 bg-rose-300/10 text-rose-100" : "border-amber-200/20 bg-amber-300/10 text-amber-100";
  return <span className={cx("rounded-full border px-3 py-1 text-xs font-bold", color)}>{value}</span>;
}

function VictronSettingsModal({ onClose }) {
  const [mode, setMode] = useState("GxLan");
  const [host, setHost] = useState("");
  const [status, setStatus] = useState("Offline");
  async function saveAndTest() {
    await camperAgentBridge.saveVictronSettings({ enabled: true, mode, host, modbusPort: 502, mqttPort: 1883, readOnly: true });
    const result = await camperAgentBridge.testVictronConnection();
    setStatus(result.ok ? result.data.state || "Offline" : "Error");
  }
  const devices = ["System", "Battery monitor", "Solar charger", "Inverter/charger", "DC-DC charger", "Tank sensors"];
  const mappings = ["Battery SOC", "Battery voltage", "Battery current", "Battery power", "PV power", "Charger power", "Shore connected", "AC input source"];
  return (
    <ModalShell title="Victron" subtitle="GX, Venus OS, VE.Direct, Modbus/MQTT telemetry" icon={SolarPanel} onClose={onClose}>
      <div className="grid h-full grid-cols-[1fr_1.2fr] gap-4">
        <div className="space-y-3">
          <SettingField label="Connection mode"><select value={mode} onChange={(e) => setMode(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option value="GxLan">GX / Venus OS LAN</option><option value="ModbusTcp">Modbus TCP</option><option value="Mqtt">MQTT</option><option value="VeDirectUsb">VE.Direct USB, future</option></select></SettingField>
          <SettingField label="Host / IP"><input value={host} onChange={(e) => setHost(e.target.value)} placeholder="venus.local or 192.168.x.x" className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white outline-none" /></SettingField>
          <div className="grid grid-cols-2 gap-3"><SettingField label="Modbus TCP"><input value="502" readOnly className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white" /></SettingField><SettingField label="MQTT"><input value="1883" readOnly className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white" /></SettingField></div>
          <div className="flex items-center justify-between rounded-2xl border border-white/10 bg-white/[0.045] p-3"><ReadOnlyBadge /><StatusBadge value={status} /></div>
          <button onClick={saveAndTest} className="w-full rounded-2xl bg-cyan-300 px-4 py-3 text-sm font-black text-slate-950">Test Connection</button>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-3 text-sm font-black text-white">Device discovery</div><div className="space-y-2">{devices.map((item) => <div key={item} className="flex justify-between rounded-xl bg-black/20 px-3 py-2 text-xs"><span>{item}</span><span className="text-slate-400">Offline</span></div>)}</div></div>
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-3 text-sm font-black text-white">Telemetry mapping</div><div className="grid grid-cols-1 gap-2">{mappings.map((item) => <div key={item} className="rounded-xl bg-cyan-300/10 px-3 py-2 text-xs text-cyan-100">{item}</div>)}</div></div>
        </div>
      </div>
    </ModalShell>
  );
}

function GarminSettingsModal({ onClose }) {
  const [status, setStatus] = useState("Offline");
  async function scan() {
    const result = await camperAgentBridge.startNmea2000Scan({ profileId: "nmea2000", bitrate: 250000, readOnly: true });
    setStatus(result.ok ? result.data.state || "Simulated" : "Error");
  }
  async function stopScan() {
    const result = await camperAgentBridge.stopNmea2000Scan();
    setStatus(result.ok ? result.data.state || "Stopped" : "Error");
  }
  const pgns = [["127505", "12", "Fluid Level", "sim", "0.5 Hz"], ["129025", "8", "Position Rapid", "sim", "1 Hz"], ["126996", "3", "Product Information", "sim", "-"]];
  return (
    <ModalShell title="Garmin / NMEA" subtitle="NMEA 2000, EmpirBus discovery, Garmin network data" icon={Radio} onClose={onClose}>
      <div className="grid h-full grid-cols-[320px_1fr] gap-4">
        <div className="space-y-3">
          <SettingField label="Connection type"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>NMEA 2000 CAN adapter</option><option>Signal K server</option><option>NMEA 0183 serial</option><option>Garmin/EmpirBus discovery</option></select></SettingField>
          <SettingField label="CAN bitrate"><input value="250000" readOnly className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white" /></SettingField>
          <SettingField label="Adapter"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>USB serial CAN adapter</option><option>Actisense-style gateway, future</option><option>SocketCAN bridge, future</option></select></SettingField>
          <div className="flex items-center justify-between rounded-2xl border border-white/10 bg-white/[0.045] p-3"><ReadOnlyBadge /><StatusBadge value={status} /></div>
          <div className="grid grid-cols-3 gap-2">
            <button onClick={() => camperAgentBridge.listCanAdapters().then((r) => setStatus(r.ok ? "Adapters listed" : "Error"))} className="rounded-2xl bg-white/[0.07] px-3 py-3 text-xs font-black text-white">Scan CAN adapters</button>
            <button onClick={scan} className="rounded-2xl bg-cyan-300 px-3 py-3 text-xs font-black text-slate-950">Start NMEA scan</button>
            <button onClick={stopScan} className="rounded-2xl bg-white/[0.07] px-3 py-3 text-xs font-black text-white">Stop scan</button>
          </div>
        </div>
        <div className="grid grid-rows-[1fr_auto] gap-4">
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-3 text-sm font-black text-white">Detected PGNs</div><div className="grid grid-cols-5 gap-2 text-[11px] font-bold uppercase tracking-[0.12em] text-slate-400"><span>PGN</span><span>Source</span><span>Name</span><span>Last seen</span><span>Rate</span></div>{pgns.map((row) => <div key={row.join(":")} className="mt-2 grid grid-cols-5 gap-2 rounded-xl bg-black/20 px-3 py-2 text-xs text-slate-200">{row.map((v) => <span key={v}>{v}</span>)}</div>)}</div>
          <div className="grid grid-cols-2 gap-4"><div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4 text-xs text-slate-300">GPS, tanks, switching, battery/electrical, environment and alarms are supported discovery categories.</div><div className="rounded-3xl border border-amber-200/20 bg-amber-300/10 p-4 text-xs text-amber-100">EmpirBus circuits are unverified. Control disabled until circuit map is verified.</div></div>
        </div>
      </div>
    </ModalShell>
  );
}

function ObdSettingsModal({ onClose }) {
  const [status, setStatus] = useState("PermissionRequired");
  const [lastError, setLastError] = useState("");
  const [lastMessage, setLastMessage] = useState("");
  const [usbStatus, setUsbStatus] = useState({});
  const [rawLog, setRawLog] = useState([]);
  const [lastTx, setLastTx] = useState("");
  const [lastRx, setLastRx] = useState("");
  const [connecting, setConnecting] = useState(false);
  const [connected, setConnected] = useState(false);
  const [verified, setVerified] = useState(false);
  const [telemetry, setTelemetry] = useState({});
  const [supportedPids, setSupportedPids] = useState([]);
  const [baudRate, setBaudRate] = useState("Auto");
  const [protocol, setProtocol] = useState("ISO15765-4 CAN 11/500");
  const [rawCommand, setRawCommand] = useState("ATI");
  const [expertMode, setExpertMode] = useState(false);
  const [permissionLockedUntil, setPermissionLockedUntil] = useState(0);
  const baudRates = ["Auto", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "921600", "500000", "1000000", "2000000"];
  const protocols = ["Auto", "ISO15765-4 CAN 11/500", "ISO15765-4 CAN 29/500", "ISO15765-4 CAN 11/250", "ISO15765-4 CAN 29/250", "SAE J1939 CAN 29/250", "SAE J1939 CAN 29/500", "ISO9141-2", "ISO14230-4 KWP", "Custom"];
  const safeRawCommands = ["ATI", "ATDP", "ATDPN", "0100", "010C", "010D"];
  const permissionBusy = Date.now() < permissionLockedUntil && status === "PermissionRequested";
  const permissionLabel = status === "NoDevice" || status === "NoDeviceFound" ? "Scan USB" : status === "PermissionRequested" ? "Waiting for Android popup..." : status === "PermissionGranted" ? "Permission already granted" : status === "Open" ? "Serial open" : "Request permission";
  const connectLabel = connected || status === "ObdConnected" || status === "Polling" ? "Connected" : connecting ? "Connecting..." : status === "SerialOpen" || status === "Open" ? "Probe OBD" : status === "AdapterDetected" ? "Probe ECU" : status === "Error" || status.includes("Failed") || status.includes("NoResponse") ? "Retry connect" : status === "PermissionRequired" || status === "PermissionRequested" ? "Request USB permission" : "Connect";
  function applyResult(result) {
    const data = result?.data || {};
    const nextStatus = data.state || data.status?.state || data.status || (result?.ok ? "Device found" : "Error");
    setStatus(nextStatus);
    setUsbStatus(data.status || data.usbStatus || data);
    setLastMessage(data.message || "");
    setLastError(result?.ok ? data.lastError || "" : result?.error || "Unknown error");
    setLastTx(data.lastTx || "");
    setLastRx(data.lastRx || "");
    setConnected(Boolean(data.connected) || nextStatus === "ObdConnected" || nextStatus === "Polling");
    setVerified(Boolean(data.verified) || nextStatus === "ObdConnected");
    if (data.telemetry) setTelemetry(data.telemetry);
    if (Array.isArray(data.supportedPids)) setSupportedPids(data.supportedPids);
    else if (Array.isArray(data.telemetry?.supportedPids)) setSupportedPids(data.telemetry.supportedPids);
    if (Array.isArray(data.rawLog)) setRawLog(data.rawLog.slice(-60));
  }
  async function bridgeAction(action) {
    const settings = { baudRate, protocol, adapterType: "VLinkerUsb", readOnly: true };
    const calls = {
      scan: () => camperAgentBridge.scanUsbSerialDevices(),
      permission: () => camperAgentBridge.requestUsbPermission("obd"),
      connect: () => camperAgentBridge.connectObd(settings),
      disconnect: () => camperAgentBridge.disconnectObd(),
    };
    if (action === "connect" && connected) return;
    if (action === "permission") setPermissionLockedUntil(Date.now() + 5000);
    if (action === "connect") setConnecting(true);
    const result = await calls[action]();
    applyResult(result);
    if (action === "disconnect") { setConnected(false); setVerified(false); }
    setConnecting(false);
  }
  async function sendRaw() {
    const result = await camperAgentBridge.sendReadOnlyObdCommand(rawCommand);
    applyResult(result.ok ? { ...result, data: { ...result.data, state: `Sent ${rawCommand}` } } : result);
  }
  useEffect(() => {
    if (!connected) return undefined;
    const timer = setInterval(() => {
      camperAgentBridge.getObdConnectionStatus().then(applyResult);
    }, 2000);
    return () => clearInterval(timer);
  }, [connected]);
  return (
    <ModalShell title="Ford OBD / vLinker" subtitle="USB vLinker, ISO15765-4 CAN11/500, PIDs, DTC read-only" icon={CarFront} onClose={onClose}>
      <div className="grid h-full grid-cols-[330px_1fr] gap-4">
        <div className="space-y-2">
          <SettingField label="USB dongle"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>Auto detect</option><option>ELM327 compatible USB</option><option>OBDLink/STN compatible USB</option><option>vLinker USB</option></select></SettingField>
          <SettingField label="USB serial baud"><select value={baudRate} onChange={(e) => setBaudRate(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white">{baudRates.map((rate) => <option key={rate}>{rate}</option>)}</select></SettingField>
          <SettingField label="OBD protocol"><select value={protocol} onChange={(e) => setProtocol(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white">{protocols.map((item) => <option key={item}>{item}</option>)}</select></SettingField>
          <div className="rounded-2xl border border-cyan-200/15 bg-cyan-300/10 p-3 text-[11px] font-bold text-cyan-100">CAN 500k is selected by ATSP6. USB baud is separate.</div>
          <div className="grid grid-cols-2 gap-2">
            <button onClick={() => bridgeAction("scan")} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Rescan USB</button>
            <button disabled={permissionBusy} onClick={() => bridgeAction(status === "NoDevice" || status === "NoDeviceFound" ? "scan" : "permission")} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white disabled:opacity-50">{permissionLabel}</button>
            <button disabled={connecting || connected} onClick={() => bridgeAction(status === "PermissionRequired" || status === "PermissionRequested" ? "permission" : "connect")} className="rounded-2xl bg-cyan-300 px-3 py-2 text-xs font-black text-slate-950 disabled:bg-emerald-300 disabled:text-slate-950">{connectLabel}</button>
            <button onClick={() => bridgeAction("disconnect")} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Disconnect</button>
          </div>
          <div className="flex items-center justify-between rounded-2xl border border-white/10 bg-white/[0.045] p-3"><ReadOnlyBadge /><StatusBadge value={verified ? "Verified" : status} /></div>
          {lastMessage && <div className="rounded-2xl border border-white/10 bg-black/20 p-3 text-xs text-slate-200">{lastMessage}</div>}
          {lastError && <div className="rounded-2xl border border-rose-200/20 bg-rose-300/10 p-3 text-xs text-rose-100">{lastError}</div>}
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-3 rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="text-sm font-black text-white">Raw OBD status</div><div className="grid grid-cols-2 gap-2 text-[11px] text-slate-300">{[["USB VID/PID", `${usbStatus.vendorId ?? "--"} / ${usbStatus.productId ?? "--"}`], ["Driver", usbStatus.driver || "--"], ["Permission", String(usbStatus.permissionGranted ?? "--")], ["Serial open", String(usbStatus.open ?? "--")], ["Adapter", usbStatus.adapter || "ELM327 v2.3"], ["ECU", verified ? "responding" : "--"], ["Protocol", `${protocol} / ATSP6`], ["Last RX", lastRx || "--"]].map(([k, v]) => <div key={k} className="rounded-xl bg-black/25 px-3 py-2"><div className="font-black uppercase tracking-[0.12em] text-slate-500">{k}</div><div className="truncate text-slate-100">{v}</div></div>)}</div><div className="grid grid-cols-3 gap-2">{safeRawCommands.map((cmd) => <button key={cmd} onClick={() => { setRawCommand(cmd); camperAgentBridge.sendReadOnlyObdCommand(cmd).then(applyResult); }} className="rounded-xl bg-white/[0.07] px-2 py-2 text-xs font-black text-white">{cmd}</button>)}<button onClick={() => setRawLog([])} className="rounded-xl bg-white/[0.07] px-2 py-2 text-xs font-black text-white">Clear log</button></div><div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs text-amber-100">OBD mode is request/response. Raw CAN stream requires USB-CAN or experimental ELM ATMA monitor.</div></div>
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-2 text-sm font-black text-white">Live read-only PIDs</div><div className="grid grid-cols-2 gap-1">{[["RPM", telemetry.rpm], ["Speed", telemetry.speedKph], ["Coolant", telemetry.coolantTempC], ["Intake", telemetry.intakeTempC], ["Voltage", telemetry.moduleVoltage], ["MAF", telemetry.mafGps], ["Throttle", telemetry.throttlePercent], ["Ambient", telemetry.ambientTempC]].map(([k, v]) => <div key={k} className="rounded-xl bg-white/[0.055] px-3 py-1.5 text-xs text-slate-200"><span className="text-slate-400">{k}</span> <span className="font-black text-white">{v ?? "--"}</span></div>)}</div><div className="mt-2 max-h-12 overflow-auto rounded-xl bg-black/20 p-2 text-[10px] text-cyan-100">Supported PIDs: {supportedPids.length ? supportedPids.join(", ") : "waiting for 0100"}</div><div className="mt-3 rounded-2xl border border-white/10 bg-black/20 p-3"><div className="mb-2 text-xs font-black text-white">Advanced / Raw OBD</div><label className="flex items-center gap-2 text-xs text-slate-300"><input type="checkbox" checked={expertMode} onChange={(e) => setExpertMode(e.target.checked)} /> Expert mode</label><div className="mt-2 flex gap-2"><input disabled={!expertMode} value={rawCommand} onChange={(e) => setRawCommand(e.target.value)} className="min-w-0 flex-1 rounded-xl bg-slate-900 px-3 py-2 text-xs text-white" /><button disabled={!expertMode} onClick={sendRaw} className="rounded-xl bg-cyan-300 px-3 py-2 text-xs font-black text-slate-950 disabled:opacity-40">Send</button></div><div className="mt-2 grid grid-cols-2 gap-2"><button disabled={!expertMode} onClick={() => camperAgentBridge.startElmMonitorReadOnly().then(applyResult)} className="rounded-xl bg-white/[0.07] px-2 py-2 text-[11px] font-black text-white disabled:opacity-40">Start ATMA</button><button disabled={!expertMode} onClick={() => camperAgentBridge.stopElmMonitorReadOnly().then(applyResult)} className="rounded-xl bg-white/[0.07] px-2 py-2 text-[11px] font-black text-white disabled:opacity-40">Stop monitor</button></div><div className="mt-2 text-[11px] text-amber-100">Experimental monitor: ATMA read-only only. Ford CAN TX remains blocked.</div></div><div className="mt-3 max-h-20 overflow-auto rounded-2xl bg-black/25 p-2 font-mono text-[10px] text-slate-300">{rawLog.length ? rawLog.map((row, idx) => <div key={`${row.timestamp}-${idx}`}>{row.direction} {row.command || ""} {row.response || row.error || row.state || ""}</div>) : "No raw OBD log yet"}</div></div>
        </div>
      </div>
    </ModalShell>
  );
}

function IntegrationsHealthModal({ onClose }) {
  const [diagnosticsResult, setDiagnosticsResult] = useState(null);
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState("");
  useEffect(() => {
    camperAgentBridge.getSystemHealthSnapshot().then((result) => {
      if (result.ok) setHealth(result.data);
      else setHealthError(result.error || "Health snapshot failed");
    });
  }, []);
  async function exportDiagnostics() {
    const exported = await camperAgentBridge.exportIntegrationDiagnostics();
    let upload = null;
    if (exported.ok) upload = await camperAgentBridge.uploadDiagnosticsNow();
    setDiagnosticsResult({ exported, upload });
  }
  const diagnosticsPath = diagnosticsResult?.exported?.data?.path || "--";
  const uploadText = diagnosticsResult?.upload ? (diagnosticsResult.upload.ok ? "remote upload OK" : diagnosticsResult.upload.error || "remote upload failed") : "--";
  return (
    <ModalShell title="Integrations Health" subtitle="Adapters, permissions, last update, logs" icon={Wrench} onClose={onClose}>
      <div className="grid h-full grid-rows-[1fr_auto] gap-4">
        <div className="grid grid-cols-4 gap-3">
          {[
            ["USB", health?.usb],
            ["Ford OBD", health?.obd],
            ["Remote Logging", health?.remoteLogging],
            ["T-CAN485", health?.tcan485],
            ["Battery BMS", health?.bms],
            ["Victron", health?.victron],
            ["Garmin/NMEA", health?.garmin],
          ].map(([card, data]) => (
            <div key={card} className="rounded-3xl border border-white/10 bg-white/[0.045] p-3">
              <div className="text-base font-black text-white">{card}</div>
              <div className="mt-3 text-xs text-slate-300">status: {valueState(data)}</div>
              <div className="mt-3 text-xs text-slate-300">source: {data?.source || data?.driver || data?.serverUrl || "--"}</div>
              <div className="mt-3 text-xs text-slate-300">last error: {data?.lastError || data?.error || "--"}</div>
              <div className="mt-3 text-xs text-slate-300">read/write: {card === "Ford OBD" ? "user-enabled saved commands only" : "read-only"}</div>
            </div>
          ))}
          {healthError && <div className="rounded-3xl border border-rose-200/20 bg-rose-300/10 p-3 text-xs text-rose-100">{healthError}</div>}
        </div>
        <div className="grid grid-cols-[auto_1fr] gap-3">
          <button onClick={exportDiagnostics} className="rounded-2xl bg-cyan-300 px-4 py-3 text-sm font-black text-slate-950">Export diagnostics JSON</button>
          <div className="rounded-2xl border border-white/10 bg-black/20 p-3 text-xs text-slate-300">File: <span className="text-white">{diagnosticsPath}</span><br />Upload: <span className="text-white">{uploadText}</span></div>
        </div>
      </div>
    </ModalShell>
  );
}

function CanBusScannerModal({ onClose }) {
  const [status, setStatus] = useState("Stopped");
  async function start(profileId) {
    const result = await camperAgentBridge.startCanScan({ profileId, readOnly: true });
    setStatus(result.ok ? `${result.data.state}: ${result.data.profile}` : "Error");
  }
  return (
    <ModalShell title="CAN Bus Scanner" subtitle="Ford OBD, NMEA 2000, BMS-CAN profiles" icon={Radio} onClose={onClose}>
      <div className="grid h-full grid-cols-[300px_1fr] gap-4">
        <div className="space-y-3">
          {[["ford_obd", "Ford OBD CAN", "500000 / 11-bit / ISO15765-4 / ELM-vLinker"], ["nmea2000", "Garmin/NMEA 2000", "250000 / 29-bit / PGN passive discovery"], ["battery_bms", "Battery BMS CAN", "Auto, 250000, 500000 / 11 or 29-bit"]].map(([id, label, sub]) => <button key={id} onClick={() => start(id)} className="w-full rounded-2xl border border-white/10 bg-white/[0.055] p-3 text-left hover:bg-white/[0.09]"><div className="text-sm font-black text-white">{label}</div><div className="text-xs text-slate-400">{sub}</div></button>)}
          <button onClick={() => camperAgentBridge.stopCanScan().then((r) => setStatus(r.ok ? "Stopped" : "Error"))} className="w-full rounded-2xl bg-white/[0.07] px-4 py-3 text-sm font-black text-white">Stop scan</button>
          <div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs text-amber-100">Passive read-only discovery only. vLinker is for Ford OBD; use USB-CAN for NMEA/BMS where possible.</div>
        </div>
        <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">
          <div className="mb-3 flex items-center justify-between"><span className="font-black text-white">Raw CAN scan</span><StatusBadge value={status} /></div>
          <div className="grid grid-cols-6 gap-2 text-xs text-slate-400"><span>Timestamp</span><span>CAN ID</span><span>DLC</span><span>Data</span><span>Rate</span><span>Decoded</span></div>
          <div className="mt-4 rounded-2xl border border-white/10 bg-black/20 p-4 text-sm text-slate-400">No frames captured yet.</div>
          <div className="mt-4 grid grid-cols-4 gap-2 text-xs">
            {["ReadOnly default", "BenchTest locked", "AuxiliaryCamperCan future", "FordVehicleCan blocked"].map((mode) => <div key={mode} className="rounded-xl border border-white/10 bg-black/20 p-3 text-slate-300">{mode}</div>)}
          </div>
          <button onClick={() => setStatus("Simulated TX logged")} className="mt-3 rounded-2xl bg-white/[0.07] px-4 py-2 text-xs font-black text-white">Simulated TX</button>
          <div className="mt-3 rounded-2xl border border-rose-200/20 bg-rose-300/10 p-3 text-xs text-rose-100">Ford vehicle CAN writing is blocked. Use bench mode or approved adapter safety model.</div>
        </div>
      </div>
    </ModalShell>
  );
}

function RemoteLoggingSettingsModal({ onClose }) {
  const defaultUrl = "https://sometimes-women-supported-writings.trycloudflare.com";
  const [enabled, setEnabled] = useState(true);
  const [serverUrl, setServerUrl] = useState(defaultUrl);
  const [server, setServer] = useState("Unknown");
  const [lastUpload, setLastUpload] = useState("--");
  const [lastError, setLastError] = useState("--");
  const [logs, setLogs] = useState([]);
  async function save() {
    const result = await camperAgentBridge.saveRemoteLoggingSettings({ enabled, serverUrl });
    setLastError(result.ok ? "--" : result.error || "Save failed");
  }
  async function test() {
    await save();
    const result = await camperAgentBridge.testRemoteLoggingServer();
    setServer(result.ok ? "Online" : "Offline");
    setLastError(result.ok ? "--" : result.error || "Connection failed");
  }
  async function upload() {
    await save();
    const result = await camperAgentBridge.uploadDiagnosticsNow();
    setLastUpload(result.ok ? new Date().toLocaleTimeString() : "--");
    setLastError(result.ok ? "--" : result.error || "Upload failed");
  }
  async function fetchLogs() {
    const result = await camperAgentBridge.fetchLatestRemoteLogs();
    const lines = result.ok ? result.data?.lines || result.data?.data?.lines || [] : [];
    setLogs(lines.slice(-20));
    setLastError(result.ok ? "--" : result.error || "Fetch logs failed");
  }
  function tunnelMessage() {
    setLastError("Tunnel is already started from the PC with scripts/start-local-update-tunnel.ps1. Android cannot run PowerShell directly.");
  }
  return (
    <ModalShell title="Remote Logging / Cloudflare" subtitle="Live logs, diagnostics upload, tunnel status" icon={Wifi} onClose={onClose}>
      <div className="grid h-full grid-cols-[360px_1fr] gap-4">
        <div className="space-y-3">
          <button onClick={() => setEnabled(!enabled)} className="flex w-full items-center justify-between rounded-2xl border border-white/10 bg-white/[0.045] p-3 text-left">
            <span className="text-sm font-black text-white">Remote logging enabled</span><span className="text-cyan-100">{enabled ? "ON" : "OFF"}</span>
          </button>
          <SettingField label="Server URL"><input value={serverUrl} onChange={(e) => setServerUrl(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white outline-none" /></SettingField>
          <div className="rounded-2xl border border-white/10 bg-white/[0.045] p-3 text-xs text-slate-300">
            <div>Local PC: http://127.0.0.1:8787</div>
            <div className="mt-2">LAN: http://172.18.96.1:8787</div>
            <div className="mt-2 break-all">Tunnel: {defaultUrl}</div>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <button onClick={test} className="rounded-2xl bg-cyan-300 px-3 py-2 text-xs font-black text-slate-950">Test connection</button>
            <button onClick={upload} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Upload diagnostics</button>
            <button onClick={fetchLogs} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Fetch latest logs</button>
            <button onClick={() => navigator.clipboard?.writeText(serverUrl)} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Copy public URL</button>
            <button onClick={upload} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Export local diagnostics</button>
            <button onClick={tunnelMessage} className="rounded-2xl bg-white/[0.03] px-3 py-2 text-xs font-black text-slate-500">Start tunnel</button>
            <button onClick={tunnelMessage} className="rounded-2xl bg-white/[0.03] px-3 py-2 text-xs font-black text-slate-500">Stop tunnel</button>
          </div>
        </div>
        <div className="grid grid-rows-[auto_1fr] gap-4">
          <div className="grid grid-cols-3 gap-3">
            <div className="rounded-2xl border border-white/10 bg-white/[0.045] p-3"><div className="text-xs text-slate-400">Server</div><div className="text-lg font-black text-white">{server}</div></div>
            <div className="rounded-2xl border border-white/10 bg-white/[0.045] p-3"><div className="text-xs text-slate-400">Last upload</div><div className="text-lg font-black text-white">{lastUpload}</div></div>
            <div className="rounded-2xl border border-white/10 bg-white/[0.045] p-3"><div className="text-xs text-slate-400">Last error</div><div className="text-sm font-bold text-amber-100">{lastError}</div></div>
          </div>
          <div className="overflow-hidden rounded-3xl border border-white/10 bg-black/30 p-4">
            <div className="mb-2 text-sm font-black text-white">Latest log lines</div>
            <div className="h-[300px] overflow-y-auto font-mono text-[11px] leading-5 text-slate-300">
              {logs.length === 0 ? "No logs loaded." : logs.map((line, index) => <div key={index}>{typeof line === "string" ? line : JSON.stringify(line)}</div>)}
            </div>
          </div>
        </div>
      </div>
    </ModalShell>
  );
}

function Tcan485GatewayModal({ onClose }) {
  const [networkMode, setNetworkMode] = useState("Android Hotspot");
  const [ssid, setSsid] = useState("");
  const [password, setPassword] = useState("");
  const [manualUrl, setManualUrl] = useState("http://camper-tcan485.local");
  const [status, setStatus] = useState("Idle");
  const [beacon, setBeacon] = useState(null);
  const [gatewayStatus, setGatewayStatus] = useState(null);
  const [rs485Status, setRs485Status] = useState(null);
  const [bmsLatest, setBmsLatest] = useState(null);
  const [rs485Raw, setRs485Raw] = useState(null);
  const [canStatus, setCanStatus] = useState(null);
  const [canFrames, setCanFrames] = useState(null);
  const discoveredUrl = beacon?.baseUrl || (beacon?.ip ? `http://${beacon.ip}` : "");
  const baseUrl = discoveredUrl || manualUrl || "http://192.168.4.1";
  const modeMap = { "Van Router": "sta_router", "Android Hotspot": "sta_android_hotspot", "LilyGO Setup AP": "setup_ap" };
  const unwrap = (result) => result?.data?.data || result?.data || {};
  const setResult = (result, okText) => setStatus(result.ok ? okText : result.error || "Request failed");
  const rs485ProtocolLabel = (value) => value === "AutoDetect" ? "Passive listen / AutoDetect future" : value || "--";
  async function startDiscovery() {
    const result = await camperAgentBridge.startTcan485Discovery();
    setStatus(result.ok ? "Listening on UDP 47887" : "Error");
    if (result.data?.beacon && typeof result.data.beacon === "object") setBeacon(result.data.beacon);
  }
  async function refreshDiscovery() {
    const result = await camperAgentBridge.getTcan485DiscoverySnapshot();
    setStatus(result.ok && result.data?.discoveryRunning ? "Listening on UDP 47887" : "Stopped");
    if (result.data?.beacon && typeof result.data.beacon === "object") setBeacon(result.data.beacon);
  }
  async function testConnection() {
    const result = await camperAgentBridge.testTcan485Health(baseUrl);
    setStatus(result.ok ? `Health OK: ${baseUrl}` : result.error || "Health check failed");
  }
  async function saveWifiToLilyGo() {
    const result = await camperAgentBridge.saveTcan485WifiSettings(baseUrl, { wifiMode: modeMap[networkMode] || "sta_android_hotspot", ssid, password, hostname: "camper-tcan485", fallbackApEnabled: true });
    await camperAgentBridge.saveTcan485Settings({ networkMode: modeMap[networkMode] || "sta_android_hotspot", baseUrl, hostname: "camper-tcan485", readOnly: true });
    setResult(result, "Wi-Fi saved to LilyGO");
  }
  async function refreshGatewayStatus() {
    const result = await camperAgentBridge.getTcan485GatewayStatus(baseUrl);
    if (result.ok) setGatewayStatus(unwrap(result));
    setResult(result, "Gateway status refreshed");
  }
  async function refreshRs485Status() {
    const result = await camperAgentBridge.getTcan485Rs485Status(baseUrl);
    if (result.ok) setRs485Status(unwrap(result));
    setResult(result, "RS485 status refreshed");
  }
  async function fetchBmsLatest() {
    const result = await camperAgentBridge.getTcan485BmsLatest(baseUrl);
    if (result.ok) setBmsLatest(unwrap(result));
    setResult(result, "BMS latest fetched");
  }
  async function fetchRs485Raw() {
    const result = await camperAgentBridge.getTcan485Rs485RawLatest(baseUrl);
    if (result.ok) setRs485Raw(unwrap(result));
    setResult(result, "RS485 raw fetched");
  }
  async function refreshCanStatus() {
    const result = await camperAgentBridge.getTcan485CanStatus(baseUrl);
    if (result.ok) setCanStatus(unwrap(result));
    setResult(result, "CAN status refreshed");
  }
  async function fetchCanFrames() {
    const result = await camperAgentBridge.getTcan485CanFramesLatest(baseUrl);
    if (result.ok) setCanFrames(unwrap(result));
    setResult(result, "CAN frames fetched");
  }
  async function rebootLilyGo() {
    const result = await camperAgentBridge.rebootTcan485(baseUrl);
    setResult(result, "LilyGO reboot requested");
  }
  return (
    <ModalShell title="T-CAN485 Gateway" subtitle="Android hotspot, UDP discovery, RS485 BMS gateway" icon={Wifi} onClose={onClose}>
      <div className="grid h-full grid-cols-[330px_1fr] gap-4">
        <div className="space-y-2">
          <SettingField label="Network mode"><select value={networkMode} onChange={(e) => setNetworkMode(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>Van Router</option><option>Android Hotspot</option><option>LilyGO Setup AP</option></select></SettingField>
          <SettingField label="Hotspot SSID"><input value={ssid} onChange={(e) => setSsid(e.target.value)} placeholder="Android hotspot SSID" className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white outline-none" /></SettingField>
          <SettingField label="Hotspot password"><input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="Not logged or uploaded" className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white outline-none" /></SettingField>
          <SettingField label="Manual base URL"><input value={manualUrl} onChange={(e) => setManualUrl(e.target.value)} className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white outline-none" /></SettingField>
          <div className="grid grid-cols-2 gap-2">
            <button onClick={() => camperAgentBridge.openAndroidHotspotSettings().then(() => setStatus("Open Android hotspot settings manually"))} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Open hotspot settings</button>
            <button onClick={testConnection} className="rounded-2xl bg-cyan-300 px-3 py-2 text-xs font-black text-slate-950">Test connection</button>
            <button onClick={startDiscovery} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Start discovery</button>
            <button onClick={() => camperAgentBridge.stopTcan485Discovery().then(() => setStatus("Stopped"))} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Stop discovery</button>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <button onClick={saveWifiToLilyGo} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Save Wi-Fi to LilyGO</button>
            <button onClick={refreshGatewayStatus} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Refresh gateway status</button>
            <button onClick={refreshRs485Status} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Refresh RS485 status</button>
            <button onClick={fetchBmsLatest} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Fetch BMS latest</button>
            <button onClick={fetchRs485Raw} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Fetch RS485 raw</button>
            <button onClick={refreshCanStatus} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Refresh CAN status</button>
            <button onClick={fetchCanFrames} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Fetch CAN frames</button>
            <button onClick={rebootLilyGo} className="rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Reboot LilyGO</button>
          </div>
          <button onClick={refreshDiscovery} className="w-full rounded-2xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Refresh discovery</button>
          <div className="rounded-2xl border border-white/10 bg-black/20 p-3 text-xs text-slate-200">{status}</div>
        </div>
        <div className="grid grid-rows-[auto_1fr] gap-4">
          <div className="grid grid-cols-3 gap-3">
            {[["Android Hotspot Mode", "Use this if the head unit has SIM/4G/USB/Ethernet internet. LilyGO joins Android's hotspot and the app keeps internet."], ["Van Router Mode", "Best mode. Android and LilyGO join the same router."], ["Setup AP Mode", "Only for first setup. Android may lose internet while connected directly to LilyGO."]].map(([title, text]) => <div key={title} className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="text-sm font-black text-white">{title}</div><div className="mt-3 text-xs text-slate-300">{text}</div></div>)}
          </div>
          <div className="overflow-hidden rounded-3xl border border-white/10 bg-white/[0.045] p-4">
            <div className="mb-3 flex items-center justify-between"><span className="font-black text-white">Live gateway data</span><StatusBadge value={beacon ? "Candidate found" : "Waiting"} /></div>
            <div className="grid max-h-[335px] grid-cols-2 gap-3 overflow-y-auto pr-1 text-xs text-slate-300">
              <div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-amber-100">Android hotspot keeps internet only if Android has non-Wi-Fi upstream internet. Enable hotspot manually, then press Discover LilyGO.</div>
              <div className="rounded-2xl bg-black/25 p-3">Base URL: <span className="font-black text-white">{baseUrl}</span><br />Discovered IP: <span className="font-black text-white">{beacon?.ip || "--"}</span><br />Hostname: <span className="font-black text-white">{beacon?.hostname || "camper-tcan485"}</span></div>
              <div className="rounded-2xl bg-black/25 p-3">Wi-Fi<br />mode: <span className="font-black text-white">{gatewayStatus?.wifi?.mode || "--"}</span><br />ip: <span className="font-black text-white">{gatewayStatus?.wifi?.ip || "--"}</span><br />rssi: <span className="font-black text-white">{gatewayStatus?.wifi?.rssi ?? "--"}</span></div>
              <div className="rounded-2xl bg-black/25 p-3">RS485<br />enabled: <span className="font-black text-white">{String((rs485Status || gatewayStatus?.rs485)?.enabled ?? "--")}</span><br />online: <span className="font-black text-white">{String((rs485Status || gatewayStatus?.rs485)?.online ?? "--")}</span><br />baud: <span className="font-black text-white">{(rs485Status || gatewayStatus?.rs485)?.baud || "--"}</span><br />protocol: <span className="font-black text-white">{rs485ProtocolLabel((rs485Status || gatewayStatus?.rs485)?.protocol)}</span><br />framesRx: <span className="font-black text-white">{(rs485Status || gatewayStatus?.rs485)?.framesRx ?? "--"}</span><br />lastRxMs: <span className="font-black text-white">{(rs485Status || gatewayStatus?.rs485)?.lastRxMs ?? "--"}</span></div>
              <div className="rounded-2xl bg-black/25 p-3">CAN<br />enabled: <span className="font-black text-white">{String((canStatus || gatewayStatus?.can)?.enabled ?? "--")}</span><br />running: <span className="font-black text-white">{String((canStatus || gatewayStatus?.can)?.running ?? "--")}</span><br />profile: <span className="font-black text-white">{(canStatus || gatewayStatus?.can)?.profile || "--"}</span><br />bitrate: <span className="font-black text-white">{(canStatus || gatewayStatus?.can)?.bitrate || "--"}</span><br />listenOnly: <span className="font-black text-white">{String((canStatus || gatewayStatus?.can)?.listenOnly ?? "--")}</span><br />framesRx: <span className="font-black text-white">{(canStatus || gatewayStatus?.can)?.framesRx ?? "--"}</span><br />busErrors: <span className="font-black text-white">{(canStatus || gatewayStatus?.can)?.busErrors ?? "--"}</span></div>
              <div className="rounded-2xl bg-black/25 p-3"><div className="mb-1 font-black text-white">BMS latest JSON</div><pre className="max-h-28 overflow-auto whitespace-pre-wrap font-mono text-[10px]">{JSON.stringify(bmsLatest || { valid: false, values: null }, null, 2)}</pre></div>
              <div className="rounded-2xl bg-black/25 p-3"><div className="mb-1 font-black text-white">Latest RS485 raw</div><pre className="max-h-28 overflow-auto whitespace-pre-wrap font-mono text-[10px]">{JSON.stringify(rs485Raw || { frames: [] }, null, 2)}</pre></div>
              <div className="rounded-2xl bg-black/25 p-3"><div className="mb-1 font-black text-white">Latest CAN frames</div><pre className="max-h-28 overflow-auto whitespace-pre-wrap font-mono text-[10px]">{JSON.stringify(canFrames || { frames: [] }, null, 2)}</pre></div>
            </div>
          </div>
        </div>
      </div>
    </ModalShell>
  );
}

function NetworkSettingsModal({ onClose }) {
  const [status, setStatus] = useState(null);
  const [message, setMessage] = useState("--");
  const [tcanUrl, setTcanUrl] = useState("http://192.168.4.1");
  useEffect(() => {
    camperAgentBridge.getNetworkStatus().then((result) => {
      if (result.ok) setStatus(result.data);
      else setMessage(result.error || "Network status failed");
    });
  }, []);
  async function testInternet() {
    const result = await camperAgentBridge.testInternetConnection();
    setMessage(result.ok && result.data.online ? "Internet OK" : result.error || "Internet offline");
  }
  async function testRemote() {
    const result = await camperAgentBridge.testRemoteLoggingServer();
    setMessage(result.ok ? "Remote logging server OK" : result.error || "Remote logging offline");
  }
  async function testTcan() {
    const result = await camperAgentBridge.testTcan485Health(tcanUrl);
    setMessage(result.ok ? "T-CAN485 health OK" : result.error || "T-CAN485 offline");
  }
  return (
    <ModalFrame title="Network" onClose={onClose}>
      <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
        <div className="space-y-3 rounded-3xl border border-white/10 bg-white/[0.045] p-4">
          <div className="text-lg font-black text-white">Android network</div>
          <div className="rounded-2xl bg-black/25 p-3 text-xs text-slate-300">Local IPs: {(status?.android?.localIps || []).join(", ") || "--"}</div>
          <button onClick={testInternet} className="rounded-2xl bg-cyan-300 px-4 py-2 text-sm font-black text-slate-950">Test internet</button>
          <button onClick={() => camperAgentBridge.openAndroidHotspotSettings().then((r) => setMessage(r.ok ? "Opened Android wireless settings" : r.error))} className="ml-2 rounded-2xl bg-white/[0.08] px-4 py-2 text-sm font-black text-white">Open hotspot settings</button>
          <div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs text-amber-100">Android hotspot keeps internet only when the head unit has SIM/4G/USB/Ethernet upstream.</div>
        </div>
        <div className="space-y-3 rounded-3xl border border-white/10 bg-white/[0.045] p-4">
          <div className="text-lg font-black text-white">Remote / T-CAN485</div>
          <button onClick={testRemote} className="rounded-2xl bg-white/[0.08] px-4 py-2 text-sm font-black text-white">Test Cloudflare logging</button>
          <label className="block text-xs text-slate-300">T-CAN485 base URL<input value={tcanUrl} onChange={(e) => setTcanUrl(e.target.value)} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 text-white" /></label>
          <button onClick={testTcan} className="rounded-2xl bg-cyan-300 px-4 py-2 text-sm font-black text-slate-950">Test T-CAN485 /health</button>
          <div className="max-h-28 overflow-auto rounded-2xl bg-black/25 p-3 font-mono text-[10px] text-cyan-100">{message}</div>
        </div>
      </div>
    </ModalFrame>
  );
}

function valueState(value) {
  if (value?.state) return value.state;
  if (value?.connected) return "Connected";
  if (value?.enabled === false) return "Disabled";
  return "Unknown";
}

function ModalFrame({ title, onClose, children }) {
  return (
    <motion.div className="absolute inset-0 z-50 flex items-center justify-center bg-black/55 p-6 backdrop-blur-md" onClick={onClose} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
      <motion.div className="h-[520px] w-[900px] rounded-[2rem] border border-white/15 bg-slate-950/92 p-5 shadow-2xl shadow-cyan-950/40" onClick={(event) => event.stopPropagation()} initial={{ scale: 0.96, y: 16 }} animate={{ scale: 1, y: 0 }} exit={{ scale: 0.96, y: 16 }}>
        <div className="mb-4 flex items-center justify-between">
          <div className="text-xl font-black text-white">{title}</div>
          <button onClick={onClose} className="rounded-2xl bg-white/[0.08] px-3 py-2 text-sm font-black text-white">X</button>
        </div>
        <div className="h-[450px] min-h-0">{children}</div>
      </motion.div>
    </motion.div>
  );
}

function DisplayFitSettingsModal({ settings, scale, onSave, onClose }) {
  const [draft, setDraft] = useState(settings);
  const [viewport, setViewport] = useState({ width: window.innerWidth || 1080, height: window.innerHeight || 600 });
  useEffect(() => {
    const update = () => setViewport({ width: window.innerWidth || 1080, height: window.innerHeight || 600 });
    update();
    window.addEventListener("resize", update);
    window.addEventListener("orientationchange", update);
    return () => {
      window.removeEventListener("resize", update);
      window.removeEventListener("orientationchange", update);
    };
  }, []);
  const update = (patch) => setDraft((current) => ({ ...current, ...patch }));
  const reset = () => setDraft({ autoFit: true, manualScale: 1, offsetY: 0 });
  return (
    <ModalFrame title="Display / Screen Fit" onClose={onClose}>
      <div className="grid h-full grid-cols-[1fr_1fr] gap-4">
        <div className="space-y-4 rounded-3xl border border-white/10 bg-white/[0.045] p-4">
          <label className="flex items-center justify-between text-sm font-bold text-white">
            Auto fit
            <input type="checkbox" checked={draft.autoFit} onChange={(e) => update({ autoFit: e.target.checked })} />
          </label>
          <div>
            <div className="mb-2 flex justify-between text-xs font-bold text-slate-300"><span>Scale</span><span>{draft.autoFit ? `auto ${scale.toFixed(2)}` : Number(draft.manualScale).toFixed(2)}</span></div>
            <input type="range" min="0.9" max="1" step="0.01" disabled={draft.autoFit} value={draft.manualScale ?? 1} onChange={(e) => update({ manualScale: Number(e.target.value) })} className="w-full" />
          </div>
          <div>
            <div className="mb-2 flex justify-between text-xs font-bold text-slate-300"><span>Vertical offset</span><span>{draft.offsetY ?? 0}px</span></div>
            <input type="range" min="-80" max="80" step="1" value={draft.offsetY ?? 0} onChange={(e) => update({ offsetY: Number(e.target.value) })} className="w-full" />
            <div className="mt-1 text-[11px] text-slate-500">Positive offset moves the GUI down. Negative offset moves it up.</div>
          </div>
          <div className="flex gap-2">
            <button onClick={() => { onSave(draft); onClose(); }} className="rounded-2xl bg-cyan-300 px-4 py-2 text-sm font-black text-slate-950">Save</button>
            <button onClick={reset} className="rounded-2xl bg-white/[0.08] px-4 py-2 text-sm font-black text-white">Reset display fit</button>
          </div>
        </div>
        <div className="rounded-3xl border border-cyan-300/20 bg-slate-950/60 p-4 text-sm text-slate-300">
          <div className="text-lg font-black text-white">Current fit</div>
          <div className="mt-3 space-y-2">
            <div>Design: 1080 x 600</div>
            <div>Viewport: {viewport.width} x {viewport.height}</div>
            <div>Auto scale: 1.00 at 600px+, 0.94 minimum between 560-599px, 0.90 minimum below 560px</div>
            <div>Current scale: {scale.toFixed(3)}</div>
            <div>Offset: {draft.offsetY ?? 0}px</div>
          </div>
        </div>
      </div>
    </ModalFrame>
  );
}

function ObdPidMappingSettingsModal({ onClose }) {
  const [tab, setTab] = useState("all");
  const [mappings, setMappings] = useState([]);
  const [selected, setSelected] = useState(null);
  const [status, setStatus] = useState("");
  const [search, setSearch] = useState("");
  const [moduleFilter, setModuleFilter] = useState("All");
  const [enabledOnly, setEnabledOnly] = useState(false);
  const mainKeys = ["speedKph", "rpm", "coolantTempC", "oilTempC", "outsideTempC"];
  const formulaPresets = ["A", "A&1", "(A>>1)&1", "(A>>2)&1", "(A>>3)&1", "(A>>4)&1", "(A>>5)&1", "A-40", "((A*256)+B)", "((A*256)+B)/4", "((A*256)+B)/10", "((A*256)+B)/16", "((A*256)+B)/100", "((A*256)+B)/1000", "((A*256)+B)/32768", "((A*256)+B)/16384", "A*100/255", "A*100/128", "A-125", "((A*256)+B)*0.05", "((A*256)+B)*2/100", "custom future"];

  useEffect(() => {
    camperAgentBridge.getObdPidLibrary().then((result) => {
      if (result.ok) setMappings(result.data.mappings || []);
    });
  }, []);

  const modules = ["All", ...Array.from(new Set(mappings.map((row) => row.module || "PCM"))).sort()];
  const visible = mappings.filter((row) => {
    const text = `${row.label} ${row.functionKey} ${row.service}${row.pid} ${row.category} ${row.module}`.toLowerCase();
    if (search && !text.includes(search.toLowerCase())) return false;
    if (moduleFilter !== "All" && row.module !== moduleFilter) return false;
    if (enabledOnly && !row.enabled) return false;
    if (tab === "main") return mainKeys.includes(row.functionKey);
    if (tab === "enabled") return row.enabled;
    if (tab === "all") return true;
    return !mainKeys.includes(row.functionKey);
  });
  const updateSelected = (patch) => setSelected((current) => ({ ...current, ...patch }));
  const toggleEnabled = async (row, enabled) => {
    const result = await camperAgentBridge.setObdPidEnabled(row.functionKey, enabled);
    if (result.ok) {
      setMappings(result.data.mappings || []);
      setSelected((current) => current?.functionKey === row.functionKey ? { ...current, enabled } : current);
    }
    setStatus(result.ok ? `${row.label}: ${enabled ? "enabled" : "disabled"}` : result.error);
  };
  const saveSelected = async () => {
    const next = mappings.map((row) => row.functionKey === selected.functionKey ? selected : row);
    setMappings(next);
    const result = await camperAgentBridge.saveObdPidMappings({ mappings: next });
    setStatus(result.ok ? "Saved PID mappings" : result.error);
  };
  const reset = async () => {
    const result = await camperAgentBridge.resetObdPidMappingsToDefault();
    if (result.ok) setMappings(result.data.mappings || []);
    setStatus(result.ok ? "Reset to Ford defaults" : result.error);
  };
  const test = async () => {
    const result = await camperAgentBridge.testObdPidMapping(selected);
    setStatus(result.ok ? `TX ${result.data.tx} RX ${result.data.rx || "--"} value ${result.data.decoded?.value ?? "--"}` : result.error);
  };

  return (
    <ModalFrame title="OBD PID Library" onClose={onClose}>
      <div className="grid h-full grid-cols-[1fr_340px] gap-4">
        <div className="min-h-0 rounded-3xl border border-white/10 bg-white/[0.04] p-4">
          <div className="mb-3 flex gap-2">
            <button onClick={() => setTab("all")} className={cx("rounded-xl px-3 py-2 text-xs font-black", tab === "all" ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-white")}>All PIDs</button>
            <button onClick={() => setTab("main")} className={cx("rounded-xl px-3 py-2 text-xs font-black", tab === "main" ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-white")}>Main Dashboard</button>
            <button onClick={() => setTab("engine")} className={cx("rounded-xl px-3 py-2 text-xs font-black", tab === "engine" ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-white")}>Engine Details</button>
            <button onClick={() => setTab("enabled")} className={cx("rounded-xl px-3 py-2 text-xs font-black", tab === "enabled" ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-white")}>Enabled</button>
            <button onClick={reset} className="ml-auto rounded-xl bg-white/[0.07] px-3 py-2 text-xs font-black text-white">Reset</button>
          </div>
          <div className="mb-3 grid grid-cols-[1fr_120px_90px] gap-2">
            <input placeholder="Search PID, name, category..." value={search} onChange={(e) => setSearch(e.target.value)} className="rounded-xl bg-slate-900 px-3 py-2 text-xs text-white" />
            <select value={moduleFilter} onChange={(e) => setModuleFilter(e.target.value)} className="rounded-xl bg-slate-900 px-2 py-2 text-xs text-white">{modules.map((module) => <option key={module}>{module}</option>)}</select>
            <label className="flex items-center gap-2 rounded-xl bg-white/[0.06] px-2 text-[11px] font-bold text-white"><input type="checkbox" checked={enabledOnly} onChange={(e) => setEnabledOnly(e.target.checked)} /> Enabled</label>
          </div>
          <div className="max-h-[395px] space-y-2 overflow-y-auto pr-1">
            {visible.map((row) => (
              <button key={row.functionKey} onClick={() => setSelected(row)} className="grid w-full grid-cols-[28px_1fr_70px_80px_70px] items-center gap-2 rounded-2xl border border-white/10 bg-slate-950/55 px-3 py-2 text-left text-xs hover:bg-white/[0.08]">
                <input type="checkbox" checked={Boolean(row.enabled)} onChange={(e) => { e.stopPropagation(); toggleEnabled(row, e.target.checked); }} onClick={(e) => e.stopPropagation()} />
                <div><div className="font-black text-white">{row.label}</div><div className="text-slate-400">{row.mode}</div></div>
                <div className={row.enabled ? "text-emerald-300" : "text-slate-500"}>{row.enabled ? "Enabled" : "Off"}</div>
                <div className="font-mono text-cyan-200">{row.service}{row.pid}</div>
                <div className="text-right text-slate-300">{row.unit}</div>
              </button>
            ))}
          </div>
        </div>
        <div className="h-full min-h-0 overflow-y-auto rounded-3xl border border-cyan-300/20 bg-slate-950/70 p-4">
          {selected ? (
            <div className="space-y-2 text-xs">
              <label className="flex items-center gap-2 font-bold text-white"><input type="checkbox" checked={selected.enabled} onChange={(e) => updateSelected({ enabled: e.target.checked })} /> Enabled</label>
              {["label", "functionKey", "mode", "service", "pid", "unit", "category", "module"].map((field) => (
                <label key={field} className="block text-slate-300">{field}<input value={selected[field] ?? ""} onChange={(e) => updateSelected({ [field]: e.target.value })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 font-mono text-white" /></label>
              ))}
              <label className="block text-slate-300">setupCommands<textarea value={(selected.setupCommands || []).join("\n")} onChange={(e) => updateSelected({ setupCommands: e.target.value.split(/\n+/).map((v) => v.trim()).filter(Boolean) })} className="mt-1 h-14 w-full rounded-xl bg-slate-900 px-3 py-2 font-mono text-white" /></label>
              <label className="block text-slate-300">Formula preset<select value={selected.formula} onChange={(e) => updateSelected({ formula: e.target.value })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 text-white">{formulaPresets.map((item) => <option key={item}>{item}</option>)}</select></label>
              <div className="grid grid-cols-2 gap-2">
                <label className="text-slate-300">Poll ms<input type="number" value={selected.pollIntervalMs ?? 2000} onChange={(e) => updateSelected({ pollIntervalMs: Number(e.target.value) })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 text-white" /></label>
                <label className="text-slate-300">Timeout ms<input type="number" value={selected.timeoutMs ?? 2000} onChange={(e) => updateSelected({ timeoutMs: Number(e.target.value) })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 text-white" /></label>
              </div>
              <div className="flex gap-2 pt-2">
                <button onClick={test} className="rounded-xl bg-white/[0.08] px-3 py-2 font-black text-white">Test PID</button>
                <button onClick={saveSelected} className="rounded-xl bg-cyan-300 px-3 py-2 font-black text-slate-950">Save</button>
              </div>
              <div className="max-h-16 overflow-auto rounded-xl bg-black/25 p-2 font-mono text-[10px] text-cyan-100">{status || "No test yet"}</div>
            </div>
          ) : <div className="flex h-full items-center justify-center text-sm text-slate-400">Select a mapping row</div>}
        </div>
      </div>
    </ModalFrame>
  );
}

function VehicleCommandLibraryModal({ onClose, initialTab = "ac" }) {
  const [tab, setTab] = useState(initialTab);
  const [commands, setCommands] = useState([]);
  const [selected, setSelected] = useState(null);
  const [status, setStatus] = useState("");
  const [log, setLog] = useState([]);
  useEffect(() => {
    camperAgentBridge.getVehicleCommands().then((result) => {
      if (result.ok) setCommands(result.data.commands || []);
    });
    camperAgentBridge.getVehicleCommandLog().then((result) => {
      if (result.ok) setLog(result.data.log || []);
    });
  }, []);
  const visible = commands.filter((command) => tab === "ac" ? command.category === "HVAC / AC" : tab === "drive" ? command.category === "Driving Modes" : true);
  const updateSelected = (patch) => setSelected((current) => ({ ...current, ...patch }));
  const save = async () => {
    const result = await camperAgentBridge.saveVehicleCommand(selected);
    if (result.ok) {
      setCommands(result.data.commands || []);
      setSelected((result.data.commands || []).find((command) => command.id === selected.id) || selected);
      window.dispatchEvent(new Event("camper-vehicle-commands-updated"));
    }
    setStatus(result.ok ? "Saved command" : result.error);
  };
  const test = async () => {
    const result = await camperAgentBridge.testVehicleCommand(selected);
    setStatus(result.ok ? `TX ${result.data.tx || selected.command} RX ${result.data.rx || "--"}` : result.error);
  };
  const execute = async () => {
    const result = await camperAgentBridge.executeVehicleCommand(selected.id);
    setStatus(result.ok ? `${selected.displayName}: ${result.data.statusVerified ? "verified by status PID" : "sent"}` : result.error);
  };
  return (
    <ModalFrame title="Vehicle Command Library" onClose={onClose}>
      <div className="grid h-full grid-cols-[1fr_360px] gap-4">
        <div className="min-h-0 rounded-3xl border border-white/10 bg-white/[0.04] p-4">
          <div className="mb-3 flex gap-2">
            {["ac", "drive", "custom", "log"].map((item) => <button key={item} onClick={() => setTab(item)} className={cx("rounded-xl px-3 py-2 text-xs font-black", tab === item ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-white")}>{item === "ac" ? "AC Commands" : item === "drive" ? "Drive Modes" : item === "log" ? "Execution Log" : "Custom"}</button>)}
          </div>
          <div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs font-bold text-amber-100">Only enable commands you have tested yourself. Enabled saved commands are logged before and after sending.</div>
          <div className="mt-3 max-h-[330px] space-y-2 overflow-y-auto pr-1">
            {tab === "log" ? log.map((row, index) => <div key={index} className="rounded-xl bg-black/25 p-2 font-mono text-[10px] text-slate-300">{row.timestamp} {row.commandId} TX {row.tx || "--"} RX {row.rx || "--"} {row.error || ""}</div>) : visible.map((command) => (
              <button key={command.id} onClick={() => setSelected(command)} className="grid w-full grid-cols-[1fr_70px_90px] items-center gap-2 rounded-2xl border border-white/10 bg-slate-950/55 px-3 py-2 text-left text-xs hover:bg-white/[0.08]">
                <div><div className="font-black text-white">{command.displayName}</div><div className="text-slate-400">{command.module} {command.category}</div></div>
                <div className={command.enabled ? "text-emerald-300" : "text-slate-500"}>{command.enabled ? "Enabled" : "Off"}</div>
                <div className={command.command ? "text-cyan-200" : "text-amber-200"}>{command.command ? "Command OK" : "No command"}</div>
              </button>
            ))}
          </div>
        </div>
        <div className="h-full min-h-0 overflow-y-auto rounded-3xl border border-cyan-300/20 bg-slate-950/70 p-4">
          {selected ? <div className="space-y-2 text-xs">
            <label className="flex items-center gap-2 font-bold text-white"><input type="checkbox" checked={selected.enabled} onChange={(e) => updateSelected({ enabled: e.target.checked })} /> Enabled</label>
            <div className="rounded-xl border border-amber-200/20 bg-amber-300/10 p-2 text-[11px] font-bold text-amber-100">Enabled means this saved command is permanently active until you turn it off here.</div>
            {["displayName", "category", "module", "command", "expectedPositiveResponse", "expectedStatusFunctionKey", "expectedStatusValue", "verifiedSource"].map((field) => (
              <label key={field} className="block text-slate-300">{field}<input value={selected[field] ?? ""} onChange={(e) => updateSelected({ [field]: e.target.value })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 font-mono text-white" /></label>
            ))}
            <label className="block text-slate-300">setupCommands<textarea value={(selected.setupCommands || []).join("\n")} onChange={(e) => updateSelected({ setupCommands: e.target.value.split(/\n+/).map((v) => v.trim()).filter(Boolean) })} className="mt-1 h-16 w-full rounded-xl bg-slate-900 px-3 py-2 font-mono text-white" /></label>
            <div className="grid grid-cols-2 gap-2">
              <label className="text-slate-300">Cooldown ms<input type="number" value={selected.cooldownMs ?? 1500} onChange={(e) => updateSelected({ cooldownMs: Number(e.target.value) })} className="mt-1 w-full rounded-xl bg-slate-900 px-3 py-2 text-white" /></label>
              <label className="flex items-end gap-2 pb-2 font-bold text-white"><input type="checkbox" checked={selected.requiresVehicleStopped} onChange={(e) => updateSelected({ requiresVehicleStopped: e.target.checked })} /> Stopped</label>
            </div>
            <div className="flex flex-wrap gap-2 pt-2">
              <button onClick={test} className="rounded-xl bg-white/[0.08] px-3 py-2 font-black text-white">Test</button>
              <button onClick={execute} className="rounded-xl bg-amber-300 px-3 py-2 font-black text-slate-950">Send</button>
              <button onClick={save} className="rounded-xl bg-cyan-300 px-3 py-2 font-black text-slate-950">Save</button>
            </div>
            <div className="rounded-xl border border-white/10 bg-black/25 p-2 font-mono text-[10px] text-slate-300">
              <div>loaded enabled={String(selected.enabled)}</div>
              <div>command={selected.command || "--"}</div>
              <div>eligible={String(Boolean(selected.enabled && selected.command))}</div>
            </div>
            <div className="max-h-16 overflow-auto rounded-xl bg-black/25 p-2 font-mono text-[10px] text-cyan-100">{status || "No command sent"}</div>
          </div> : <div className="flex h-full items-center justify-center text-sm text-slate-400">Select a command</div>}
        </div>
      </div>
    </ModalFrame>
  );
}
function BatteryBmsSettingsModal({ onClose }) {
  const [tab, setTab] = useState("Overview");
  const [canStatus, setCanStatus] = useState("Passive listen ready");
  const [bleStatus, setBleStatus] = useState("Not scanned");
  const bms = DEFAULT_BMS.telemetry;
  async function scanCan() {
    const result = await camperAgentBridge.startBatteryCanScan({ profileId: "battery_bms", bitrate: "Auto", readOnly: true });
    setCanStatus(result.ok ? result.data.state || "Passive listen ready" : "Error");
  }
  async function stopCan() {
    const result = await camperAgentBridge.stopBatteryCanScan();
    setCanStatus(result.ok ? result.data.state || "Stopped" : "Error");
  }
  async function scanBle() {
    const result = await camperAgentBridge.scanBatteryBluetooth();
    setBleStatus(result.ok ? result.data.state || "Discovery only" : "Error");
  }
  const tabs = ["Overview", "CAN / Victron", "Bluetooth", "Cells", "Diagnostics"];
  return (
    <ModalShell title="Battery / BMS" subtitle="12V 320Ah LiFePO4, 250A BMS, Bluetooth + CAN" icon={BatteryCharging} onClose={onClose}>
      <div className="grid h-full grid-rows-[auto_1fr] gap-4">
        <div className="flex gap-2">{tabs.map((item) => <button key={item} onClick={() => setTab(item)} className={cx("rounded-2xl px-4 py-2 text-sm font-black", tab === item ? "bg-cyan-300 text-slate-950" : "bg-white/[0.07] text-slate-300")}>{item}</button>)}</div>
        {tab === "Overview" && <div className="grid grid-cols-[330px_1fr] gap-4">
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4 text-sm text-slate-300">
            <div className="text-xl font-black text-white">PUPVWMHB 12V 320Ah LiFePO4 250A BMS</div>
            {["Capacity: 320Ah", "Nominal voltage: 12.8V", "BMS current: 250A", "Chemistry: LiFePO4", "Read-only: Active"].map((line) => <div key={line} className="mt-3">{line}</div>)}
            <div className="mt-4 rounded-2xl border border-cyan-200/20 bg-cyan-300/10 p-3 text-xs text-cyan-100">Source priority: Victron/GX CAN BMS, Direct CAN, Bluetooth BMS, Shunt/estimated, Simulator fallback.</div>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {[["SOC", `${bms.socPercent}%`], ["Voltage", `${bms.voltage}V`], ["Current", `${bms.current}A`], ["Power", `${bms.powerWatts}W`], ["Remaining", `${bms.remainingCapacityAh}Ah`], ["Charge allowed", String(bms.chargeAllowed)], ["Discharge allowed", String(bms.dischargeAllowed)], ["Warnings", "0"], ["Alarms", "0"]].map(([k, v]) => <div key={k} className="rounded-2xl border border-white/10 bg-white/[0.045] p-3"><div className="text-xs text-slate-400">{k}</div><div className="mt-2 text-lg font-black text-white">{v}</div></div>)}
          </div>
        </div>}
        {tab === "CAN / Victron" && <div className="grid grid-cols-[320px_1fr] gap-4">
          <div className="space-y-3">
            <div className="rounded-2xl border border-emerald-200/20 bg-emerald-300/10 p-3 text-sm text-emerald-100">Victron can read this BMS: user confirmed.</div>
            <SettingField label="Connection path"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>Via Victron GX / CAN BMS</option><option>Direct CAN adapter</option><option>Unknown/manual</option></select></SettingField>
            <SettingField label="CAN bitrate"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white"><option>Auto</option><option>250000</option><option>500000</option></select></SettingField>
            <SettingField label="Protocol"><select className="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm text-white">{["Auto detect", "Victron/GX mapped telemetry", "Pylontech-like", "JK BMS / JKBMS", "JBD / Xiaoxiang", "Daly", "Seplos", "Seplos v3", "Pace", "Renogy BMS", "RV-C House Battery", "EG4", "Felicity", "LiTime / Power Queen / Redodo", "Heltec / YanYang", "Valence", "ANT", "Sinowealth", "Unknown raw CAN"].map((item) => <option key={item}>{item}</option>)}</select></SettingField>
            <div className="grid grid-cols-3 gap-2">
              <button onClick={() => camperAgentBridge.listCanAdapters().then((r) => setCanStatus(r.ok ? "Adapters listed" : "Error"))} className="rounded-2xl bg-white/[0.07] px-3 py-3 text-xs font-black text-white">Scan CAN adapters</button>
              <button onClick={scanCan} className="rounded-2xl bg-cyan-300 px-3 py-3 text-xs font-black text-slate-950">Start BMS CAN scan</button>
              <button onClick={stopCan} className="rounded-2xl bg-white/[0.07] px-3 py-3 text-xs font-black text-white">Stop scan</button>
            </div>
            <div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs text-amber-100">vLinker is configured for Ford OBD. Use a USB-CAN adapter for Garmin/NMEA and BMS-CAN sniffing unless adapter supports raw passive CAN monitor safely.</div>
          </div>
          <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-3 flex justify-between"><span className="font-black text-white">Raw CAN table</span><StatusBadge value={canStatus} /></div><div className="grid grid-cols-6 gap-2 text-xs text-slate-400"><span>CAN ID</span><span>DLC</span><span>Data</span><span>Rate</span><span>Last seen</span><span>Decoded as</span></div><div className="mt-4 rounded-2xl border border-white/10 bg-black/20 p-4 text-sm text-slate-400">No frames yet. Discovery starts in passive listen mode only.</div></div>
        </div>}
        {tab === "Bluetooth" && <div className="grid grid-cols-[320px_1fr] gap-4"><div className="space-y-3"><div className="rounded-2xl border border-white/10 bg-white/[0.045] p-3 text-sm text-slate-300">Bluetooth support: available, protocol unverified.</div><button onClick={scanBle} className="w-full rounded-2xl bg-cyan-300 px-4 py-3 text-sm font-black text-slate-950">Scan BLE BMS devices</button><div className="rounded-2xl border border-amber-200/20 bg-amber-300/10 p-3 text-xs text-amber-100">Bluetooth write commands disabled. Read-only discovery only.</div></div><div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4"><div className="mb-3 flex justify-between"><span className="font-black text-white">BLE discovery</span><StatusBadge value={bleStatus} /></div>{["Device name/address list", "Connection state", "Services discovered", "Characteristics discovered", "Protocol: Unknown / future"].map((line) => <div key={line} className="mt-3 rounded-xl bg-black/20 px-3 py-2 text-sm text-slate-300">{line}</div>)}</div></div>}
        {tab === "Cells" && <div className="rounded-3xl border border-white/10 bg-white/[0.045] p-5"><div className="text-xl font-black text-white">Cell balance</div><div className="mt-3 text-sm text-slate-400">Cell data unavailable until BMS protocol is decoded.</div><div className="mt-5 grid grid-cols-8 gap-2">{Array.from({ length: 8 }).map((_, index) => <div key={index} className="h-32 rounded-2xl border border-white/10 bg-white/[0.035]" />)}</div></div>}
        {tab === "Diagnostics" && <div className="grid grid-cols-2 gap-4"><div className="rounded-3xl border border-white/10 bg-white/[0.045] p-4">{["source: simulator_fallback", "protocol: PUPVWMHB discovery pending", "last payload: none", "raw CAN frame count: 0", "BLE services count: 0", "stale age: n/a", "warnings: none", "alarms: none"].map((line) => <div key={line} className="mb-3 text-sm text-slate-300">{line}</div>)}</div><button onClick={() => camperAgentBridge.exportBatteryBmsDiagnostics().then((r) => console.log("battery bms diagnostics", r))} className="h-16 self-end rounded-2xl bg-cyan-300 px-4 py-3 text-sm font-black text-slate-950">Export diagnostics JSON</button></div>}
      </div>
    </ModalShell>
  );
}

function SystemsView({ state, setters, openIntegrationSettings }) {
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
        <div className="mt-5 rounded-3xl bg-cyan-300/10 p-4 text-sm text-cyan-100 ring-1 ring-cyan-200/20">
          Open Integration Health for live USB, OBD, remote logging and T-CAN485 status.
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
        <div className="max-h-[390px] space-y-2 overflow-y-auto pr-1">
          {[
            ["Ford OBD / vLinker", "USB vLinker, ISO15765-4 CAN11/500, PIDs, DTC read-only", "obd"],
            ["OBD PID Library", "Read PIDs, formulas, polling and troubleshooting", "obdPidMapping"],
            ["Vehicle Command Library", "User-enabled commands for AC and drive modes", "vehicleCommands"],
            ["Vehicle Dashboard", "Live gauges, vehicle display, mapped OBD values", null],
            ["T-CAN485 Gateway", "Android hotspot, UDP discovery, RS485 BMS gateway", "tcan485"],
            ["Battery / BMS", "12V 320Ah LiFePO4, 250A BMS, Bluetooth + CAN", "battery"],
            ["Victron System", "SmartSolar MPPT 100/20 and Orion-Tr 12/12V 18A", "victron"],
            ["Renogy DC/DC", "40A alternator battery charger"],
            ["Garmin / NMEA", "NMEA 2000, EmpirBus discovery, Garmin network data", "garmin"],
            ["CAN Bus Scanner", "Ford OBD, NMEA 2000, BMS-CAN profiles", "canbus"],
            ["Remote Logging / Cloudflare", "Live logs, diagnostics upload, tunnel status", "remoteLogging"],
            ["Display / Screen Fit", "Scale and vertical offset for the Android media unit", "displayFit"],
            ["Integration Health", "Adapters, permissions, last update, logs", "health"],
            ["Network", "Wi-Fi, hotspot, Cloudflare and T-CAN485 health", "network"],
            ["Service", "Logs, firmware, backup, factory reset"],
          ].map(([title, sub, modal]) => (
            <button key={title} onClick={() => modal && openIntegrationSettings(modal)} className="flex w-full items-center justify-between rounded-2xl border border-white/10 bg-white/[0.055] px-4 py-3 text-left hover:bg-white/[0.09]">
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
  const [integrationModal, setIntegrationModal] = useState(null);
  const [displayFit, setDisplayFit] = useState(() => {
    try {
      return { autoFit: true, manualScale: 1, offsetY: 0, ...JSON.parse(localStorage.getItem(DISPLAY_FIT_KEY) || "{}") };
    } catch {
      return { autoFit: true, manualScale: 1, offsetY: 0 };
    }
  });
  const [hmiScale, setHmiScale] = useState(1);

  const [battery, setBattery] = useState(87);
  const [batteryBms] = useState(DEFAULT_BMS);
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

  useEffect(() => {
    const updateScale = () => {
      const availableWidth = window.innerWidth || DESIGN_WIDTH;
      const availableHeight = window.innerHeight || DESIGN_HEIGHT;
      const heightScale = availableHeight >= DESIGN_HEIGHT ? 1 : availableHeight >= 560 ? Math.max(availableHeight / DESIGN_HEIGHT, 0.94) : Math.max(availableHeight / DESIGN_HEIGHT, MIN_AUTO_SCALE);
      const widthScale = Math.min(availableWidth / DESIGN_WIDTH, 1);
      const rawScale = Math.min(widthScale, heightScale, 1);
      const safeScale = displayFit.autoFit ? rawScale : Number(displayFit.manualScale || 1);
      setHmiScale(Math.min(1, Math.max(0.9, safeScale)));
    };
    updateScale();
    window.addEventListener("resize", updateScale);
    window.addEventListener("orientationchange", updateScale);
    return () => {
      window.removeEventListener("resize", updateScale);
      window.removeEventListener("orientationchange", updateScale);
    };
  }, [displayFit]);

  const saveDisplayFit = (next) => {
    setDisplayFit(next);
    localStorage.setItem(DISPLAY_FIT_KEY, JSON.stringify(next));
  };

  const state = {
    battery,
    batteryBms,
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
      case "vehicle":
        return <VehicleDashboardView openIntegrationSettings={setIntegrationModal} />;
      case "power":
        return <PowerView state={state} setters={setters} openEnergyStats={() => setShowEnergyStats(true)} />;
      case "climate":
        return <ClimateView state={state} setters={setters} />;
      case "water":
        return <WaterView state={state} setters={setters} />;
      case "lights":
        return <LightsView state={state} setters={setters} />;
      case "systems":
        return <SystemsView state={state} setters={setters} openIntegrationSettings={setIntegrationModal} />;
      default:
        return <HomeView state={state} setters={setters} openEnergyStats={() => setShowEnergyStats(true)} />;
    }
  }, [activeTab, state, setters]);

  return (
    <div className="fixed inset-0 h-screen w-screen overflow-hidden bg-slate-950 text-slate-100">
      <div style={{ width: DESIGN_WIDTH, height: DESIGN_HEIGHT, transform: `translateX(-50%) translateY(${Number(displayFit.offsetY || 0)}px) scale(${hmiScale})`, transformOrigin: "top center" }} className="absolute left-1/2 top-0 overflow-hidden rounded-[2.25rem] border border-white/10 bg-[radial-gradient(circle_at_top_left,rgba(34,211,238,0.16),transparent_30%),radial-gradient(circle_at_bottom_right,rgba(168,85,247,0.18),transparent_35%),linear-gradient(135deg,#020617,#0f172a_45%,#020617)] shadow-2xl shadow-black">
        <div
          className="pointer-events-none absolute inset-0 bg-cover bg-center opacity-70"
          style={{ backgroundImage: `url(${VAN_BACKGROUND_URL})` }}
        />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/50 to-slate-950/80" />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-b from-slate-950/45 via-transparent to-slate-950/80" />
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_58%_28%,rgba(14,165,233,0.08),transparent_28%),radial-gradient(circle_at_18%_35%,rgba(0,0,0,0.28),transparent_34%)]" />
        <div className="relative z-10 h-full">
        {activeTab !== "vehicle" && <TopBar activeScene={activeScene} setActiveScene={setActiveScene} />}
        <div className={cx("flex border-t border-white/10", activeTab === "vehicle" ? "h-full" : "h-[526px]")}>
          <SideNav activeTab={activeTab} setActiveTab={setActiveTab} />
          <main className={cx("h-full flex-1", activeTab === "vehicle" ? "p-2" : "p-4")}>
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
        <AnimatePresence>
          {(integrationModal?.type || integrationModal) === "battery" && <BatteryBmsSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "canbus" && <CanBusScannerModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "remoteLogging" && <RemoteLoggingSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "tcan485" && <Tcan485GatewayModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "victron" && <VictronSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "garmin" && <GarminSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "obd" && <ObdSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "obdPidMapping" && <ObdPidMappingSettingsModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "vehicleCommands" && <VehicleCommandLibraryModal initialTab={integrationModal?.initialTab || "ac"} onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "displayFit" && <DisplayFitSettingsModal settings={displayFit} scale={hmiScale} onSave={saveDisplayFit} onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "health" && <IntegrationsHealthModal onClose={() => setIntegrationModal(null)} />}
          {(integrationModal?.type || integrationModal) === "network" && <NetworkSettingsModal onClose={() => setIntegrationModal(null)} />}
        </AnimatePresence>
        </div>
      </div>
    </div>
  );
}
