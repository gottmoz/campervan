const simulator = {
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
  requestUsbPermission: (kind) => ({ kind, status: "simulated" }),
  testVictronConnection: () => ({ state: "Offline", readOnly: true }),
  testObdConnection: () => ({ state: "PermissionRequired", readOnly: true }),
  scanNmeaBus: () => ({ state: "Simulated", pgnCount: 3, readOnly: true }),
  exportIntegrationDiagnostics: () => ({ exportedAt: new Date().toISOString(), mode: "simulated" }),
  scanBatteryCan: () => ({ state: "PassiveListenReady", frames: [], readOnly: true }),
  scanBatteryBluetooth: () => ({ state: "DiscoveryOnly", devices: [], readOnly: true }),
  exportBatteryBmsDiagnostics: () => simulator.getBatteryBmsSnapshot(),
};

function nativeCall(method, ...args) {
  const native = globalThis.window?.CamperAgent;
  if (!native || typeof native[method] !== "function") {
    return { ok: true, data: simulator[method](...args) };
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
  requestUsbPermission: (kind) => call("requestUsbPermission", kind),
  testVictronConnection: () => call("testVictronConnection"),
  testObdConnection: () => call("testObdConnection"),
  scanNmeaBus: () => call("scanNmeaBus"),
  exportIntegrationDiagnostics: () => call("exportIntegrationDiagnostics"),
  scanBatteryCan: () => call("scanBatteryCan"),
  scanBatteryBluetooth: () => call("scanBatteryBluetooth"),
  exportBatteryBmsDiagnostics: () => call("exportBatteryBmsDiagnostics"),
};
