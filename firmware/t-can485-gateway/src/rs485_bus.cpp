#include "rs485_bus.h"
#include "config.h"
#include "debug_log.h"
#include "bms_decoder.h"
#include "ring_buffer.h"
#include <ArduinoJson.h>

static HardwareSerial Rs485Serial(2);
static Rs485Status status;
static RingBuffer<Rs485RawFrame, 100> rawFrames;
static uint8_t frameBuf[256];
static size_t frameLen = 0;
static uint32_t lastByteMs = 0;

static String bytesToHex(const uint8_t* data, size_t len) {
  String out;
  for (size_t i = 0; i < len; i++) {
    if (i) out += " ";
    if (data[i] < 16) out += "0";
    out += String(data[i], HEX);
  }
  out.toUpperCase();
  return out;
}

static void closeRxFrame() {
  if (frameLen == 0) return;
  String hex = bytesToHex(frameBuf, frameLen);
  Rs485RawFrame frame;
  frame.tsMs = millis();
  frame.direction = "rx";
  frame.hex = hex;
  frame.baud = status.baud;
  frame.protocol = "AutoDetect";
  frame.decodeStatus = "unknown";
  rawFrames.push(frame);
  status.framesRx++;
  status.online = true;
  status.lastRxMs = millis();
  if (DEBUG_RS485_HEX) logLine(LogLevel::DEBUG, "RS485", "RX " + hex);
  bmsDecoderOnRs485Frame(frameBuf, frameLen);
  frameLen = 0;
}

void rs485Begin(uint32_t baud) {
  pinMode(PIN_RS485_EN, OUTPUT);
  digitalWrite(PIN_RS485_EN, HIGH);
  pinMode(PIN_RS485_CALLBACK, INPUT_PULLUP);
  status.enabled = true;
  status.baud = baud;
  Rs485Serial.begin(baud, SERIAL_8N1, PIN_RS485_RX, PIN_RS485_TX);
  logLine(LogLevel::INFO, "RS485", "Begin baud=" + String(baud) + " TX=22 RX=21 EN=9 CALLBACK=17");
}

void rs485SetBaud(uint32_t baud) {
  Rs485Serial.updateBaudRate(baud);
  status.baud = baud;
  logLine(LogLevel::INFO, "RS485", "Baud changed baud=" + String(baud));
}

void rs485Loop() {
  while (Rs485Serial.available()) {
    int value = Rs485Serial.read();
    if (value < 0) break;
    if (frameLen < sizeof(frameBuf)) frameBuf[frameLen++] = static_cast<uint8_t>(value);
    else status.errors++;
    lastByteMs = millis();
  }
  if (frameLen > 0 && millis() - lastByteMs > 20) closeRxFrame();
}

void rs485Write(const uint8_t* data, size_t len, const char* reason) {
  if (!BMS_WRITE_ENABLED_PHASE1) {
    logLine(LogLevel::WARN, "RS485", String("TX blocked phase1 reason=") + reason);
    return;
  }
  Rs485Serial.write(data, len);
  status.framesTx++;
}

bool rs485Available() { return Rs485Serial.available() > 0; }
size_t rs485ReadAvailable() { return Rs485Serial.available(); }
Rs485Status getRs485Status() { return status; }

String getRs485StatusJson() {
  JsonDocument doc;
  doc["ok"] = true;
  JsonObject data = doc["data"].to<JsonObject>();
  data["enabled"] = status.enabled;
  data["scanning"] = status.scanning;
  data["online"] = status.online;
  data["baud"] = status.baud;
  data["protocol"] = "AutoDetect";
  data["framesRx"] = status.framesRx;
  data["framesTx"] = status.framesTx;
  data["errors"] = status.errors;
  data["lastRxMs"] = status.lastRxMs;
  data["lastError"] = status.lastError;
  String out;
  serializeJson(doc, out);
  return out;
}

String getRs485RawLatestJson(size_t limit) {
  JsonDocument doc;
  doc["ok"] = true;
  JsonArray data = doc["data"].to<JsonArray>();
  size_t count = min(limit, rawFrames.size());
  for (size_t i = 0; i < count; i++) {
    Rs485RawFrame frame = rawFrames.atLatest(count - 1 - i);
    JsonObject row = data.add<JsonObject>();
    row["tsMs"] = frame.tsMs;
    row["direction"] = frame.direction;
    row["hex"] = frame.hex;
    row["baud"] = frame.baud;
    row["protocol"] = frame.protocol;
    row["decodeStatus"] = frame.decodeStatus;
  }
  String out;
  serializeJson(doc, out);
  return out;
}
