package dev.selfmadesystem.mod.manager.installer.step.download

import androidx.compose.runtime.Stable
import dev.selfmadesystem.mod.manager.R
import dev.selfmadesystem.mod.manager.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the languages split, will always be English because Discord doesn't store their strings in this split
 */
@Stable
class DownloadLangStep(
    dir: File,
    workingDir: File,
    version: String
): DownloadStep() {

    override val nameRes = R.string.step_dl_lang

    override val url: String = "$baseUrl/tracker/download/$version/config.en"
    override val destination = dir.resolve("config.en-$version.apk")
    override val workingCopy = workingDir.resolve("config.en-$version.apk")

}