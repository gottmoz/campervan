#pragma once
#include <Arduino.h>
#include "models.h"

struct Rs485RawFrame {
  uint32_t tsMs = 0;
  String direction;
  String hex;
  uint32_t baud = 9600;
  String protocol;
  String decodeStatus;
};

void rs485Begin(uint32_t baud);
void rs485SetBaud(uint32_t baud);
void rs485Loop();
void rs485Write(const uint8_t* data, size_t len, const char* reason);
bool rs485Available();
size_t rs485ReadAvailable();
Rs485Status getRs485Status();
String getRs485StatusJson();
String getRs485RawLatestJson(size_t limit);
