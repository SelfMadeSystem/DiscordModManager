package dev.selfmadesystem.dmm.di

import dev.selfmadesystem.dmm.ui.viewmodel.home.HomeViewModel
import dev.selfmadesystem.dmm.ui.viewmodel.installer.InstallerViewModel
import dev.selfmadesystem.dmm.ui.viewmodel.installer.LogViewerViewModel
import dev.selfmadesystem.dmm.ui.viewmodel.libraries.LibrariesViewModel
import dev.selfmadesystem.dmm.ui.viewmodel.settings.AdvancedSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::AdvancedSettingsViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::LogViewerViewModel)
    factoryOf(::LibrariesViewModel)
}