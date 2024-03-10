package dev.selfmadesystem.dmm.installer.step.download

import androidx.compose.runtime.Stable
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the Vendetta XPosed module
 *
 * https://github.com/vendetta-mod/VendettaXposed
 */
@Stable
class DownloadVendettaStep(
    workingDir: File
) : DownloadStep() {

    override val nameRes = R.string.step_dl_vd

    override val url: String =
        "https://github.com/vendetta-mod/VendettaXposed/releases/latest/download/app-release.apk"
    override val destination = preferenceManager.moduleLocation
    override val workingCopy = workingDir.resolve("vendetta.apk")

}