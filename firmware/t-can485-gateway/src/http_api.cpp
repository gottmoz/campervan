#include "http_api.h"
#include "config.h"
#include "debug_log.h"
#include "wifi_manager.h"
#include "rs485_bus.h"
#include "bms_decoder.h"
#include "can_gateway.h"
#include <WebServer.h>
#include <ArduinoJson.h>

static WebServer server(HTTP_PORT);

static size_t limitArg(size_t fallback) {
  if (!server.hasArg("limit")) return fallback;
  int value = server.arg("limit").toInt();
  return value > 0 ? min(static_cast<size_t>(value), static_cast<size_t>(500)) : fallback;
}

static void cors() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
}

static void sendJson(int code, const String& body) {
  cors();
  server.send(code, "application/json", body);
}

static void logRequest() {
  if (DEBUG_HTTP) logLine(LogLevel::INFO, "HTTP", String(server.method() == HTTP_GET ? "GET " : "POST ") + server.uri() + " from " + server.client().remoteIP().toString());
}

static String healthJson() {
  JsonDocument doc;
  doc["ok"] = true;
  doc["device"] = FW_NAME;
  doc["version"] = FW_VERSION;
  doc["uptimeMs"] = millis();
  doc["ip"] = wifiIpString();
  doc["wifiMode"] = wifiModeString();
  doc["rs485Online"] = getRs485Status().online;
  doc["canRunning"] = getCanStatus().running;
  String out;
  serializeJson(doc, out);
  return out;
}

static String gatewayStatusJson() {
  JsonDocument doc;
  doc["ok"] = true;
  JsonObject data = doc["data"].to<JsonObject>();
  data["device"] = FW_NAME;
  data["version"] = FW_VERSION;
  data["uptimeMs"] = millis();
  JsonDocument wifiDoc;
  deserializeJson(wifiDoc, wifiStatusJson());
  data["wifi"] = wifiDoc.as<JsonObject>();
  JsonDocument rsDoc;
  deserializeJson(rsDoc, getRs485StatusJson());
  data["rs485"] = rsDoc["data"];
  JsonDocument canDoc;
  deserializeJson(canDoc, getCanStatusJson());
  data["can"] = canDoc["data"];
  String out;
  serializeJson(doc, out);
  return out;
}

static String buildJson() {
  JsonDocument doc;
  doc["ok"] = true;
  JsonObject data = doc["data"].to<JsonObject>();
  data["firmware"] = FW_NAME;
  data["version"] = FW_VERSION;
  data["buildDate"] = __DATE__;
  data["buildTime"] = __TIME__;
  JsonObject pins = data["pins"].to<JsonObject>();
  pins["CAN_TX"] = PIN_CAN_TX;
  pins["CAN_RX"] = PIN_CAN_RX;
  pins["RS485_TX"] = PIN_RS485_TX;
  pins["RS485_RX"] = PIN_RS485_RX;
  pins["RS485_EN"] = PIN_RS485_EN;
  pins["RS485_CALLBACK"] = PIN_RS485_CALLBACK;
  pins["WS2812"] = PIN_WS2812;
  pins["ME2107_EN"] = PIN_ME2107_EN;
  JsonObject safety = data["safety"].to<JsonObject>();
  safety["CAN_TX_ENABLED_PHASE1"] = CAN_TX_ENABLED_PHASE1;
  safety["BMS_WRITE_ENABLED_PHASE1"] = BMS_WRITE_ENABLED_PHASE1;
  String out;
  serializeJson(doc, out);
  return out;
}

static void handleWifiSettings() {
  JsonDocument doc;
  DeserializationError err = deserializeJson(doc, server.arg("plain"));
  if (err) {
    sendJson(400, "{\"ok\":false,\"error\":\"invalid json\"}");
    return;
  }
  saveWifiSettings(
    doc["wifiMode"] | "sta_android_hotspot",
    doc["ssid"] | "",
    doc["password"] | "",
    doc["hostname"] | DEFAULT_HOSTNAME,
    doc["fallbackApEnabled"] | true
  );
  sendJson(200, "{\"ok\":true,\"data\":{\"saved\":true,\"password\":\"[redacted]\"}}");
}

static void handleCanSettings() {
  JsonDocument doc;
  deserializeJson(doc, server.arg("plain"));
  String profileText = doc["profile"] | "disabled";
  uint32_t bitrate = doc["bitrate"] | 250000;
  CanProfile profile = CanProfile::Disabled;
  if (profileText == "battery_bms_can") profile = CanProfile::BatteryBmsCan;
  else if (profileText == "garmin_nmea2000") profile = CanProfile::GarminNmea2000;
  else if (profileText == "ford_can") profile = CanProfile::FordCan;
  else if (profileText == "raw_can") profile = CanProfile::RawCan;
  canStop();
  canBegin(profile, bitrate, true);
  sendJson(200, getCanStatusJson());
}

static void handleRs485Settings() {
  JsonDocument doc;
  deserializeJson(doc, server.arg("plain"));
  uint32_t baud = doc["baud"] | 9600;
  rs485SetBaud(baud);
  sendJson(200, getRs485StatusJson());
}

void httpApiBegin() {
  server.onNotFound([] {
    if (server.method() == HTTP_OPTIONS) {
      cors();
      server.send(204);
      return;
    }
    sendJson(404, "{\"ok\":false,\"error\":\"not found\"}");
  });
  server.on("/health", HTTP_GET, [] { logRequest(); sendJson(200, healthJson()); });
  server.on("/api/gateway/status", HTTP_GET, [] { logRequest(); sendJson(200, gatewayStatusJson()); });
  server.on("/api/rs485/status", HTTP_GET, [] { logRequest(); sendJson(200, getRs485StatusJson()); });
  server.on("/api/rs485/raw/latest", HTTP_GET, [] { logRequest(); sendJson(200, getRs485RawLatestJson(limitArg(50))); });
  server.on("/api/rs485/scan", HTTP_POST, [] { logRequest(); startRs485BmsScan(); sendJson(200, "{\"ok\":true,\"data\":{\"scan\":\"started_passive\"}}"); });
  server.on("/api/bms/latest", HTTP_GET, [] { logRequest(); sendJson(200, getBmsTelemetryJson()); });
  server.on("/api/can/status", HTTP_GET, [] { logRequest(); sendJson(200, getCanStatusJson()); });
  server.on("/api/can/frames/latest", HTTP_GET, [] { logRequest(); sendJson(200, getCanFramesLatestJson(limitArg(100))); });
  server.on("/api/can/tx", HTTP_POST, [] { logRequest(); sendJson(403, "{\"ok\":false,\"error\":\"CAN TX disabled in phase 1\"}"); });
  server.on("/api/debug/logs/latest", HTTP_GET, [] { logRequest(); sendJson(200, getRecentLogsJson(limitArg(100))); });
  server.on("/api/debug/build", HTTP_GET, [] { logRequest(); sendJson(200, buildJson()); });
  server.on("/api/settings/wifi", HTTP_POST, [] { logRequest(); handleWifiSettings(); });
  server.on("/api/settings/can", HTTP_POST, [] { logRequest(); handleCanSettings(); });
  server.on("/api/settings/rs485", HTTP_POST, [] { logRequest(); handleRs485Settings(); });
  server.on("/api/reboot", HTTP_POST, [] { logRequest(); sendJson(200, "{\"ok\":true,\"data\":{\"rebooting\":true}}"); delay(500); ESP.restart(); });
  server.begin();
  logLine(LogLevel::INFO, "HTTP", "Server started port=80");
}

void httpApiLoop() {
  server.handleClient();
}
