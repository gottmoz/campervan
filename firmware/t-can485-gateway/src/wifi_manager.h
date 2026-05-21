#pragma once
#include <Arduino.h>
#include "models.h"

bool beginWifi();
bool startSta(const String& ssid, const String& password);
void startFallbackAp();
bool isWifiConnected();
String wifiIpString();
String wifiSsid();
int32_t wifiRssi();
String wifiModeString();
void saveWifiSettings(const String& wifiMode, const String& ssid, const String& password, const String& hostname, bool fallbackApEnabled);
void loadWifiSettings();
String wifiStatusJson();
