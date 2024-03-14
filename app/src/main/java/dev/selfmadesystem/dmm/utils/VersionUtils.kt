package dev.selfmadesystem.dmm.utils

import androidx.annotation.StringRes
import dev.selfmadesystem.dmm.R
import java.io.Serializable

data class DiscordVersion(
    val major: Int,
    val minor: Int,
    val type: Type
) : Serializable, Comparable<DiscordVersion> {

    @kotlinx.serialization.Serializable
    enum class Type(val label: String, @StringRes val labelRes: Int) {
        STABLE("Stable", R.string.channel_stable),
        BETA("Beta", R.string.channel_beta),
        ALPHA("Alpha", R.string.channel_alpha)
    }

    override fun compareTo(other: DiscordVersion): Int =
        toVersionCode().toInt() - other.toVersionCode().toInt()

    override fun toString() = "$major.$minor - ${type.label}"

    fun toVersionCode() = "$major${type.ordinal}${if (minor < 10) 0 else ""}${minor}"

    companion object {

        fun fromVersionCode(string: String): DiscordVersion? = with(string) {
            if (length < 4) return@with null
            if (toIntOrNull() == null) return@with null
            if (toInt() <= 126021) return@with null
            val codeReversed = toCharArray().reversed().joinToString("")
            val typeInt = codeReversed[2].toString().toInt()
            val type = Type.values().getOrNull(typeInt) ?: return@with null
            DiscordVersion(
                codeReversed.slice(3..codeReversed.lastIndex).reversed().toInt(),
                codeReversed.substring(0, 2).reversed().toInt(),
                type
            )
        }

    }

}