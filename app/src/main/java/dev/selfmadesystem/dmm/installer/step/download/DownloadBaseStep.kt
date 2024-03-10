package dev.selfmadesystem.dmm.installer.step.download

import androidx.compose.runtime.Stable
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the base Discord APK
 */
@Stable
class DownloadBaseStep(
    dir: File,
    workingDir: File,
    version: String
) : DownloadStep() {

    override val nameRes = R.string.step_dl_base

    override val url: String = "$baseUrl/tracker/download/$version/base"
    override val destination = dir.resolve("base-$version.apk")
    override val workingCopy = workingDir.resolve("base-$version.apk")

}