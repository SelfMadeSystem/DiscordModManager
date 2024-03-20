package dev.selfmadesystem.dmm.domain.manager

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.StringRes
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.base.BasePreferenceManager
import dev.selfmadesystem.dmm.utils.DiscordVersion
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
data class ProfileManager(
    var version: Int = 1,

    // Discord mod
    var packageName: String = "dev.selfmadesystem.bunny",
    var appName: String = "Bunny",
    var appIconFileName: String = "bunny",
    var channel: DiscordVersion.Type = DiscordVersion.Type.STABLE,
    var discordVersion: String = "",
    var moduleVersion: String = "",
    var debuggable: Boolean = false,
    var moduleGetStrategy: ModuleGetStrategy = ModuleGetStrategy.Url("https://raw.githubusercontent.com/pyoncord/detta-builds/main/bunny.js"),
    var installMethod: InstallMethod = InstallMethod.DEFAULT,

    // Theme and misc mod manager related
    var monet: Boolean = false,
    var autoClearCache: Boolean = true,
    var theme: Theme = Theme.SYSTEM,
    var updateDuration: UpdateCheckerDuration = UpdateCheckerDuration.HOURLY,
)

class ProfileManagerSerializer : KSerializer<ProfileManager> {
    override val descriptor = ProfileManager.serializer().descriptor
    override fun serialize(encoder: Encoder, value: ProfileManager) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.version)
        composite.encodeStringElement(descriptor, 1, value.packageName)
        composite.encodeStringElement(descriptor, 2, value.appName)
        composite.encodeStringElement(descriptor, 3, value.appIconFileName)
        composite.encodeSerializableElement(
            descriptor,
            4,
            DiscordVersion.Type.serializer(),
            value.channel
        )
        composite.encodeStringElement(descriptor, 5, value.discordVersion)
        composite.encodeStringElement(descriptor, 6, value.moduleVersion)
        composite.encodeBooleanElement(descriptor, 7, value.debuggable)
        composite.encodeSerializableElement(
            descriptor,
            8,
            ModuleGetStrategy.serializer(),
            value.moduleGetStrategy
        )
        composite.encodeSerializableElement(
            descriptor,
            9,
            InstallMethod.serializer(),
            value.installMethod
        )
        composite.encodeBooleanElement(descriptor, 10, value.monet)
        composite.encodeBooleanElement(descriptor, 11, value.autoClearCache)
        composite.encodeSerializableElement(descriptor, 12, Theme.serializer(), value.theme)
        composite.encodeSerializableElement(
            descriptor,
            13,
            UpdateCheckerDuration.serializer(),
            value.updateDuration
        )
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ProfileManager {
        val composite = decoder.beginStructure(descriptor)
        var version = 1
        var packageName = "dev.selfmadesystem.bunny"
        var appName = "Bunny"
        var appIconFileName = "bunny"
        var channel = DiscordVersion.Type.STABLE
        var discordVersion = ""
        var moduleVersion = ""
        var debuggable = false
        var moduleGetStrategy: ModuleGetStrategy =
            ModuleGetStrategy.Url("https://raw.githubusercontent.com/pyoncord/detta-builds/main/bunny.js")
        var installMethod = InstallMethod.DEFAULT
        var monet = false
        var autoClearCache = true
        var theme = Theme.SYSTEM
        var updateDuration = UpdateCheckerDuration.HOURLY
        while (true) {
            when (composite.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break
                0 -> version = composite.decodeIntElement(descriptor, 0)
                1 -> packageName = composite.decodeStringElement(descriptor, 1)
                2 -> appName = composite.decodeStringElement(descriptor, 2)
                3 -> appIconFileName = composite.decodeStringElement(descriptor, 3)
                4 -> channel = composite.decodeSerializableElement(
                    descriptor,
                    4,
                    DiscordVersion.Type.serializer()
                )

                5 -> discordVersion = composite.decodeStringElement(descriptor, 5)
                6 -> moduleVersion = composite.decodeStringElement(descriptor, 6)
                7 -> debuggable = composite.decodeBooleanElement(descriptor, 7)
                8 -> moduleGetStrategy = composite.decodeSerializableElement(
                    descriptor,
                    8,
                    ModuleGetStrategy.serializer()
                )

                9 -> installMethod =
                    composite.decodeSerializableElement(descriptor, 9, InstallMethod.serializer())

                10 -> monet = composite.decodeBooleanElement(descriptor, 10)
                11 -> autoClearCache = composite.decodeBooleanElement(descriptor, 11)
                12 -> theme =
                    composite.decodeSerializableElement(descriptor, 12, Theme.serializer())

                13 -> updateDuration = composite.decodeSerializableElement(
                    descriptor,
                    13,
                    UpdateCheckerDuration.serializer()
                )
            }
        }
        composite.endStructure(descriptor)
        return ProfileManager(
            version,
            packageName,
            appName,
            appIconFileName,
            channel,
            discordVersion,
            moduleVersion,
            debuggable,
            moduleGetStrategy,
            installMethod,
            monet,
            autoClearCache,
            theme,
            updateDuration
        )
    }
}

class PreferenceManager(var context: Context) :
    BasePreferenceManager(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)) {

    protected fun profileManagersPreference(
        key: String,
        defaultValue: List<Pair<String, ProfileManager>> = emptyList()
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = { key, defaultValue ->
            getStringOrNull(key, null)?.let {
                val serializer = ProfileManagerSerializer()
                val pairSerializer = PairSerializer(String.serializer(), serializer)
                val listPairSerializer = ListSerializer(pairSerializer)
                val decoder = Json {
                    serializersModule = SerializersModule {
                        contextual(ProfileManager::class, serializer)
                    }
                }
                decoder.decodeFromString(listPairSerializer, it)
            } ?: defaultValue
        },
        setter = { key, newValue ->
            val serializer = ProfileManagerSerializer()
            val pairSerializer = PairSerializer(String.serializer(), serializer)
            val listPairSerializer = ListSerializer(pairSerializer)
            val encoder = Json {
                serializersModule = SerializersModule {
                    contextual(ProfileManager::class, serializer)
                }
            }
            putString(key, encoder.encodeToString(listPairSerializer, newValue))
        }
    )

    val DEFAULT_MODULE_LOCATION =
        (context.externalCacheDir ?: File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_DOWNLOADS
        ).resolve("VendettaManager").also { it.mkdirs() }).resolve("vendetta.apk")

    var moduleVersion by stringPreference("module_version", "")

    var patchIcon by booleanPreference("patch_icon", true)

    var mirror by enumPreference("mirror", Mirror.DEFAULT)

    var monet by booleanPreference("monet", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    var isDeveloper by booleanPreference("is_developer", false)

    var autoClearCache by booleanPreference("auto_clear_cache", true)

    var theme by enumPreference("theme", Theme.SYSTEM)

    var updateDuration by enumPreference("update_duration", UpdateCheckerDuration.HOURLY)

    var moduleLocation by filePreference("module_location", DEFAULT_MODULE_LOCATION)

    var installMethod by enumPreference("install_method", InstallMethod.DEFAULT)

    var logsAlternateBackground by booleanPreference("logs_alternate_bg", true)

    var logsLineWrap by booleanPreference("logs_line_wrap", false)

    var profiles by profileManagersPreference("profiles", listOf("Bunny" to ProfileManager()))

    var profileIdx by intPreference("profile_idx", 0) { _, it ->
        _currentProfile = profiles[it].second
    }

    private var _currentProfile: ProfileManager = profiles[profileIdx].second

    var currentProfile: ProfileManager
        get() = _currentProfile
        set(value) {
            _currentProfile = value
            profiles = profiles.toMutableList().apply {
                set(profileIdx, profiles[profileIdx].first to value)
            }
        }

    fun getCurrentProfileName() = profiles[profileIdx].first

    fun setCurrentProfileName(name: String) {
        val profile = profiles[profileIdx].second
        profiles = profiles.toMutableList().apply {
            set(profileIdx, name to profile)
        }
    }

    fun addProfile(name: String, profile: ProfileManager) {
        profiles = profiles + (name to profile)
    }

    init {
        // Will be removed next update
        if (mirror == Mirror.VENDETTA_ROCKS) mirror = Mirror.VENDETTA_ROCKS_ALT
    }

}

@Serializable
enum class Theme(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

@Serializable
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

@Serializable
sealed class ModuleGetStrategy {
    companion object {
        fun fromString(string: String): ModuleGetStrategy {
            val (type, value) = string.split(":")
            return when (type) {
                "U" -> Url(value)
                "F" -> File(value)
                else -> throw IllegalArgumentException("Invalid module get strategy: $string")
            }
        }
    }

    @Serializable
    data class Url(val url: String) : ModuleGetStrategy() {
        override fun toString(): String = "U:$url"
    }

    @Serializable
    data class File(val file: String) : ModuleGetStrategy() {
        override fun toString(): String = "F:$file"
    }
}

enum class Mirror(val baseUrl: String) {
    DEFAULT("https://tracker.vendetta.rocks"),
    VENDETTA_ROCKS("https://proxy.vendetta.rocks"), // Temporarily added for compatibility
    VENDETTA_ROCKS_ALT("https://proxy.vendetta.rocks"),
    K6("https://vd.k6.tf"),
    NEXPID("https://tracker.vd.nexpid.xyz")
}

@Serializable
enum class InstallMethod(@StringRes val labelRes: Int) {
    DEFAULT(R.string.default_installer),
    SHIZUKU(R.string.shizuku_installer)
}