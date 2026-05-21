# Camper T-CAN485 Gateway

Buildable PlatformIO firmware for LilyGO T-CAN485.

Phase 1 is read-only:

- no CAN TX
- no BMS write/config commands
- RS485 raw capture and defensive BMS decode framework
- CAN/TWAI listen-only when enabled
- HTTP API for Android HMI
- UDP discovery on port `47887`

## Hardware

- Board: LilyGO T-CAN485 as ESP32 Dev Module
- MCU: ESP32
- Flash: 4MB
- PSRAM: none
- RS485 chip: MAX13487EESA+
- CAN transceiver: SN65HVD231
- RS485 TX: IO22
- RS485 RX: IO21
- RS485 CALLBACK: IO17
- RS485 EN: IO9
- CAN TX: IO27
- CAN RX: IO26
- WS2812 DATA: IO4
- ME2107 EN: IO16
- Upload speed: 921600
- Monitor speed: 115200

## Install PlatformIO

Use the VS Code PlatformIO extension, or:

```powershell
pip install platformio
```

## Build

```powershell
cd E:\camper-agent\firmware\t-can485-gateway
pio run -e t-can485
```

## Upload to COM8

```powershell
cd E:\camper-agent\firmware\t-can485-gateway
pio run -e t-can485 -t upload --upload-port COM8
```

## Monitor COM8

```powershell
pio device monitor -p COM8 -b 115200
```

Full upload and monitor:

```powershell
pio run -e t-can485 -t upload --upload-port COM8
pio device monitor -p COM8 -b 115200
```

If upload fails, hold `BOOT/BOOT-0`, run upload again, and release BOOT when `Connecting...` appears.

## Test from PC

If AP fallback starts, connect PC/phone to:

```text
Camper-TCAN485-XXXX
```

Then:

```powershell
Invoke-RestMethod http://192.168.4.1/health
```

If STA/hotspot/router mode connects, find IP from serial monitor or UDP discovery:

```powershell
Invoke-RestMethod http://<esp-ip>/health
Invoke-RestMethod http://<esp-ip>/api/gateway/status
Invoke-RestMethod http://<esp-ip>/api/rs485/status
Invoke-RestMethod http://<esp-ip>/api/bms/latest
Invoke-RestMethod http://<esp-ip>/api/debug/logs/latest
```

## Configure Android hotspot Wi-Fi

```powershell
$body = @{
  wifiMode = "sta_android_hotspot"
  ssid = "YOUR_ANDROID_HOTSPOT_SSID"
  password = "YOUR_PASSWORD"
  hostname = "camper-tcan485"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -ContentType "application/json" -Body $body http://192.168.4.1/api/settings/wifi
Invoke-RestMethod -Method Post http://192.168.4.1/api/reboot
```

The firmware does not log or return the Wi-Fi password.

## API

- `GET /health`
- `GET /api/gateway/status`
- `GET /api/rs485/status`
- `GET /api/bms/latest`
- `GET /api/rs485/raw/latest?limit=50`
- `POST /api/rs485/scan`
- `GET /api/can/status`
- `GET /api/can/frames/latest?limit=100`
- `POST /api/can/tx` returns 403, disabled in phase 1
- `GET /api/debug/logs/latest?limit=100`
- `POST /api/settings/wifi`
- `POST /api/settings/can`
- `POST /api/settings/rs485`
- `POST /api/reboot`

## RS485 wiring

- Battery RS485 A to LilyGO RS485 A
- Battery RS485 B to LilyGO RS485 B
- GND/reference if BMS manual requires it
- Use twisted pair
- If no data, try swapping A/B
- Do not connect RS485 to CAN

## CAN wiring

- Connect only one CAN bus at a time
- Garmin/NMEA2000: 250k, 29-bit extended, listen-only
- Ford CAN: 500k, listen-only only
- Battery CAN: 250k/500k, listen-only
- Do not connect Garmin CAN, BMS CAN and Ford CAN together
- CAN TX disabled in phase 1

## Android hotspot note

Best mode if Android has SIM/4G/USB/Ethernet internet. LilyGO connects as Wi-Fi STA to Android hotspot. Android keeps Cloudflare/internet only if upstream is not the same Wi-Fi being replaced.

## Expected serial debug

```text
[000001][INFO][BOOT] Camper T-CAN485 Gateway v0.1.0
[000010][INFO][PINS] RS485 TX=22 RX=21 EN=9 CALLBACK=17
[000020][INFO][WiFi] Trying STA ssid=VanHotspot
[005321][INFO][WiFi] Connected ip=192.168.43.88 rssi=-51
[005400][INFO][HTTP] Server started port=80
[005450][INFO][UDP] Discovery started port=47887
[005500][INFO][RS485] Begin baud=9600
[005550][INFO][CAN] Disabled by default
[015550][INFO][STATUS] ip=192.168.43.88 rs485Rx=0 canRx=0 heap=182344 uptime=15550
```
