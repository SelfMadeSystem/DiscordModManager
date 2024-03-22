package dev.selfmadesystem.dmm.updatechecker.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.selfmadesystem.dmm.domain.manager.InstallManager
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.domain.manager.ProfileManager
import dev.selfmadesystem.dmm.domain.repository.RestRepository
import dev.selfmadesystem.dmm.network.utils.ApiResponse
import dev.selfmadesystem.dmm.updatechecker.reciever.UpdateBroadcastReceiver
import dev.selfmadesystem.dmm.utils.DiscordVersion
import dev.selfmadesystem.dmm.utils.Intents
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    val api: RestRepository by inject()
    val prefs: PreferenceManager by inject()
    val installManager: InstallManager by inject()

    suspend fun doWorkForProfile(profile: ProfileManager): Result {
        if (profile.discordVersion.isNotBlank()) return Result.success()
        return when (val res = api.getLatestDiscordVersions()) {
            is ApiResponse.Success -> {
                val currentVersion =
                    DiscordVersion.fromVersionCode(installManager.current?.versionCode.toString())
                val latestVersion = res.data[profile.channel]

                if (latestVersion == null || currentVersion == null) return Result.failure()

                if (latestVersion > currentVersion) {
                    context.sendBroadcast(
                        Intent(
                            context,
                            UpdateBroadcastReceiver::class.java
                        ).apply {
                            putExtra(Intents.Extras.VERSION, latestVersion.toVersionCode())
                        })
                }

                Result.success()
            }

            else -> Result.failure()
        }
    }

    override suspend fun doWork(): Result {
        val profiles = prefs.profiles
        if (profiles.isEmpty()) return Result.failure()

        for (profile in profiles) {
            doWorkForProfile(profile.second)
        }

        return Result.success()
    }

}