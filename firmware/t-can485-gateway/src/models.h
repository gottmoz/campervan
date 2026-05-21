#pragma once
#include <Arduino.h>

enum class WifiMode { StaToRouter, StaToAndroidHotspot, SetupApFallback };
enum class GatewayProfile { BatteryRs485, BatteryCan, GarminNmea2000, FordCan, RawCan };
enum class Rs485Protocol { AutoDetect, JbdXiaoxiang, JkBms, Daly, Seplos, Pace, RenogyBms, ModbusRtuGeneric, UnknownRaw };
enum class CanProfile { Disabled, BatteryBmsCan, GarminNmea2000, FordCan, RawCan };

struct Rs485Status {
  bool enabled = false;
  bool scanning = false;
  bool online = false;
  uint32_t baud = 9600;
  Rs485Protocol protocol = Rs485Protocol::AutoDetect;
  uint32_t framesRx = 0;
  uint32_t framesTx = 0;
  uint32_t errors = 0;
  uint32_t lastRxMs = 0;
  String lastError;
};

struct BmsTelemetry {
  bool valid = false;
  String source = "rs485";
  String protocol = "unknown_raw";
  float socPercent = NAN;
  float voltage = NAN;
  float current = NAN;
  float powerWatts = NAN;
  float remainingAh = NAN;
  float capacityAh = 320.0f;
  float minCellVoltage = NAN;
  float maxCellVoltage = NAN;
  float cellDeltaMv = NAN;
  bool chargeAllowed = false;
  bool dischargeAllowed = false;
  String alarms;
  String warnings;
  uint32_t updatedMs = 0;
};

struct CanFrameDto {
  uint32_t tsMs = 0;
  uint32_t id = 0;
  bool extended = false;
  bool rtr = false;
  uint8_t dlc = 0;
  uint8_t data[8]{};
};

struct CanStatus {
  bool enabled = false;
  bool running = false;
  bool listenOnly = true;
  uint32_t bitrate = 250000;
  CanProfile profile = CanProfile::Disabled;
  uint32_t framesRx = 0;
  uint32_t framesDropped = 0;
  uint32_t busErrors = 0;
  uint32_t rxQueueFull = 0;
  uint32_t lastFrameMs = 0;
  String lastError;
};

inline const char* wifiModeToString(WifiMode mode) {
  switch (mode) {
    case WifiMode::StaToRouter: return "sta_router";
    case WifiMode::StaToAndroidHotspot: return "sta_android_hotspot";
    case WifiMode::SetupApFallback: return "setup_ap";
  }
  return "unknown";
}

inline const char* canProfileToString(CanProfile profile) {
  switch (profile) {
    case CanProfile::Disabled: return "disabled";
    case CanProfile::BatteryBmsCan: return "battery_bms_can";
    case CanProfile::GarminNmea2000: return "garmin_nmea2000";
    case CanProfile::FordCan: return "ford_can";
    case CanProfile::RawCan: return "raw_can";
  }
  return "unknown";
}
