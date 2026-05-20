package se.gottmoz.camperagent.integration.canbus

class CanScanner {
    private var activeProfile: CanBusProfile? = null
    private val frames = mutableListOf<CanFrame>()

    fun start(profile: CanBusProfile) {
        require(profile.passiveListenOnly) { "Only passive CAN scanning is allowed in phase 1" }
        activeProfile = profile
        frames.clear()
    }

    fun stop() {
        activeProfile = null
    }

    fun observe(frame: CanFrame) {
        if (activeProfile != null) frames += frame
    }

    fun snapshot(): List<CanFrame> = frames.toList()
}
