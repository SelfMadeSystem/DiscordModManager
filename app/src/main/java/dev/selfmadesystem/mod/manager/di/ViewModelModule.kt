package dev.selfmadesystem.mod.manager.di

import dev.selfmadesystem.mod.manager.ui.viewmodel.home.HomeViewModel
import dev.selfmadesystem.mod.manager.ui.viewmodel.installer.InstallerViewModel
import dev.selfmadesystem.mod.manager.ui.viewmodel.installer.LogViewerViewModel
import dev.selfmadesystem.mod.manager.ui.viewmodel.libraries.LibrariesViewModel
import dev.selfmadesystem.mod.manager.ui.viewmodel.settings.AdvancedSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::AdvancedSettingsViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::LogViewerViewModel)
    factoryOf(::LibrariesViewModel)
}