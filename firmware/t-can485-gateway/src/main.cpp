#include <Arduino.h>
#include <Adafruit_NeoPixel.h>
#include "config.h"
#include "debug_log.h"
#include "wifi_manager.h"
#include "udp_discovery.h"
#include "http_api.h"
#include "rs485_bus.h"
#include "bms_decoder.h"
#include "can_gateway.h"

static Adafruit_NeoPixel led(1, PIN_WS2812, NEO_GRB + NEO_KHZ800);
static uint32_t lastStatusMs = 0;
static uint32_t lastLedMs = 0;
static bool ledOn = false;

static void setLed(uint8_t r, uint8_t g, uint8_t b) {
  led.setPixelColor(0, led.Color(r, g, b));
  led.show();
}

static void bootBanner() {
  Serial.println();
  Serial.println("========================================");
  Serial.println("Camper T-CAN485 Gateway v0.1.0");
  Serial.println("Board: LilyGO T-CAN485");
  Serial.println("Serial: 115200");
  Serial.println("RS485: TX=22 RX=21 EN=9 CALLBACK=17");
  Serial.printf("CAN: TX=%d RX=%d\n", PIN_CAN_TX, PIN_CAN_RX);
  Serial.println("Mode: READ ONLY");
  Serial.println("========================================");
}

void setup() {
  Serial.begin(SERIAL_BAUD);
  delay(300);
  logInit();
  bootBanner();
  logLine(LogLevel::INFO, "BOOT", "Camper T-CAN485 Gateway v0.1.0");
  logLine(LogLevel::INFO, "PINS", "RS485 TX=22 RX=21 EN=9 CALLBACK=17 CAN_TX=" + String(PIN_CAN_TX) + " CAN_RX=" + String(PIN_CAN_RX));

  pinMode(PIN_ME2107_EN, OUTPUT);
  digitalWrite(PIN_ME2107_EN, HIGH);

  led.begin();
  setLed(0, 0, 40);

  bool wifiConnected = beginWifi();
  setLed(wifiConnected ? 0 : 40, wifiConnected ? 40 : 20, 0);

  discoveryBegin();
  rs485Begin(9600);
  bmsDecoderBegin();
  canBegin(CanProfile::Disabled, 250000, true);
  httpApiBegin();
}

void loop() {
  discoveryLoop();
  httpApiLoop();
  rs485Loop();
  canLoop();

  if (millis() - lastStatusMs > 10000) {
    lastStatusMs = millis();
    Rs485Status rs = getRs485Status();
    CanStatus can = getCanStatus();
    logLine(LogLevel::INFO, "STATUS", "ip=" + wifiIpString() + " rs485Rx=" + String(rs.framesRx) + " canRx=" + String(can.framesRx) + " heap=" + String(ESP.getFreeHeap()) + " uptime=" + String(millis()));
  }

  if (millis() - lastLedMs > 1000) {
    lastLedMs = millis();
    ledOn = !ledOn;
    if (wifiModeString() == "ap") setLed(ledOn ? 40 : 4, ledOn ? 20 : 2, 0);
    else setLed(0, ledOn ? 40 : 4, 0);
  }
}
