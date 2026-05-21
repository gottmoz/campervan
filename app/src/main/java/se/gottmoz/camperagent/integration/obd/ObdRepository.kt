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

    fun addLog(
        direction: String,
        command: String? = null,
        response: String? = null,
        state: String? = null,
        elapsedMs: Long? = null,
        error: String? = null
    ) {
        if (rawLog.size >= 200) rawLog.removeFirst()
        rawLog.addLast(
            JSONObject()
                .put("timestamp", nowIso())
                .put("direction", direction)
                .put("command", command ?: JSONObject.NULL)
                .put("response", response ?: JSONObject.NULL)
                .put("state", state ?: JSONObject.NULL)
                .put("elapsedMs", elapsedMs ?: JSONObject.NULL)
                .put("error", error ?: JSONObject.NULL)
        )
    }

    fun rawLogTail(): JSONArray = JSONArray(rawLog.toList())

    fun updateSupportedPids(supportedPids: Set<String>) {
        _telemetry.value = _telemetry.value.copy(
            supportedPids = supportedPids,
            source = "Live",
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }

    fun mergeTelemetry(update: ObdTelemetry) {
        val current = _telemetry.value
        _telemetry.value = current.copy(
            speedKph = update.speedKph ?: current.speedKph,
            rpm = update.rpm ?: current.rpm,
            coolantTempC = update.coolantTempC ?: current.coolantTempC,
            intakeTempC = update.intakeTempC ?: current.intakeTempC,
            moduleVoltage = update.moduleVoltage ?: current.moduleVoltage,
            mafGps = update.mafGps ?: current.mafGps,
            throttlePercent = update.throttlePercent ?: current.throttlePercent,
            ambientTempC = update.ambientTempC ?: current.ambientTempC,
            driverDemandTorquePercent = update.driverDemandTorquePercent ?: current.driverDemandTorquePercent,
            actualTorquePercent = update.actualTorquePercent ?: current.actualTorquePercent,
            engineReferenceTorqueNm = update.engineReferenceTorqueNm ?: current.engineReferenceTorqueNm,
            supportedPids = if (update.supportedPids.isNotEmpty()) update.supportedPids else current.supportedPids,
            source = "Live",
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }

    fun telemetryJson(): JSONObject {
        val value = _telemetry.value
        return JSONObject()
            .put("speedKph", value.speedKph ?: JSONObject.NULL)
            .put("rpm", value.rpm ?: JSONObject.NULL)
            .put("coolantTempC", value.coolantTempC ?: JSONObject.NULL)
            .put("intakeTempC", value.intakeTempC ?: JSONObject.NULL)
            .put("moduleVoltage", value.moduleVoltage ?: JSONObject.NULL)
            .put("mafGps", value.mafGps ?: JSONObject.NULL)
            .put("throttlePercent", value.throttlePercent ?: JSONObject.NULL)
            .put("ambientTempC", value.ambientTempC ?: JSONObject.NULL)
            .put("supportedPids", JSONArray(value.supportedPids.sorted()))
            .put("source", value.source)
            .put("updatedAtEpochMs", value.updatedAtEpochMs)
    }

    fun simulated(): ObdTelemetry = ObdTelemetry(
        speedKph = 0,
        rpm = 780,
        coolantTempC = 86,
        intakeTempC = 24,
        mafGps = 4.2,
        throttlePercent = 12.0,
        ambientTempC = 18,
        dtcCodes = emptyList(),
        supportedPids = rawSupportedPids(),
        source = "Simulated"
    )

    private fun rawSupportedPids() = setOf("0C", "0D", "05", "0F", "10", "11", "46")

    private fun nowIso(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }
}
