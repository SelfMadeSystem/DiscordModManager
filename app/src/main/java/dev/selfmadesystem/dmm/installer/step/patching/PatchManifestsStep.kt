package dev.selfmadesystem.dmm.installer.step.patching

import com.github.diamondminer88.zip.ZipReader
import com.github.diamondminer88.zip.ZipWriter
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.installer.step.Step
import dev.selfmadesystem.dmm.installer.step.StepGroup
import dev.selfmadesystem.dmm.installer.step.StepRunner
import dev.selfmadesystem.dmm.installer.step.download.DownloadBaseStep
import dev.selfmadesystem.dmm.installer.step.download.DownloadLangStep
import dev.selfmadesystem.dmm.installer.step.download.DownloadLibsStep
import dev.selfmadesystem.dmm.installer.step.download.DownloadResourcesStep
import dev.selfmadesystem.dmm.installer.util.ManifestPatcher
import org.koin.core.component.inject

/**
 * Modifies each APKs manifest in order to change the package and app name as well as whether or not its debuggable
 */
class PatchManifestsStep : Step() {

    private val preferences: PreferenceManager by inject()

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_patch_manifests

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().workingCopy
        val libsApk = runner.getCompletedStep<DownloadLibsStep>().workingCopy
        val langApk = runner.getCompletedStep<DownloadLangStep>().workingCopy
        val resApk = runner.getCompletedStep<DownloadResourcesStep>().workingCopy
        val currentProfile = preferences.currentProfile

        arrayOf(baseApk, libsApk, langApk, resApk).forEach { apk ->
            runner.logger.i("Reading AndroidManifest.xml from ${apk.name}")
            val manifest = ZipReader(apk)
                .use { zip -> zip.openEntry("AndroidManifest.xml")?.read() }
                ?: throw IllegalStateException("No manifest in ${apk.name}")

            ZipWriter(apk, true).use { zip ->
                runner.logger.i("Changing package and app name in ${apk.name}")
                val patchedManifestBytes = if (apk == baseApk) {
                    ManifestPatcher.patchManifest(
                        manifestBytes = manifest,
                        packageName = currentProfile.packageName,
                        appName = currentProfile.appName,
                        debuggable = currentProfile.debuggable,
                    )
                } else {
                    runner.logger.i("Changing package name in ${apk.name}")
                    ManifestPatcher.renamePackage(manifest, currentProfile.packageName)
                }

                runner.logger.i("Deleting old AndroidManifest.xml in ${apk.name}")
                zip.deleteEntry(
                    "AndroidManifest.xml",
                    apk == libsApk
                ) // Preserve alignment in libs apk

                runner.logger.i("Adding patched AndroidManifest.xml in ${apk.name}")
                zip.writeEntry("AndroidManifest.xml", patchedManifestBytes)
            }
        }
    }

}