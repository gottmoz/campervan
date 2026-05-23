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
        return parse(raw, "01")
    }

    fun isValid0100Response(raw: String): Boolean {
        val parsed = parseMode01(raw) ?: return false
        return parsed.pid == "00" && parsed.dataBytes.size >= 4
    }

    fun parse(raw: String, requestService: String): ParsedObdResponse? {
        val clean = raw
            .replace(">", "")
            .replace(Regex("\\s+"), "")
            .uppercase()
            .replace(Regex("[^0-9A-F]"), "")
        if (clean.length < 4) return null
        val positiveService = when (requestService.uppercase()) {
            "01" -> "41"
            "21" -> "61"
            "22" -> "62"
            else -> "%02X".format((requestService.toIntOrNull(16) ?: 0) + 0x40)
        }

        val candidates = buildList {
            add(clean)
            val firstPositive = clean.indexOf(positiveService)
            if (firstPositive >= 0) add(clean.substring(firstPositive))
        }

        for (candidate in candidates) {
            parseCandidate(candidate, clean, positiveService)?.let { return it }
        }
        return null
    }

    private fun parseCandidate(candidate: String, rawClean: String, positiveService: String): ParsedObdResponse? {
        var offset = 0
        var ecuId: String? = null
        var length: Int? = null
        if (candidate.length >= 8 && candidate.take(3).matches(Regex("[0-9A-F]{3}"))) {
            ecuId = candidate.take(3)
            length = candidate.substring(3, 5).toIntOrNull(16)
            if (candidate.substring(5, 7) == positiveService) offset = 5
        }
        if (candidate.length < offset + 4) return null
        val service = candidate.substring(offset, offset + 2)
        if (service != positiveService) return null
        val pidLength = if (service == "62" && candidate.length >= offset + 6) 4 else 2
        if (candidate.length < offset + 2 + pidLength) return null
        val pid = candidate.substring(offset + 2, offset + 2 + pidLength)
        val data = candidate.substring(offset + 2 + pidLength).chunked(2).mapNotNull { it.toIntOrNull(16) }
        return ParsedObdResponse(ecuId, length, service, pid, data, rawClean)
    }
}
