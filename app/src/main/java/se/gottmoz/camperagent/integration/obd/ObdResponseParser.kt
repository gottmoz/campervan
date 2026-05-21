package se.gottmoz.camperagent.integration.obd

data class ParsedObdResponse(
    val ecuId: String?,
    val length: Int?,
    val service: String,
    val pid: String,
    val dataBytes: List<Int>,
    val raw: String
)

object ObdResponseParser {
    fun parseMode01(raw: String): ParsedObdResponse? {
        val clean = raw
            .replace(">", "")
            .replace(Regex("\\s+"), "")
            .uppercase()
            .replace(Regex("[^0-9A-F]"), "")
        if (clean.length < 4) return null

        val candidates = buildList {
            add(clean)
            val first41 = clean.indexOf("41")
            if (first41 >= 0) add(clean.substring(first41))
        }

        for (candidate in candidates) {
            parseCandidate(candidate, clean)?.let { return it }
        }
        return null
    }

    private fun parseCandidate(candidate: String, rawClean: String): ParsedObdResponse? {
        var offset = 0
        var ecuId: String? = null
        var length: Int? = null
        if (candidate.length >= 8 && candidate.take(3).matches(Regex("7E[8-9A-F]"))) {
            ecuId = candidate.take(3)
            length = candidate.substring(3, 5).toIntOrNull(16)
            offset = 5
        }
        if (candidate.length < offset + 4) return null
        val service = candidate.substring(offset, offset + 2)
        if (service != "41") return null
        val pid = candidate.substring(offset + 2, offset + 4)
        val data = candidate.substring(offset + 4).chunked(2).mapNotNull { it.toIntOrNull(16) }
        return ParsedObdResponse(ecuId, length, service, pid, data, rawClean)
    }
}
