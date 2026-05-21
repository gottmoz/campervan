#include "wifi_manager.h"
#include "config.h"
#include "debug_log.h"
#include <WiFi.h>
#include <Preferences.h>

static Preferences prefs;
static WifiMode g_mode = WifiMode::SetupApFallback;
static String g_ssid;
static String g_password;
static String g_hostname = DEFAULT_HOSTNAME;
static bool g_fallbackApEnabled = true;
static bool g_apMode = false;

void loadWifiSettings() {
  prefs.begin("wifi", true);
  String mode = prefs.getString("wifiMode", "setup_ap");
  g_ssid = prefs.getString("ssid", "");
  g_password = prefs.getString("password", "");
  g_hostname = prefs.getString("hostname", DEFAULT_HOSTNAME);
  g_fallbackApEnabled = prefs.getBool("fallbackAp", true);
  prefs.end();
  if (mode == "sta_router") g_mode = WifiMode::StaToRouter;
  else if (mode == "sta_android_hotspot") g_mode = WifiMode::StaToAndroidHotspot;
  else g_mode = WifiMode::SetupApFallback;
}

void saveWifiSettings(const String& wifiMode, const String& ssid, const String& password, const String& hostname, bool fallbackApEnabled) {
  prefs.begin("wifi", false);
  prefs.putString("wifiMode", wifiMode);
  prefs.putString("ssid", ssid);
  if (password.length() > 0) prefs.putString("password", password);
  prefs.putString("hostname", hostname.length() ? hostname : DEFAULT_HOSTNAME);
  prefs.putBool("fallbackAp", fallbackApEnabled);
  prefs.end();
  logLine(LogLevel::INFO, "WiFi", "Settings saved ssid=" + ssid + " password=[redacted]");
}

bool beginWifi() {
  loadWifiSettings();
  WiFi.setHostname(g_hostname.c_str());
  logLine(LogLevel::INFO, "WiFi", "Mode=" + String(wifiModeToString(g_mode)) + " ssid=" + g_ssid);
  if (g_ssid.length() > 0 && g_mode != WifiMode::SetupApFallback) {
    if (startSta(g_ssid, g_password)) return true;
  }
  if (g_fallbackApEnabled) startFallbackAp();
  return false;
}

bool startSta(const String& ssid, const String& password) {
  g_apMode = false;
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid.c_str(), password.c_str());
  logLine(LogLevel::INFO, "WiFi", "Trying STA ssid=" + ssid);
  uint32_t start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 20000) {
    delay(250);
  }
  if (WiFi.status() == WL_CONNECTED) {
    logLine(LogLevel::INFO, "WiFi", "Connected ip=" + WiFi.localIP().toString() + " rssi=" + String(WiFi.RSSI()));
    return true;
  }
  logLine(LogLevel::WARN, "WiFi", "STA failed ssid=" + ssid);
  return false;
}

void startFallbackAp() {
  g_apMode = true;
  WiFi.mode(WIFI_AP);
  String suffix = String((uint32_t)ESP.getEfuseMac(), HEX);
  suffix.toUpperCase();
  String apSsid = String(AP_SSID_PREFIX) + suffix.substring(max(0, static_cast<int>(suffix.length()) - 4));
  WiFi.softAP(apSsid.c_str(), AP_PASSWORD);
  logLine(LogLevel::INFO, "WiFi", "Fallback AP started ssid=" + apSsid + " ip=" + WiFi.softAPIP().toString() + " password=[redacted]");
}

bool isWifiConnected() { return WiFi.status() == WL_CONNECTED || g_apMode; }
String wifiIpString() { return g_apMode ? WiFi.softAPIP().toString() : WiFi.localIP().toString(); }
String wifiSsid() { return g_apMode ? WiFi.softAPSSID() : WiFi.SSID(); }
int32_t wifiRssi() { return g_apMode ? 0 : WiFi.RSSI(); }
String wifiModeString() { return g_apMode ? "ap" : "sta"; }

String wifiStatusJson() {
  JsonDocument doc;
  doc["mode"] = wifiModeString();
  doc["configuredMode"] = wifiModeToString(g_mode);
  doc["ip"] = wifiIpString();
  doc["ssid"] = wifiSsid();
  doc["rssi"] = wifiRssi();
  doc["hostname"] = g_hostname;
  String out;
  serializeJson(doc, out);
  return out;
}
