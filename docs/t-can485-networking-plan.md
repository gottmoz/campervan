# T-CAN485 networking plan

Phase 1 supports the Android head unit sharing internet while the LilyGO T-CAN485 stays on the same LAN.

## Network modes

### 1. Van Router Mode

Android and LilyGO both connect to the same van router or 4G router.

This is the best default for stable internet and predictable LAN discovery.

### 2. Android Hotspot Mode

Android creates the Wi-Fi hotspot. LilyGO connects as a Wi-Fi STA/client to that hotspot.

Use this when Android has non-Wi-Fi upstream internet:

- Android SIM / 4G
- Android USB modem
- Android Ethernet
- other non-Wi-Fi upstream

In this mode Android keeps running the Campervan HMI, keeps Cloudflare logging online when upstream internet exists, and talks to LilyGO over the hotspot LAN.

Warning: if Android itself uses Wi-Fi for internet, enabling hotspot may disconnect Wi-Fi internet on some Android 9 head units.

### 3. LilyGO Setup AP Mode

LilyGO creates a temporary setup Wi-Fi network. Android connects directly only for first setup or recovery.

This mode can make Android lose internet while connected to LilyGO.

## Firmware target

Firmware project target path, when added:

`firmware/t-can485-gateway`

Required Wi-Fi mode enum:

```cpp
enum class WifiMode {
  StaToRouter,
  StaToAndroidHotspot,
  SetupApFallback
};
```

Required settings shape:

```json
{
  "wifiMode": "sta_android_hotspot",
  "ssid": "",
  "password": "",
  "hostname": "camper-tcan485",
  "apFallbackEnabled": true,
  "udpDiscoveryEnabled": true
}
```

Do not log or upload Wi-Fi passwords. Diagnostics must redact `password`.

## UDP discovery

LilyGO sends a broadcast beacon every 2 seconds:

- destination: `255.255.255.255`
- port: `47887`

Payload:

```json
{
  "type": "camper_tcan485_hello",
  "device": "t-can485-gateway",
  "version": "0.1.0",
  "ip": "192.168.x.x",
  "hostname": "camper-tcan485",
  "profile": "battery_bms",
  "rs485": true,
  "can": true
}
```

## HTTP API

LilyGO must expose:

- `GET /health`
- `GET /api/gateway/status`
- `GET /api/rs485/status`
- `GET /api/bms/latest`
- `GET /api/can/status`
- `GET /api/can/frames/latest`

Default fallback URLs in Android:

- `http://camper-tcan485.local`
- `http://192.168.4.1`
- discovered UDP IP

## Android hotspot limitation

Phase 1 does not assume the app can programmatically enable hotspot.

Android 9 normal apps often cannot toggle hotspot directly unless the app is privileged, rooted, or using a vendor API. The HMI opens Android wireless settings and tells the user to enable hotspot manually, then press Discover LilyGO.
