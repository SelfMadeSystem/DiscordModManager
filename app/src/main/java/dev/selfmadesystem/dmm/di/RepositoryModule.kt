package dev.selfmadesystem.dmm.di

import dev.selfmadesystem.dmm.domain.repository.RestRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::RestRepository)
}