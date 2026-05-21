#include "can_gateway.h"
#include "config.h"
#include "debug_log.h"
#include "ring_buffer.h"
#include <ArduinoJson.h>
#include "driver/twai.h"

static CanStatus status;
static RingBuffer<CanFrameDto, 500> frames;

static String frameDataHex(const CanFrameDto& frame) {
  String out;
  for (uint8_t i = 0; i < frame.dlc; i++) {
    if (i) out += " ";
    if (frame.data[i] < 16) out += "0";
    out += String(frame.data[i], HEX);
  }
  out.toUpperCase();
  return out;
}

static twai_timing_config_t timingFor(uint32_t bitrate) {
  if (bitrate == 500000) return TWAI_TIMING_CONFIG_500KBITS();
  return TWAI_TIMING_CONFIG_250KBITS();
}

bool canBegin(CanProfile profile, uint32_t bitrate, bool listenOnly) {
  if (profile == CanProfile::Disabled) {
    status = CanStatus();
    logLine(LogLevel::INFO, "CAN", "Disabled by default");
    return true;
  }
  status.profile = profile;
  status.bitrate = bitrate;
  status.listenOnly = listenOnly || !CAN_TX_ENABLED_PHASE1;
  twai_general_config_t g_config = TWAI_GENERAL_CONFIG_DEFAULT((gpio_num_t)PIN_CAN_TX, (gpio_num_t)PIN_CAN_RX, status.listenOnly ? TWAI_MODE_LISTEN_ONLY : TWAI_MODE_NORMAL);
  twai_timing_config_t t_config = timingFor(bitrate);
  twai_filter_config_t f_config = TWAI_FILTER_CONFIG_ACCEPT_ALL();
  esp_err_t err = twai_driver_install(&g_config, &t_config, &f_config);
  if (err != ESP_OK && err != ESP_ERR_INVALID_STATE) {
    status.lastError = "twai_driver_install failed";
    logLine(LogLevel::ERROR, "CAN", status.lastError);
    return false;
  }
  twai_reconfigure_alerts(TWAI_ALERT_RX_DATA | TWAI_ALERT_BUS_ERROR | TWAI_ALERT_RX_QUEUE_FULL | TWAI_ALERT_BUS_OFF | TWAI_ALERT_ERR_PASS | TWAI_ALERT_ERR_ACTIVE, nullptr);
  err = twai_start();
  if (err != ESP_OK && err != ESP_ERR_INVALID_STATE) {
    status.lastError = "twai_start failed";
    logLine(LogLevel::ERROR, "CAN", status.lastError);
    return false;
  }
  status.enabled = true;
  status.running = true;
  logLine(LogLevel::INFO, "CAN", "Started profile=" + String(canProfileToString(profile)) + " bitrate=" + String(bitrate) + " listenOnly=true TX=blocked");
  return true;
}

void canStop() {
  twai_stop();
  twai_driver_uninstall();
  status.running = false;
  status.enabled = false;
}

void canLoop() {
  if (!status.running) return;
  uint32_t alerts = 0;
  if (twai_read_alerts(&alerts, 0) == ESP_OK) {
    if (alerts & TWAI_ALERT_BUS_ERROR) status.busErrors++;
    if (alerts & TWAI_ALERT_RX_QUEUE_FULL) status.rxQueueFull++;
  }
  twai_message_t message;
  while (twai_receive(&message, 0) == ESP_OK) {
    CanFrameDto frame;
    frame.tsMs = millis();
    frame.id = message.identifier;
    frame.extended = message.extd;
    frame.rtr = message.rtr;
    frame.dlc = message.data_length_code;
    for (uint8_t i = 0; i < frame.dlc && i < 8; i++) frame.data[i] = message.data[i];
    frames.push(frame);
    status.framesRx++;
    status.lastFrameMs = millis();
    if (DEBUG_CAN_FRAMES) logLine(LogLevel::DEBUG, "CAN", "RX id=" + String(frame.id, HEX) + " ext=" + String(frame.extended) + " dlc=" + String(frame.dlc) + " data=" + frameDataHex(frame));
  }
}

CanStatus getCanStatus() { return status; }

String getCanStatusJson() {
  JsonDocument doc;
  doc["ok"] = true;
  JsonObject data = doc["data"].to<JsonObject>();
  data["enabled"] = status.enabled;
  data["running"] = status.running;
  data["listenOnly"] = status.listenOnly;
  data["bitrate"] = status.bitrate;
  data["profile"] = canProfileToString(status.profile);
  data["framesRx"] = status.framesRx;
  data["framesDropped"] = status.framesDropped;
  data["busErrors"] = status.busErrors;
  data["rxQueueFull"] = status.rxQueueFull;
  data["lastFrameMs"] = status.lastFrameMs;
  data["lastError"] = status.lastError;
  String out;
  serializeJson(doc, out);
  return out;
}

String getCanFramesLatestJson(size_t limit) {
  JsonDocument doc;
  doc["ok"] = true;
  JsonArray data = doc["data"].to<JsonArray>();
  size_t count = min(limit, frames.size());
  for (size_t i = 0; i < count; i++) {
    CanFrameDto frame = frames.atLatest(count - 1 - i);
    JsonObject row = data.add<JsonObject>();
    row["tsMs"] = frame.tsMs;
    row["id"] = String(frame.id, HEX);
    row["idDec"] = frame.id;
    row["extended"] = frame.extended;
    row["rtr"] = frame.rtr;
    row["dlc"] = frame.dlc;
    row["data"] = frameDataHex(frame);
  }
  String out;
  serializeJson(doc, out);
  return out;
}
