#include "debug_log.h"
#include "config.h"
#include "ring_buffer.h"

static RingBuffer<LogEntry, 200> g_logs;

void logInit() {
  g_logs.clear();
}

String levelToString(LogLevel level) {
  switch (level) {
    case LogLevel::DEBUG: return "DEBUG";
    case LogLevel::INFO: return "INFO";
    case LogLevel::WARN: return "WARN";
    case LogLevel::ERROR: return "ERROR";
  }
  return "INFO";
}

void logLine(LogLevel level, const String& tag, const String& message) {
  LogEntry entry;
  entry.tsMs = millis();
  entry.level = level;
  entry.tag = tag;
  entry.message = message;
  g_logs.push(entry);
  if (DEBUG_SERIAL) {
    Serial.printf("[%06lu][%s][%s] %s\n",
      static_cast<unsigned long>(entry.tsMs),
      levelToString(level).c_str(),
      tag.c_str(),
      message.c_str());
  }
}

void logJson(LogLevel level, const String& tag, const String& message, JsonDocument& data) {
  String json;
  serializeJson(data, json);
  logLine(level, tag, message + " " + json);
}

String getRecentLogsJson(size_t maxLines) {
  JsonDocument doc;
  doc["ok"] = true;
  JsonArray data = doc["data"].to<JsonArray>();
  size_t count = min(maxLines, g_logs.size());
  for (size_t i = 0; i < count; i++) {
    LogEntry entry = g_logs.atLatest(count - 1 - i);
    JsonObject row = data.add<JsonObject>();
    row["tsMs"] = entry.tsMs;
    row["level"] = levelToString(entry.level);
    row["tag"] = entry.tag;
    row["message"] = entry.message;
  }
  String out;
  serializeJson(doc, out);
  return out;
}
