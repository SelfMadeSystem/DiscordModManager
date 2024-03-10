package dev.selfmadesystem.dmm.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.Mirror
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.ui.components.settings.SettingsButton
import dev.selfmadesystem.dmm.ui.components.settings.SettingsItemChoice
import dev.selfmadesystem.dmm.ui.components.settings.SettingsSwitch
import dev.selfmadesystem.dmm.ui.viewmodel.settings.AdvancedSettingsViewModel
import dev.selfmadesystem.dmm.utils.DimenUtils
import org.koin.androidx.compose.get

class AdvancedSettings : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val ctx = LocalContext.current
        val prefs: PreferenceManager = get()
        val viewModel: AdvancedSettingsViewModel = getScreenModel()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            topBar = { TitleBar(scrollBehavior) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { pv ->
            Column(
                modifier = Modifier
                    .padding(pv)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = DimenUtils.navBarPadding)
            ) {
                SettingsItemChoice(
                    label = stringResource(R.string.settings_check_updates),
                    pref = prefs.updateDuration,
                    labelFactory = {
                        ctx.getString(it.labelRes)
                    },
                    onPrefChange = {
                        prefs.updateDuration = it
                        viewModel.updateCheckerDuration(it)
                    }
                )

                SettingsItemChoice(
                    label = stringResource(R.string.settings_mirror),
                    pref = prefs.mirror,
                    excludedOptions = listOf(Mirror.VENDETTA_ROCKS),
                    labelFactory = {
                        it.baseUrl.toUri().authority ?: it.baseUrl
                    },
                    onPrefChange = {
                        prefs.mirror = it
                    }
                )

                SettingsItemChoice(
                    label = stringResource(R.string.install_method),
                    pref = prefs.installMethod,
                    labelFactory = {
                        ctx.getString(it.labelRes)
                    },
                    onPrefChange = viewModel::setInstallMethod,
                )

                SettingsSwitch(
                    label = stringResource(R.string.settings_auto_clear_cache),
                    secondaryLabel = stringResource(R.string.settings_auto_clear_cache_description),
                    pref = prefs.autoClearCache,
                    onPrefChange = {
                        prefs.autoClearCache = it
                    }
                )

                SettingsButton(
                    label = stringResource(R.string.action_clear_cache),
                    onClick = {
                        viewModel.clearCache()
                    }
                )
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun TitleBar(
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        val navigator = LocalNavigator.currentOrThrow

        LargeTopAppBar(
            title = {
                Text(stringResource(R.string.settings_advanced))
            },
            navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }

}