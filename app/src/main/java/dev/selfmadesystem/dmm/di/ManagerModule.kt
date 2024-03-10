package dev.selfmadesystem.dmm.di

import dev.selfmadesystem.dmm.domain.manager.DownloadManager
import dev.selfmadesystem.dmm.domain.manager.InstallManager
import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::DownloadManager)
    singleOf(::PreferenceManager)
    singleOf(::InstallManager)
}