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
