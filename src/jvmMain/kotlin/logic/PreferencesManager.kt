package logic

import java.util.prefs.Preferences

object PreferencesManager {

    val preferences: Preferences = Preferences.userRoot().node("com/loloof64/chess_against_engine")

    fun saveEnginePath(newPath: String) {
        preferences.put("enginePath", newPath)
    }

    fun getEnginePath(): String {
        return preferences.get("enginePath", "")
    }
}