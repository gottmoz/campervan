function defaultPidMappings() {
  return [
    { functionKey: "rpm", label: "RPM", enabled: true, mode: "Standard OBD", service: "01", pid: "0C", formula: "((A*256)+B)/4", unit: "rpm", min: 0, max: 5000, pollIntervalMs: 700, timeoutMs: 2000, decimals: 0 },
    { functionKey: "speedKph", label: "Speed", enabled: true, mode: "Standard OBD", service: "01", pid: "0D", formula: "A", unit: "km/h", min: 0, max: 200, pollIntervalMs: 700, timeoutMs: 2000, decimals: 0 },
    { functionKey: "coolantTempC", label: "Coolant temp", enabled: true, mode: "Ford PID", service: "22", pid: "F405", formula: "A-40", unit: "degC", category: "Main Dashboard", module: "PCM", setupCommands: ["ATSH7E0"], min: 40, max: 120, pollIntervalMs: 2000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "oilTempC", label: "Oil temp", enabled: false, mode: "Standard OBD", service: "01", pid: "5C", formula: "A-40", unit: "degC", min: 40, max: 130, pollIntervalMs: 3000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "outsideTempC", label: "Outside temp", enabled: true, mode: "Ford PID", service: "22", pid: "057D", formula: "A-40", unit: "degC", category: "Main Dashboard", module: "PCM", setupCommands: ["ATSH7E0"], min: -30, max: 50, pollIntervalMs: 3000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "acCompressorStatus", label: "AC compressor status", enabled: true, mode: "Ford PID", service: "22", pid: "099B", formula: "A", unit: "enum", category: "HVAC / AC", module: "PCM", setupCommands: ["ATSH7E0"], min: 0, max: 1, pollIntervalMs: 2000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "driveMode", label: "Drive mode", enabled: true, mode: "Ford PID", service: "22", pid: "0651", formula: "A", unit: "enum", category: "Driving Modes", module: "PCM", setupCommands: ["ATSH7E0"], pollIntervalMs: 2000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "alternatorDutyPercent", label: "Alternator duty", enabled: true, mode: "Ford PID", service: "22", pid: "0598", formula: "A", unit: "%", category: "Charging / Electrical", module: "PCM", setupCommands: ["ATSH7E0"], min: 0, max: 100, pollIntervalMs: 3000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "generatorCurrentA", label: "Generator / battery current", enabled: true, mode: "Ford PID", service: "22", pid: "402B", formula: "((A*256)+B)", unit: "A", category: "Charging / Electrical", module: "BCM", setupCommands: ["ATSH000726", "STCAFCP726,72E"], pollIntervalMs: 3000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "vehicleBatteryVoltage", label: "Vehicle battery voltage", enabled: true, mode: "Ford PID", service: "22", pid: "402A", formula: "((A*256)+B)/1000", unit: "V", category: "Charging / Electrical", module: "BCM", setupCommands: ["ATSH000726", "STCAFCP726,72E"], min: 10, max: 16, pollIntervalMs: 3000, timeoutMs: 2000, decimals: 1 },
    { functionKey: "intakeTempC", label: "Intake temp", enabled: true, mode: "Standard OBD", service: "01", pid: "0F", formula: "A-40", unit: "degC", min: 40, max: 120, pollIntervalMs: 2000, timeoutMs: 2000, decimals: 0 },
    { functionKey: "moduleVoltage", label: "Module voltage", enabled: true, mode: "Standard OBD", service: "01", pid: "42", formula: "((A*256)+B)/1000", unit: "V", min: 11, max: 15.5, pollIntervalMs: 3000, timeoutMs: 2000, decimals: 1 },
    { functionKey: "mafGps", label: "MAF", enabled: true, mode: "Standard OBD", service: "01", pid: "10", formula: "((A*256)+B)/100", unit: "g/s", min: 0, max: 100, pollIntervalMs: 2000, timeoutMs: 2000, decimals: 1 },
    { functionKey: "throttlePercent", label: "Throttle", enabled: true, mode: "Standard OBD", service: "01", pid: "11", formula: "A*100/255", unit: "%", min: 0, max: 100, pollIntervalMs: 1500, timeoutMs: 2000, decimals: 0 },
    { functionKey: "engineLoadPercent", label: "Engine load", enabled: true, mode: "Standard OBD", service: "01", pid: "04", formula: "A*100/255", unit: "%", min: 0, max: 100, pollIntervalMs: 1500, timeoutMs: 2000, decimals: 0 },
  ];
}

function defaultVehicleCommands() {
  return [
    { id: "ac_on", enabled: false, displayName: "AC Compressor ON", description: "Paste FORScan-tested AC ON command.", category: "HVAC / AC", module: "PCM", setupCommands: ["ATSH7E0"], command: "", expectedPositiveResponse: "", verifiedByUser: false, verifiedSource: "FORScan", expectedStatusFunctionKey: "acCompressorStatus", expectedStatusValue: "01", cooldownMs: 1500, confirmBeforeSend: true },
    { id: "ac_off", enabled: false, displayName: "AC Compressor OFF", description: "Paste FORScan-tested AC OFF command.", category: "HVAC / AC", module: "PCM", setupCommands: ["ATSH7E0"], command: "", expectedPositiveResponse: "", verifiedByUser: false, verifiedSource: "FORScan", expectedStatusFunctionKey: "acCompressorStatus", expectedStatusValue: "00", cooldownMs: 1500, confirmBeforeSend: true },
    ...[
      ["drive_mode_normal", "Normal", "00"],
      ["drive_mode_eco", "Eco", "06"],
      ["drive_mode_slippery", "Slippery", "05"],
      ["drive_mode_mud_ruts", "Mud & Ruts", "08"],
      ["drive_mode_tow_haul", "Tow / Haul", "03"],
    ].map(([id, displayName, expectedStatusValue]) => ({ id, enabled: false, displayName, description: `Paste FORScan-tested ${displayName} drive mode command.`, category: "Driving Modes", module: "PCM", setupCommands: ["ATSH7E0"], command: "", expectedPositiveResponse: "", verifiedByUser: false, verifiedSource: "FORScan", expectedStatusFunctionKey: "driveMode", expectedStatusValue, cooldownMs: 1500, confirmBeforeSend: true })),
  ];
}

const simulator = {
  remoteUrl: "https://sometimes-women-supported-writings.trycloudflare.com",
  getIntegrationSnapshot: () => ({
    mode: "simulated",
    readOnly: true,
    health: [
      { id: "victron", label: "Victron", state: "Disconnected", readOnly: true, source: "simulator" },
      { id: "garmin", label: "Garmin/NMEA", state: "Disconnected", readOnly: true, source: "simulator" },
      { id: "obd", label: "Ford OBD", state: "PermissionRequired", readOnly: true, source: "simulator" },
      { id: "usb", label: "USB subsystem", state: "Disconnected", readOnly: true, source: "simulator" },
    ],
  }),
  getBatteryBmsSnapshot: () => ({
    profile: {
      id: "pupvwmhb_lifepo4_12v_320ah_250a",
      displayName: "PUPVWMHB 12V 320Ah LiFePO4 250A BMS",
      brand: "PUPVWMHB",
      chemistry: "LiFePO4",
      nominalVoltage: 12.8,
      capacityAh: 320,
      bmsContinuousCurrentAmp: 250,
    },
    telemetry: {
      socPercent: 87,
      voltage: 13.2,
      current: -18,
      powerWatts: -238,
      remainingCapacityAh: 278.4,
      chargeAllowed: true,
      dischargeAllowed: true,
      warnings: [],
      alarms: [],
      source: "simulator_fallback",
      protocol: "PUPVWMHB discovery pending",
    },
    readOnly: true,
  }),
  getBatteryBmsSettings: () => ({ enabled: true, profileId: "pupvwmhb_lifepo4_12v_320ah_250a", connectionPath: "VictronCanViaGx", canBitrate: "Auto", protocol: "Auto detect", readOnly: true }),
  saveBatteryBmsSettings: (json) => ({ ...json, readOnly: true }),
  getVictronSettings: () => ({ enabled: false, mode: "GxLan", host: "", modbusPort: 502, mqttPort: 1883, readOnly: true }),
  saveVictronSettings: (json) => ({ ...json, readOnly: true }),
  getGarminSettings: () => ({ enabled: false, mode: "Nmea2000Can", canBitrate: 250000, readOnly: true }),
  saveGarminSettings: (json) => ({ ...json, readOnly: true }),
  getObdSettings: () => ({ enabled: false, adapterType: "Auto", baudRate: null, protocol: "AUTO", readOnly: true }),
  saveObdSettings: (json) => ({ ...json, readOnly: true }),
  getObdPidMappings: () => ({ profile: "Ford Transit EcoBlue 2.0 2016 - Default", mappings: defaultPidMappings(), readOnly: true }),
  saveObdPidMappings: (json) => {
    localStorage.setItem("camper_obd_pid_mappings", JSON.stringify(json.mappings || []));
    return { profile: "Ford Transit EcoBlue 2.0 2016 - Default", mappings: json.mappings || [], readOnly: true };
  },
  resetObdPidMappingsToDefault: () => ({ profile: "Ford Transit EcoBlue 2.0 2016 - Default", mappings: defaultPidMappings(), readOnly: true }),
  testObdPidMapping: (mapping) => ({ tx: `${mapping.service || "01"}${mapping.pid || ""}`, rx: "simulated", decoded: { value: null, unit: mapping.unit }, readOnly: true }),
  getObdPidMappingStatus: () => ({ mappings: defaultPidMappings().filter((row) => row.enabled), lastValues: {}, pidErrorCounts: {}, pausedMs: {}, readOnly: true }),
  getVehicleCommands: () => ({ profileName: "Ford Transit FORScan verified commands", commands: JSON.parse(localStorage.getItem("camper_vehicle_commands") || "null") || defaultVehicleCommands() }),
  saveVehicleCommands: (json) => {
    localStorage.setItem("camper_vehicle_commands", JSON.stringify(json.commands || []));
    return { profileName: "Ford Transit FORScan verified commands", commands: json.commands || [] };
  },
  executeVehicleCommand: (commandId) => ({ commandId, error: "Command execution unavailable in desktop simulator" }),
  testVehicleCommand: (command) => ({ commandId: command.id, tx: command.command, rx: "simulated", statusVerified: false }),
  getVehicleCommandLog: () => ({ log: [] }),
  exportVehicleCommands: () => ({ profileName: "Ford Transit FORScan verified commands", createdAt: new Date().toISOString(), commands: defaultVehicleCommands() }),
  importVehicleCommands: (json) => simulator.saveVehicleCommands(json),
  requestUsbPermission: (kind) => ({ kind, status: "simulated" }),
  scanUsbSerialDevices: () => ({ devices: [], status: { state: "NoDevice", permissionGranted: false, open: false } }),
  getUsbPermissionStatus: () => ({ state: "NoDevice", permissionGranted: false, open: false }),
  openUsbSerial: (json) => ({ ...json, state: "Simulated" }),
  closeUsbSerial: () => ({ state: "Disconnected" }),
  testVictronConnection: () => ({ state: "Offline", readOnly: true }),
  getVictronSnapshot: () => ({ dbusPaths: ["/Dc/Battery/Soc", "/Dc/Battery/Voltage", "/Dc/Pv/Power"], readOnly: true }),
  testObdConnection: () => ({ state: "PermissionRequired", readOnly: true }),
  connectObd: (json) => ({ state: "PermissionRequired", connected: false, verified: false, protocol: json.protocol || "ISO15765-4 CAN 11/500", elmProtocol: "6", readOnly: true }),
  disconnectObd: () => ({ state: "Disconnected", readOnly: true }),
  getObdConnectionStatus: () => ({ state: "NoDevice", protocol: "ISO15765-4 CAN 11/500", elmProtocol: "6", readOnly: true }),
  getVehicleTelemetrySnapshot: () => {
    const t = Date.now() / 1000;
    const rpm = Math.round(1450 + Math.sin(t * 1.3) * 420);
    const speedKph = Math.max(0, Math.round(72 + Math.sin(t * 0.45) * 18));
    return {
      connected: true,
      verified: true,
      polling: true,
      stale: false,
      lastSuccessEpochMs: Date.now(),
      adapterName: "ELM327 v2.3",
      adapterDescription: "OBDII to RS232 Interpreter",
      protocol: "ISO 15765-4 (CAN 11/500)",
      elmProtocol: "6",
      telemetry: {
        rpm,
        speedKph,
        coolantTempC: Math.round(86 + Math.sin(t * 0.08) * 3),
        oilTempC: 92,
        outsideTempC: 16,
        intakeTempC: Math.round(24 + Math.sin(t * 0.15) * 5),
        moduleVoltage: Math.round((14.1 + Math.sin(t * 0.4) * 0.12) * 10) / 10,
        mafGps: Math.round((18 + Math.sin(t * 0.8) * 6) * 10) / 10,
        throttlePercent: Math.round(22 + Math.sin(t * 1.1) * 9),
        ambientTempC: 16,
        engineLoadPercent: Math.round(38 + Math.sin(t * 0.7) * 12),
        acCompressorStatus: Math.sin(t * 0.05) > 0 ? 1 : 0,
        driveMode: 0,
        alternatorDutyPercent: Math.round(42 + Math.sin(t * 0.2) * 8),
        generatorCurrentA: Math.round(18 + Math.sin(t * 0.25) * 6),
        vehicleBatteryVoltage: Math.round((14.2 + Math.sin(t * 0.3) * 0.15) * 10) / 10,
        boostBar: null,
        egtTempC: null,
        dpfSootPercent: null,
        source: "simulator",
      },
      supportedPids: ["0C", "0D", "05", "0F", "10", "11", "42"],
      pidErrorCounts: { "0C": 0, "0D": 0, "05": 0 },
      lastError: null,
    };
  },
  sendReadOnlyObdCommand: (command) => ({ command, queued: false, readOnly: true }),
  scanSupportedPids: () => ({ commands: ["0100", "0120", "0140", "0160"], readOnly: true }),
  readDtcReadOnly: () => ({ command: "03", dtcs: [], readOnly: true }),
  startElmMonitorReadOnly: () => ({ state: "ExperimentalMonitorUnavailable", readOnly: true }),
  stopElmMonitorReadOnly: () => ({ state: "ElmMonitorStopped", readOnly: true }),
  listCanAdapters: () => ({ profiles: [], readOnly: true }),
  startCanScan: (json) => ({ state: "PassiveScanStarted", profile: json.profileId, readOnly: true }),
  stopCanScan: () => ({ state: "Stopped", readOnly: true }),
  getCanScanSnapshot: () => ({ frames: [], readOnly: true }),
  scanNmeaBus: () => ({ state: "Simulated", pgnCount: 3, readOnly: true }),
  exportIntegrationDiagnostics: () => ({ exportedAt: new Date().toISOString(), mode: "simulated" }),
  getRemoteLoggingSettings: () => ({ enabled: true, serverUrl: simulator.remoteUrl }),
  startTcan485Discovery: () => ({ state: "Listening", discoveryRunning: true, port: 47887, beacon: null, fallbackUrls: ["http://camper-tcan485.local", "http://192.168.4.1"], readOnly: true }),
  stopTcan485Discovery: () => ({ state: "Stopped", discoveryRunning: false, port: 47887, beacon: null, readOnly: true }),
  getTcan485DiscoverySnapshot: () => ({ discoveryRunning: false, port: 47887, beacon: null, fallbackUrls: ["http://camper-tcan485.local", "http://192.168.4.1"], readOnly: true }),
  getTcan485Settings: () => ({ enabled: true, networkMode: "sta_android_hotspot", baseUrl: "http://192.168.4.1", hostname: "camper-tcan485", readOnly: true }),
  saveTcan485Settings: (json) => ({ ...json, readOnly: true }),
  testTcan485Health: async (baseUrl) => {
    const response = await fetch(`${baseUrl.replace(/\/$/, "")}/health`);
    return { baseUrl, health: await response.json(), readOnly: true };
  },
  getTcan485GatewayStatus: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/gateway/status`)).json(),
  getTcan485Rs485Status: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/rs485/status`)).json(),
  getTcan485BmsLatest: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/bms/latest`)).json(),
  getTcan485Rs485RawLatest: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/rs485/raw/latest?limit=50`)).json(),
  getTcan485CanStatus: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/can/status`)).json(),
  getTcan485CanFramesLatest: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/can/frames/latest?limit=100`)).json(),
  saveTcan485WifiSettings: async (baseUrl, json) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/settings/wifi`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(json) })).json(),
  saveTcan485CanSettings: async (baseUrl, json) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/settings/can`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(json) })).json(),
  saveTcan485Rs485Settings: async (baseUrl, json) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/settings/rs485`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(json) })).json(),
  rebootTcan485: async (baseUrl) => (await fetch(`${baseUrl.replace(/\/$/, "")}/api/reboot`, { method: "POST" })).json(),
  openAndroidHotspotSettings: () => ({ opened: false, target: "Android settings unavailable in desktop mode" }),
  saveRemoteLoggingSettings: (json) => {
    simulator.remoteUrl = json.serverUrl || simulator.remoteUrl;
    return { enabled: json.enabled !== false, serverUrl: simulator.remoteUrl };
  },
  testRemoteLoggingServer: async () => {
    const response = await fetch(`${simulator.remoteUrl}/health`);
    return response.json();
  },
  uploadDiagnosticsNow: async () => {
    const response = await fetch(`${simulator.remoteUrl}/api/diagnostics/upload`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ timestamp: new Date().toISOString(), source: "hmi-desktop", mode: "simulated" }),
    });
    return response.json();
  },
  getRemoteRuntimeStatus: async () => {
    const response = await fetch(`${simulator.remoteUrl}/api/runtime/status`);
    return response.json();
  },
  fetchLatestRemoteLogs: async () => {
    const response = await fetch(`${simulator.remoteUrl}/api/logs/latest`);
    return response.json();
  },
  scanBatteryCan: () => ({ state: "PassiveListenReady", frames: [], readOnly: true }),
  startBatteryCanScan: (json) => ({ state: "PassiveScanStarted", profile: json.profileId || "battery_bms", readOnly: true }),
  stopBatteryCanScan: () => ({ state: "Stopped", readOnly: true }),
  scanBatteryBluetooth: () => ({ state: "DiscoveryOnly", devices: [], readOnly: true }),
  startBatteryBluetoothScan: () => ({ state: "DiscoveryOnly", devices: [], readOnly: true }),
  stopBatteryBluetoothScan: () => ({ state: "Stopped", readOnly: true }),
  exportBatteryBmsDiagnostics: () => simulator.getBatteryBmsSnapshot(),
  startNmea2000Scan: (json) => ({ state: "PassiveScanStarted", profile: json.profileId || "nmea2000", bitrate: 250000, readOnly: true }),
  stopNmea2000Scan: () => ({ state: "Stopped", readOnly: true }),
  getNmea2000Snapshot: () => ({ frames: [], readOnly: true }),
};

async function nativeCall(method, ...args) {
  const native = globalThis.window?.CamperAgent;
  if (!native || typeof native[method] !== "function") {
    return { ok: true, data: await simulator[method](...args) };
  }
  try {
    const encodedArgs = args.map((arg) => (typeof arg === "string" ? arg : JSON.stringify(arg)));
    return JSON.parse(native[method](...encodedArgs));
  } catch (error) {
    return { ok: false, error: error?.message || "Native bridge call failed" };
  }
}

async function call(method, ...args) {
  return nativeCall(method, ...args);
}

export const camperAgentBridge = {
  getIntegrationSnapshot: () => call("getIntegrationSnapshot"),
  getBatteryBmsSnapshot: () => call("getBatteryBmsSnapshot"),
  getBatteryBmsSettings: () => call("getBatteryBmsSettings"),
  saveBatteryBmsSettings: (settings) => call("saveBatteryBmsSettings", { ...settings, readOnly: true }),
  getVictronSettings: () => call("getVictronSettings"),
  saveVictronSettings: (settings) => call("saveVictronSettings", { ...settings, readOnly: true }),
  getGarminSettings: () => call("getGarminSettings"),
  saveGarminSettings: (settings) => call("saveGarminSettings", { ...settings, readOnly: true }),
  getObdSettings: () => call("getObdSettings"),
  saveObdSettings: (settings) => call("saveObdSettings", { ...settings, readOnly: true }),
  getObdPidMappings: () => call("getObdPidMappings"),
  saveObdPidMappings: (settings) => call("saveObdPidMappings", settings),
  resetObdPidMappingsToDefault: () => call("resetObdPidMappingsToDefault"),
  testObdPidMapping: (mapping) => call("testObdPidMapping", mapping),
  getObdPidMappingStatus: () => call("getObdPidMappingStatus"),
  getVehicleCommands: () => call("getVehicleCommands"),
  saveVehicleCommands: (settings) => call("saveVehicleCommands", settings),
  executeVehicleCommand: (commandId) => call("executeVehicleCommand", commandId),
  testVehicleCommand: (command) => call("testVehicleCommand", command),
  getVehicleCommandLog: () => call("getVehicleCommandLog"),
  exportVehicleCommands: () => call("exportVehicleCommands"),
  importVehicleCommands: (settings) => call("importVehicleCommands", settings),
  requestUsbPermission: (kind) => call("requestUsbPermission", kind),
  scanUsbSerialDevices: () => call("scanUsbSerialDevices"),
  getUsbPermissionStatus: () => call("getUsbPermissionStatus"),
  openUsbSerial: (settings) => call("openUsbSerial", settings),
  closeUsbSerial: () => call("closeUsbSerial"),
  testVictronConnection: () => call("testVictronConnection"),
  getVictronSnapshot: () => call("getVictronSnapshot"),
  testObdConnection: () => call("testObdConnection"),
  connectObd: (settings) => call("connectObd", settings),
  disconnectObd: () => call("disconnectObd"),
  getObdConnectionStatus: () => call("getObdConnectionStatus"),
  getVehicleTelemetrySnapshot: () => call("getVehicleTelemetrySnapshot"),
  sendReadOnlyObdCommand: (command) => call("sendReadOnlyObdCommand", command),
  scanSupportedPids: () => call("scanSupportedPids"),
  readDtcReadOnly: () => call("readDtcReadOnly"),
  startElmMonitorReadOnly: () => call("startElmMonitorReadOnly"),
  stopElmMonitorReadOnly: () => call("stopElmMonitorReadOnly"),
  listCanAdapters: () => call("listCanAdapters"),
  startCanScan: (profile) => call("startCanScan", profile),
  stopCanScan: () => call("stopCanScan"),
  getCanScanSnapshot: () => call("getCanScanSnapshot"),
  scanNmeaBus: () => call("scanNmeaBus"),
  exportIntegrationDiagnostics: () => call("exportIntegrationDiagnostics"),
  getRemoteLoggingSettings: () => call("getRemoteLoggingSettings"),
  startTcan485Discovery: () => call("startTcan485Discovery"),
  stopTcan485Discovery: () => call("stopTcan485Discovery"),
  getTcan485DiscoverySnapshot: () => call("getTcan485DiscoverySnapshot"),
  getTcan485Settings: () => call("getTcan485Settings"),
  saveTcan485Settings: (settings) => call("saveTcan485Settings", settings),
  testTcan485Health: (baseUrl) => call("testTcan485Health", baseUrl),
  getTcan485GatewayStatus: (baseUrl) => call("getTcan485GatewayStatus", baseUrl),
  getTcan485Rs485Status: (baseUrl) => call("getTcan485Rs485Status", baseUrl),
  getTcan485BmsLatest: (baseUrl) => call("getTcan485BmsLatest", baseUrl),
  getTcan485Rs485RawLatest: (baseUrl) => call("getTcan485Rs485RawLatest", baseUrl),
  getTcan485CanStatus: (baseUrl) => call("getTcan485CanStatus", baseUrl),
  getTcan485CanFramesLatest: (baseUrl) => call("getTcan485CanFramesLatest", baseUrl),
  saveTcan485WifiSettings: (baseUrl, settings) => call("saveTcan485WifiSettings", baseUrl, settings),
  saveTcan485CanSettings: (baseUrl, settings) => call("saveTcan485CanSettings", baseUrl, settings),
  saveTcan485Rs485Settings: (baseUrl, settings) => call("saveTcan485Rs485Settings", baseUrl, settings),
  rebootTcan485: (baseUrl) => call("rebootTcan485", baseUrl),
  openAndroidHotspotSettings: () => call("openAndroidHotspotSettings"),
  saveRemoteLoggingSettings: (settings) => call("saveRemoteLoggingSettings", settings),
  testRemoteLoggingServer: () => call("testRemoteLoggingServer"),
  uploadDiagnosticsNow: () => call("uploadDiagnosticsNow"),
  getRemoteRuntimeStatus: () => call("getRemoteRuntimeStatus"),
  fetchLatestRemoteLogs: () => call("fetchLatestRemoteLogs"),
  scanBatteryCan: () => call("scanBatteryCan"),
  startBatteryCanScan: (profile) => call("startBatteryCanScan", profile),
  stopBatteryCanScan: () => call("stopBatteryCanScan"),
  scanBatteryBluetooth: () => call("scanBatteryBluetooth"),
  startBatteryBluetoothScan: () => call("startBatteryBluetoothScan"),
  stopBatteryBluetoothScan: () => call("stopBatteryBluetoothScan"),
  exportBatteryBmsDiagnostics: () => call("exportBatteryBmsDiagnostics"),
  startNmea2000Scan: (profile) => call("startNmea2000Scan", profile),
  stopNmea2000Scan: () => call("stopNmea2000Scan"),
  getNmea2000Snapshot: () => call("getNmea2000Snapshot"),
};
