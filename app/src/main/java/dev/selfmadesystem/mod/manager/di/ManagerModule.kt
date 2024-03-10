package dev.selfmadesystem.mod.manager.di

import dev.selfmadesystem.mod.manager.domain.manager.DownloadManager
import dev.selfmadesystem.mod.manager.domain.manager.InstallManager
import dev.selfmadesystem.mod.manager.domain.manager.PreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::DownloadManager)
    singleOf(::PreferenceManager)
    singleOf(::InstallManager)
}