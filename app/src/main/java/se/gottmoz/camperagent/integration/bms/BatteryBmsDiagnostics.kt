package se.gottmoz.camperagent.integration.bms

import org.json.JSONArray
import org.json.JSONObject

data class BatteryBmsDiagnostics(
    val rawCanFrames: List<BatteryCanFrameSummary> = emptyList(),
    val bleCandidates: List<BleBmsCandidate> = emptyList(),
    val errors: List<String> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("rawCanFrameCount", rawCanFrames.size)
        .put("bleServicesCount", bleCandidates.sumOf { it.services.size })
        .put("errors", JSONArray(errors))
        .put("readOnly", true)
}
