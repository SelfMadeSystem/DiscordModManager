package dev.selfmadesystem.mod.manager.di

import dev.selfmadesystem.mod.manager.domain.repository.RestRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::RestRepository)
}