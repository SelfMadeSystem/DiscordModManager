package dev.selfmadesystem.mod.manager.installer.session

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.IBinder
import dev.selfmadesystem.mod.manager.R
import dev.selfmadesystem.mod.manager.ui.activity.MainActivity
import dev.selfmadesystem.mod.manager.utils.showToast

class InstallService : Service() {

    private val messages = mapOf(
        PackageInstaller.STATUS_FAILURE to R.string.install_fail_generic,
        PackageInstaller.STATUS_FAILURE_BLOCKED to R.string.install_fail_blocked,
        PackageInstaller.STATUS_FAILURE_INVALID to R.string.install_fail_invalid,
        PackageInstaller.STATUS_FAILURE_CONFLICT to R.string.install_fail_conflict,
        PackageInstaller.STATUS_FAILURE_STORAGE to R.string.install_fail_storage,
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE to R.string.install_fail_incompatible,
        8 /* STATUS_FAILURE_TIMEOUT (Added in Android 14) */ to R.string.install_fail_timeout
    )

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val isInstall = intent.action == "vendetta.actions.ACTION_INSTALL"
        when (val statusCode = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION") // No.
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(confirmationIntent)
            }

            PackageInstaller.STATUS_SUCCESS -> if (isInstall) showToast(R.string.installer_success)

            PackageInstaller.STATUS_FAILURE_ABORTED -> if (isInstall) showToast(R.string.installer_aborted)

            else -> {
                if (isInstall) {
                    messages[statusCode]?.let(::showToast)

                    // Send error messages back to the activity for debugging (to be received by InstallerScreen)
                    startActivity(
                        Intent("vendetta.actions.ACTION_INSTALL_FINISHED").apply {
                            setClass(this@InstallService, MainActivity::class.java)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("vendetta.extras.EXTRA_MESSAGE", intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE))
                        }
                    )
                }
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

}