#pragma once
#include <Arduino.h>

// LilyGO T-CAN485 pinout from official repo README.
static constexpr int PIN_RS485_TX = 22;
static constexpr int PIN_RS485_RX = 21;
static constexpr int PIN_RS485_CALLBACK = 17;
static constexpr int PIN_RS485_EN = 9;

static constexpr int PIN_WS2812 = 4;
static constexpr int PIN_ME2107_EN = 16;

// Confirmed T-CAN485 pinout: CAN_TX=IO27, CAN_RX=IO26.
#ifndef PIN_CAN_TX
#define PIN_CAN_TX 27
#endif

#ifndef PIN_CAN_RX
#define PIN_CAN_RX 26
#endif

static constexpr uint32_t SERIAL_BAUD = 115200;
static constexpr uint32_t UDP_DISCOVERY_PORT = 47887;
static constexpr uint32_t HTTP_PORT = 80;

static constexpr const char* FW_NAME = "t-can485-gateway";
static constexpr const char* FW_VERSION = "0.1.0";
static constexpr const char* DEFAULT_HOSTNAME = "camper-tcan485";

static constexpr const char* AP_SSID_PREFIX = "Camper-TCAN485-";
static constexpr const char* AP_PASSWORD = "camper485";

static constexpr bool DEBUG_SERIAL = true;
static constexpr bool DEBUG_RS485_HEX = true;
static constexpr bool DEBUG_CAN_FRAMES = true;
static constexpr bool DEBUG_HTTP = true;

static constexpr bool CAN_TX_ENABLED_PHASE1 = false;
static constexpr bool BMS_WRITE_ENABLED_PHASE1 = false;
