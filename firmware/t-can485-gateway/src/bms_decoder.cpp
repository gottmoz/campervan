#include "bms_decoder.h"
#include "debug_log.h"
#include <ArduinoJson.h>

static BmsTelemetry telemetry;
static uint32_t rawFrames = 0;

static void putNullable(JsonObject obj, const char* key, float value) {
  if (isnan(value)) obj[key] = nullptr;
  else obj[key] = value;
}

void bmsDecoderBegin() {
  telemetry.valid = false;
  telemetry.capacityAh = 320.0f;
  logLine(LogLevel::INFO, "BMS", "Decoder ready protocol=AutoDetect readOnly=true");
}

void bmsDecoderOnRs485Frame(const uint8_t* data, size_t len) {
  rawFrames++;
  telemetry.updatedMs = millis();
  telemetry.source = "rs485";
  telemetry.protocol = "unknown_raw";
  telemetry.valid = false;
  if (len >= 2 && data[0] == 0xDD) {
    telemetry.protocol = "jbd_xiaoxiang_candidate";
    logLine(LogLevel::INFO, "BMS", "JBD/Xiaoxiang candidate frame seen");
  }
}

BmsTelemetry getBmsTelemetry() { return telemetry; }

String getBmsTelemetryJson() {
  JsonDocument doc;
  doc["ok"] = true;
  JsonObject data = doc["data"].to<JsonObject>();
  data["valid"] = telemetry.valid;
  data["source"] = telemetry.source;
  data["protocol"] = telemetry.protocol;
  putNullable(data, "socPercent", telemetry.socPercent);
  putNullable(data, "voltage", telemetry.voltage);
  putNullable(data, "current", telemetry.current);
  putNullable(data, "powerWatts", telemetry.powerWatts);
  putNullable(data, "remainingAh", telemetry.remainingAh);
  data["capacityAh"] = telemetry.capacityAh;
  putNullable(data, "minCellVoltage", telemetry.minCellVoltage);
  putNullable(data, "maxCellVoltage", telemetry.maxCellVoltage);
  putNullable(data, "cellDeltaMv", telemetry.cellDeltaMv);
  data["chargeAllowed"] = telemetry.chargeAllowed;
  data["dischargeAllowed"] = telemetry.dischargeAllowed;
  data["alarms"] = telemetry.alarms;
  data["warnings"] = telemetry.warnings;
  data["updatedMs"] = telemetry.updatedMs;
  String out;
  serializeJson(doc, out);
  return out;
}

String getBmsRawDecodeStatusJson() {
  JsonDocument doc;
  doc["ok"] = true;
  doc["rawFrames"] = rawFrames;
  doc["protocol"] = telemetry.protocol;
  doc["valid"] = telemetry.valid;
  String out;
  serializeJson(doc, out);
  return out;
}

void startRs485BmsScan() {
  logLine(LogLevel::INFO, "BMS", "RS485 scan requested. Passive first; no write probes in phase 1.");
}
