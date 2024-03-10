package dev.selfmadesystem.dmm.installer.step.patching

import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.installer.step.Step
import dev.selfmadesystem.dmm.installer.step.StepGroup
import dev.selfmadesystem.dmm.installer.step.StepRunner
import dev.selfmadesystem.dmm.installer.step.download.DownloadVendettaStep
import dev.selfmadesystem.dmm.installer.util.Patcher
import java.io.File

/**
 * Uses LSPatch to inject the Vendetta XPosed module into Discord
 *
 * @param signedDir The signed apks to patch
 * @param lspatchedDir Output directory for LSPatch
 */
class AddVendettaStep(
    private val signedDir: File,
    private val lspatchedDir: File
) : Step() {

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_add_vd

    override suspend fun run(runner: StepRunner) {
        val vendetta = runner.getCompletedStep<DownloadVendettaStep>().workingCopy

        runner.logger.i("Adding Vendetta module with LSPatch")
        val files = signedDir.listFiles()
            ?.takeIf { it.isNotEmpty() }
            ?: throw Error("Missing APKs from signing step")

        Patcher.patch(
            runner.logger,
            outputDir = lspatchedDir,
            apkPaths = files.map { it.absolutePath },
            embeddedModules = listOf(vendetta.absolutePath)
        )
    }

}