package dev.selfmadesystem.dmm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.selfmadesystem.dmm.di.httpModule
import dev.selfmadesystem.dmm.di.managerModule
import dev.selfmadesystem.dmm.di.repositoryModule
import dev.selfmadesystem.dmm.di.viewModelModule
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.domain.manager.UpdateCheckerDuration
import dev.selfmadesystem.dmm.updatechecker.worker.UpdateWorker
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initNotificationChannels()

        startKoin {
            androidContext(this@ManagerApplication)
            modules(
                httpModule,
                managerModule,
                viewModelModule,
                repositoryModule
            )
        }

        val prefs: PreferenceManager = get()

        if (prefs.updateDuration != UpdateCheckerDuration.DISABLED) {
            val duration = prefs.updateDuration
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "dev.selfmadesystem.dmm.UPDATE_CHECK",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UpdateWorker>(duration.time, duration.unit).build()
            )
        }
    }

    private fun initNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val updates = NotificationChannel(
            "${BuildConfig.APPLICATION_ID}.notifications.UPDATE",
            "Discord updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        nm.createNotificationChannel(updates)
    }

}