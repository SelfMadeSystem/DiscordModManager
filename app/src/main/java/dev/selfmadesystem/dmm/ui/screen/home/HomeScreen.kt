package dev.selfmadesystem.dmm.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.selfmadesystem.dmm.BuildConfig
import dev.selfmadesystem.dmm.R
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.domain.manager.ProfileManager
import dev.selfmadesystem.dmm.ui.components.SegmentedButton
import dev.selfmadesystem.dmm.ui.screen.installer.InstallerScreen
import dev.selfmadesystem.dmm.ui.screen.settings.SettingsScreen
import dev.selfmadesystem.dmm.ui.viewmodel.home.HomeViewModel
import dev.selfmadesystem.dmm.ui.widgets.AppIcon
import dev.selfmadesystem.dmm.ui.widgets.dialog.StoragePermissionsDialog
import dev.selfmadesystem.dmm.ui.widgets.home.CommitList
import dev.selfmadesystem.dmm.ui.widgets.updater.UpdateDialog
import dev.selfmadesystem.dmm.utils.Constants
import dev.selfmadesystem.dmm.utils.DiscordVersion
import dev.selfmadesystem.dmm.utils.navigate
import org.koin.androidx.compose.get

class HomeScreen : Screen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val prefs: PreferenceManager = get()
        val viewModel: HomeViewModel = getScreenModel()

        val currentVersion = remember {
            DiscordVersion.fromVersionCode(viewModel.installManager.current?.versionCode.toString())
        }

        val latestVersion =
            remember(prefs.discordVersion, viewModel.discordVersions, prefs.channel) {
                when {
                    prefs.discordVersion.isBlank() -> viewModel.discordVersions?.get(prefs.channel)
                    else -> DiscordVersion.fromVersionCode(prefs.discordVersion)
                }
            }

        // == Dialogs == //

        StoragePermissionsDialog()

        if (
            viewModel.showUpdateDialog &&
            viewModel.release != null &&
            !BuildConfig.DEBUG
        ) {
            UpdateDialog(
                release = viewModel.release!!,
                isUpdating = viewModel.isUpdating,
                onDismiss = { viewModel.showUpdateDialog = false },
                onConfirm = {
                    viewModel.downloadAndInstallUpdate()
                }
            )
        }

        // == Screen == //

        Scaffold(
            topBar = { TitleBar() },
        ) { pv ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(pv)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                AppIcon(
                    customIcon = prefs.patchIcon,
                    releaseChannel = prefs.channel,
                    modifier = Modifier.size(60.dp)
                )

                Text(
                    text = prefs.appName,
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedVisibility(visible = currentVersion != null) {
                        Text(
                            text = stringResource(
                                R.string.version_current,
                                currentVersion.toString()
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = LocalContentColor.current.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }

                    val latestLabel =
                        if (prefs.discordVersion.isNotBlank()) R.string.version_target else R.string.version_latest

                    AnimatedVisibility(visible = latestVersion != null) {
                        Text(
                            text = stringResource(latestLabel, latestVersion.toString()),
                            style = MaterialTheme.typography.labelLarge,
                            color = LocalContentColor.current.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        navigator.navigate(InstallerScreen(latestVersion!!))
                    },
                    enabled = latestVersion != null && latestVersion >= (currentVersion
                        ?: Constants.DUMMY_VERSION),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val label = when {
                        latestVersion == null -> R.string.msg_loading
                        currentVersion == null -> R.string.action_install
                        currentVersion == latestVersion -> R.string.action_reinstall
                        latestVersion > currentVersion -> R.string.action_update
                        else -> R.string.msg_downgrade
                    }

                    Text(
                        text = stringResource(label),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .basicMarquee()
                            .fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = viewModel.installManager.current != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        SegmentedButton(
                            icon = Icons.Filled.OpenInNew,
                            text = stringResource(R.string.action_launch),
                            onClick = { viewModel.launchDiscord() }
                        )
                        SegmentedButton(
                            icon = Icons.Filled.Info,
                            text = stringResource(R.string.action_info),
                            onClick = { viewModel.launchDiscordInfo() }
                        )
                        SegmentedButton(
                            icon = Icons.Filled.Delete,
                            text = stringResource(R.string.action_uninstall),
                            onClick = { viewModel.uninstallDiscord() }
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CommitList(
                        commits = viewModel.commits.collectAsLazyPagingItems()
                    )
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun TitleBar() {
        val prefs: PreferenceManager = get()
        val profiles by remember {
            mutableStateOf(
                prefs.profiles + Pair(
                    "Create New Profile",
                    ProfileManager()
                )
            )
        } // TODO: Add string resource
        var selectedProfile by remember { mutableIntStateOf(prefs.currentProfile) }
        var expanded by remember { mutableStateOf(false) }
        val navigation = LocalNavigator.currentOrThrow

        TopAppBar(
            title = {
                Box(
                    modifier = Modifier
                        .clickable { expanded = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing)
                    ) {
                        val iconRotation by animateFloatAsState(
                            if (expanded) -180f else 0f,
                            label = "Icon Rotation"
                        )
                        Text(
                            text = profiles[selectedProfile].first
                        )
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = "Arrow Icon",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(iconRotation)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        profiles.forEachIndexed { index, profile ->
                            DropdownMenuItem(
                                text = { Text(text = profile.first) },
                                onClick = {
                                    assert(index < profiles.size)
                                    assert(index >= 0)
                                    if (index == profiles.size - 1) {
                                        prefs.addProfile(
                                            "New Profile",
                                            prefs.getCurrentProfile().second
                                        )
                                        navigation.navigate(SettingsScreen())
                                    }
                                    selectedProfile = index
                                    prefs.currentProfile = index
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            actions = { Actions() }
        )
    }

    @Composable
    private fun Actions() {
        val viewModel: HomeViewModel = getScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        IconButton(onClick = { viewModel.getDiscordVersions() }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.action_reload)
            )
        }
        IconButton(onClick = { navigator.navigate(SettingsScreen()) }) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.action_open_about)
            )
        }
    }

}