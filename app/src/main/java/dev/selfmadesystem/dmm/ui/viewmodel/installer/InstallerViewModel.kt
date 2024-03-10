package dev.selfmadesystem.dmm.ui.viewmodel.installer

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.selfmadesystem.dmm.BuildConfig
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.InstallManager
import dev.selfmadesystem.dmm.installer.step.Step
import dev.selfmadesystem.dmm.installer.step.StepGroup
import dev.selfmadesystem.dmm.installer.step.StepRunner
import dev.selfmadesystem.dmm.utils.DiscordVersion
import dev.selfmadesystem.dmm.utils.showToast
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class InstallerViewModel(
    private val context: Context,
    private val installManager: InstallManager,
    discordVersion: DiscordVersion
) : ScreenModel {

    val runner = StepRunner(discordVersion)

    val groupedSteps: ImmutableMap<StepGroup, List<Step>> = StepGroup.entries
        .associateWith { group ->
            runner.steps.filter { step -> step.group == group }
        }
        .toImmutableMap()

    private val tempLogStorageDir = context.filesDir.resolve("logsTmp").also {
        it.mkdirs()
    }

    private val logsString by lazy {
        runner.logger.logs.joinToString("\n") { it.toString() }
    }

    private val installationRunning = AtomicBoolean(false)

    private val job = screenModelScope.launch(Dispatchers.Main) {
        if (installationRunning.getAndSet(true)) {
            return@launch
        }

        withContext(Dispatchers.IO) {
            runner.runAll()
        }
    }

    var backDialogOpened by mutableStateOf(false)
        private set

    var expandedGroup by mutableStateOf<StepGroup?>(null)
        private set

    fun logError(msg: String?) {
        runner.logger.e("")
        runner.logger.e(msg)
    }

    fun clearCache() {
        runner.clearCache()
        context.showToast(R.string.msg_cleared_cache)
    }

    fun openBackDialog() {
        backDialogOpened = true
    }

    fun closeBackDialog() {
        backDialogOpened = false
    }

    fun dismissDownloadFailedDialog() {
        runner.downloadErrored = false
    }

    fun expandGroup(group: StepGroup?) {
        expandedGroup = group
    }

    fun launchVendetta() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun cancelInstall() {
        runCatching {
            job.cancel("User exited the installer")
        }
    }

    private fun saveToAppStorage(): File {
        // Delete old logs to prevent junk buildup
        tempLogStorageDir.deleteRecursively()
        tempLogStorageDir.mkdirs()

        val tmpFile = tempLogStorageDir.resolve("VD-Manager-${System.currentTimeMillis()}.log")
        tmpFile.outputStream().use { stream ->
            stream.write(logsString.toByteArray())
        }

        return tmpFile
    }

    fun shareLogs(activityContext: Context) {
        val saved = saveToAppStorage()
        val uri = FileProvider.getUriForFile(
            activityContext,
            BuildConfig.APPLICATION_ID + ".provider",
            saved
        )

        ShareCompat.IntentBuilder(activityContext)
            .setType("text/plain")
            .setStream(uri)
            .apply {
                intent.apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            .startChooser()
    }

}