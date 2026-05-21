#pragma once
#include <Arduino.h>
#include "models.h"

bool canBegin(CanProfile profile, uint32_t bitrate, bool listenOnly);
void canStop();
void canLoop();
CanStatus getCanStatus();
String getCanStatusJson();
String getCanFramesLatestJson(size_t limit);
