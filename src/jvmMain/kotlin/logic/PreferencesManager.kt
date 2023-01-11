package logic

import java.util.prefs.Preferences

object PreferencesManager {

    private val preferences: Preferences = Preferences.userRoot().node("com/loloof64/chess_against_engine")

    fun saveEnginePath(newPath: String) {
        preferences.put("enginePath", newPath)
    }

    fun saveEngineThinkingTime(timeMillis: Int) {
        preferences.putInt("engineThinkingTimeMs", timeMillis)
    }

    fun getEnginePath(): String {
        return preferences.get("enginePath", "")
    }

    fun getEngineThinkingTime(): Int {
        return preferences.getInt("engineThinkingTimeMs", 1000)
    }
}