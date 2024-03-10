package dev.selfmadesystem.dmm.domain.manager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.selfmadesystem.dmm.installer.session.InstallService

class InstallManager(
    private val context: Context,
    private val prefs: PreferenceManager,
) {

    var current by mutableStateOf<PackageInfo?>(null)

    init {
        getInstalled()
    }

    fun getInstalled() {
        current = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    context.packageManager.getPackageInfo(
                        prefs.packageName.ifBlank { "dev.selfmadesystem.mod" },
                        PackageManager.PackageInfoFlags.of(
                            0L
                        )
                    )
                }

                else -> {
                    context.packageManager.getPackageInfo(
                        prefs.packageName.ifBlank { "dev.selfmadesystem.mod" },
                        0
                    )
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun uninstall() {
        current?.let {
            val callbackIntent = Intent(context, InstallService::class.java).apply {
                action = "dmm.actions.ACTION_UNINSTALL"
            }

            @SuppressLint("UnspecifiedImmutableFlag")
            val contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getService(context, 0, callbackIntent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getService(context, 0, callbackIntent, 0)
            }

            context.packageManager.packageInstaller.uninstall(
                it.packageName,
                contentIntent.intentSender
            )
        }
    }
}