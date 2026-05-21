package se.gottmoz.camperagent.integration.obd

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ObdRepository {
    private val _telemetry = MutableStateFlow(ObdTelemetry(supportedPids = rawSupportedPids()))
    val telemetry: StateFlow<ObdTelemetry> = _telemetry
    private val rawLog = ArrayDeque<JSONObject>()

    fun addLog(direction: String, command: String? = null, response: String? = null, state: String? = null) {
        if (rawLog.size >= 200) rawLog.removeFirst()
        rawLog.addLast(
            JSONObject()
                .put("timestamp", nowIso())
                .put("direction", direction)
                .put("command", command ?: JSONObject.NULL)
                .put("response", response ?: JSONObject.NULL)
                .put("state", state ?: JSONObject.NULL)
        )
    }

    fun rawLogTail(): JSONArray = JSONArray(rawLog.toList())

    fun simulated(): ObdTelemetry = ObdTelemetry(
        speedKph = 0,
        rpm = 780,
        coolantTempC = 86,
        intakeTempC = 24,
        mafGps = 4.2,
        throttlePercent = 12.0,
        ambientTempC = 18,
        dtcCodes = emptyList(),
        supportedPids = rawSupportedPids()
    )

    private fun rawSupportedPids() = setOf("0C", "0D", "05", "0F", "10", "11", "46")

    private fun nowIso(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }
}
