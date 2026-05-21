#include "udp_discovery.h"
#include "config.h"
#include "wifi_manager.h"
#include "debug_log.h"
#include <WiFiUdp.h>
#include <ArduinoJson.h>

static WiFiUDP udp;
static uint32_t lastBeaconMs = 0;

void discoveryBegin() {
  udp.begin(UDP_DISCOVERY_PORT);
  logLine(LogLevel::INFO, "UDP", "Discovery started port=" + String(UDP_DISCOVERY_PORT));
}

String discoveryPayload() {
  JsonDocument doc;
  doc["type"] = "camper_tcan485_hello";
  doc["device"] = FW_NAME;
  doc["version"] = FW_VERSION;
  doc["ip"] = wifiIpString();
  doc["hostname"] = DEFAULT_HOSTNAME;
  doc["wifiMode"] = wifiModeString();
  doc["rs485"] = true;
  doc["can"] = true;
  doc["profile"] = "battery_rs485";
  doc["uptimeMs"] = millis();
  String out;
  serializeJson(doc, out);
  return out;
}

void discoveryLoop() {
  if (millis() - lastBeaconMs < 2000 || !isWifiConnected()) return;
  lastBeaconMs = millis();
  String payload = discoveryPayload();
  udp.beginPacket(IPAddress(255, 255, 255, 255), UDP_DISCOVERY_PORT);
  udp.write(reinterpret_cast<const uint8_t*>(payload.c_str()), payload.length());
  udp.endPacket();
  logLine(LogLevel::INFO, "UDP", "Broadcast discovery ip=" + wifiIpString());
}
