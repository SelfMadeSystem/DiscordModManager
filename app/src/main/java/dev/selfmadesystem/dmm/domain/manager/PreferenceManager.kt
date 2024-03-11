package dev.selfmadesystem.dmm.domain.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import androidx.annotation.StringRes
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.base.BasePreferenceManager
import dev.selfmadesystem.dmm.utils.DiscordVersion
import java.io.File
import java.util.concurrent.TimeUnit

class ProfileManager(context: Context, pref: SharedPreferences) : BasePreferenceManager(pref) {
    val DEFAULT_MODULE_LOCATION =
        (context.externalCacheDir ?: File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_DOWNLOADS
        ).resolve("DiscordModManager").also { it.mkdirs() }).resolve("mod-module.apk")

    // Discord mod
    var packageName by stringPreference("package_name", "dev.pylix.bunny")
    var appName by stringPreference("app_name", "Bunny")
    var appIcon by filePreference("appIcon", File(""))
    var channel by enumPreference("channel", DiscordVersion.Type.STABLE)
    var discordVersion by stringPreference("discord_version", "")
    var moduleVersion by stringPreference("module_version", "")
    var debuggable by booleanPreference("debuggable", false)
    var moduleUrl by stringPreference("module_url", "")
    var moduleLocation by filePreference("module_location", File(""))
    var installMethod by enumPreference("install_method", InstallMethod.DEFAULT)

    // Theme and misc mod manager related
    var monet by booleanPreference("monet", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    var autoClearCache by booleanPreference("auto_clear_cache", true)
    var theme by enumPreference("theme", Theme.SYSTEM)
    var updateDuration by enumPreference("update_duration", UpdateCheckerDuration.HOURLY)
}

class PreferenceManager(var context: Context) :
    BasePreferenceManager(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)) {

    val DEFAULT_MODULE_LOCATION =
        (context.externalCacheDir ?: File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_DOWNLOADS
        ).resolve("VendettaManager").also { it.mkdirs() }).resolve("vendetta.apk")

    var packageName by stringPreference("package_name", "dev.selfmadesystem.discord")

    var appName by stringPreference("app_name", "Vendetta")

    var discordVersion by stringPreference("discord_version", "")

    var moduleVersion by stringPreference("module_version", "")

    var patchIcon by booleanPreference("patch_icon", true)

    var debuggable by booleanPreference("debuggable", false)

    var mirror by enumPreference("mirror", Mirror.DEFAULT)

    var monet by booleanPreference("monet", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    var isDeveloper by booleanPreference("is_developer", false)

    var autoClearCache by booleanPreference("auto_clear_cache", true)

    var theme by enumPreference("theme", Theme.SYSTEM)

    var channel by enumPreference("channel", DiscordVersion.Type.STABLE)

    var updateDuration by enumPreference("update_duration", UpdateCheckerDuration.HOURLY)

    var moduleLocation by filePreference("module_location", DEFAULT_MODULE_LOCATION)

    var installMethod by enumPreference("install_method", InstallMethod.DEFAULT)

    var logsAlternateBackground by booleanPreference("logs_alternate_bg", true)

    var logsLineWrap by booleanPreference("logs_line_wrap", false)

    var profiles by stringSetPreference("profiles", setOf("0:Bunny"))

    var currentProfile by intPreference("current_profile", 0)

    private var profileManagers = mutableMapOf<String, ProfileManager>()

    fun getProfileManager(profile: Int) = run {
        profileManagers.getOrPut(profile.toString()) {
            ProfileManager(
                context,
                context.getSharedPreferences("profile_$profile", Context.MODE_PRIVATE)
            )
        }
    }

    fun getCurrentProfileManager() = getProfileManager(currentProfile)

    fun getProfileIdName(profileIdx: Int) = profiles.find { it.startsWith("$profileIdx:") }
        ?: throw IllegalArgumentException("Profile not found")

    fun getProfileName(profileIdx: Int) = getProfileIdName(profileIdx).split(":", limit = 2)[1]

    fun getCurrentProfileIdName() = getProfileIdName(currentProfile)

    fun getCurrentProfileName() = getProfileName(currentProfile)

    fun setProfileName(profileIdx: Int, name: String) {
        profiles = profiles.toMutableSet().apply {
            remove(getProfileIdName(profileIdx))
            add("$profileIdx:$name")
        }
    }

    fun setCurrentProfileName(name: String) {
        setProfileName(currentProfile, name)
    }

    fun addProfile(name: String) {
        println("Hello. New profile: $name")
        profiles = profiles.toMutableSet().apply {
            add("${profiles.size}:$name")
        }
        println("Profiles: $profiles")
    }

    init {
        // Will be removed next update
        if (mirror == Mirror.VENDETTA_ROCKS) mirror = Mirror.VENDETTA_ROCKS_ALT
    }

}

enum class Theme(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class UpdateCheckerDuration(@StringRes val labelRes: Int, val time: Long, val unit: TimeUnit) {
    DISABLED(R.string.duration_disabled, 0, TimeUnit.SECONDS),
    QUARTERLY(R.string.duration_fifteen_min, 15, TimeUnit.MINUTES),
    HALF_HOUR(R.string.duration_half_hour, 30, TimeUnit.MINUTES),
    HOURLY(R.string.duration_hourly, 1, TimeUnit.HOURS),
    BIHOURLY(R.string.duration_bihourly, 2, TimeUnit.HOURS),
    TWICE_DAILY(R.string.duration_twice_daily, 12, TimeUnit.HOURS),
    DAILY(R.string.duration_daily, 1, TimeUnit.DAYS),
    WEEKLY(R.string.duration_weekly, 7, TimeUnit.DAYS)
}

enum class Mirror(val baseUrl: String) {
    DEFAULT("https://tracker.vendetta.rocks"),
    VENDETTA_ROCKS("https://proxy.vendetta.rocks"), // Temporarily added for compatibility
    VENDETTA_ROCKS_ALT("https://proxy.vendetta.rocks"),
    K6("https://vd.k6.tf"),
    NEXPID("https://tracker.vd.nexpid.xyz")
}

enum class InstallMethod(@StringRes val labelRes: Int) {
    DEFAULT(R.string.default_installer),
    SHIZUKU(R.string.shizuku_installer)
}