#pragma once
#include <Arduino.h>
#include <ArduinoJson.h>

enum class LogLevel { DEBUG, INFO, WARN, ERROR };

struct LogEntry {
  uint32_t tsMs = 0;
  LogLevel level = LogLevel::INFO;
  String tag;
  String message;
};

void logInit();
void logLine(LogLevel level, const String& tag, const String& message);
void logJson(LogLevel level, const String& tag, const String& message, JsonDocument& data);
String levelToString(LogLevel level);
String getRecentLogsJson(size_t maxLines);
