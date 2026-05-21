#pragma once
#include <Arduino.h>
#include "models.h"

void bmsDecoderBegin();
void bmsDecoderOnRs485Frame(const uint8_t* data, size_t len);
BmsTelemetry getBmsTelemetry();
String getBmsTelemetryJson();
String getBmsRawDecodeStatusJson();
void startRs485BmsScan();
