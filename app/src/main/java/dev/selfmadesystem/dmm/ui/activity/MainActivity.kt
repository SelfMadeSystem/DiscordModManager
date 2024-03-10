package dev.selfmadesystem.dmm.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.selfmadesystem.dmm.domain.manager.InstallMethod
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.installer.shizuku.ShizukuPermissions
import dev.selfmadesystem.dmm.ui.screen.home.HomeScreen
import dev.selfmadesystem.dmm.ui.screen.installer.InstallerScreen
import dev.selfmadesystem.dmm.ui.theme.DMMTheme
import dev.selfmadesystem.dmm.utils.DiscordVersion
import dev.selfmadesystem.dmm.utils.Intents
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val preferences: PreferenceManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val version = intent.getStringExtra(Intents.Extras.VERSION)

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                0
            )
        }

        if (preferences.installMethod == InstallMethod.SHIZUKU) {
            lifecycleScope.launch {
                if (!ShizukuPermissions.waitShizukuPermissions()) {
                    preferences.installMethod = InstallMethod.DEFAULT
                }
            }
        }

        val screen = if (intent.action == Intents.Actions.INSTALL && version != null) {
            InstallerScreen(DiscordVersion.fromVersionCode(version)!!)
        } else {
            HomeScreen()
        }

        setContent {
            DMMTheme {
                Navigator(screen) {
                    SlideTransition(it)
                }
            }
        }
    }

}
