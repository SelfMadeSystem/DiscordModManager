package dev.selfmadesystem.dmm.installer.step.installing

import android.content.Context
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.InstallMethod
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.installer.Installer
import dev.selfmadesystem.dmm.installer.session.SessionInstaller
import dev.selfmadesystem.dmm.installer.shizuku.ShizukuInstaller
import dev.selfmadesystem.dmm.installer.step.Step
import dev.selfmadesystem.dmm.installer.step.StepGroup
import dev.selfmadesystem.dmm.installer.step.StepRunner
import dev.selfmadesystem.dmm.utils.isMiui
import org.koin.core.component.inject
import java.io.File

/**
 * Installs all the modified splits with the users desired [Installer]
 *
 * @see SessionInstaller
 * @see ShizukuInstaller
 *
 * @param lspatchedDir Where all the patched APKs are
 */
class InstallStep(
    private val lspatchedDir: File
) : Step() {

    private val preferences: PreferenceManager by inject()
    private val context: Context by inject()

    override val group = StepGroup.INSTALLING
    override val nameRes = R.string.step_installing

    override suspend fun run(runner: StepRunner) {
        runner.logger.i("Installing apks")
        val files = lspatchedDir.listFiles()
            ?.takeIf { it.isNotEmpty() }
            ?: throw Error("Missing APKs from LSPatch step; failure likely")

        val installer: Installer = when (preferences.installMethod) {
            InstallMethod.DEFAULT -> SessionInstaller(context)
            InstallMethod.SHIZUKU -> ShizukuInstaller(context)
        }

        installer.installApks(silent = !isMiui, *files)
    }

}