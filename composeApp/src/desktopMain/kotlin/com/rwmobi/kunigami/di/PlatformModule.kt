/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.di

import com.russhwolf.settings.Settings
import com.rwmobi.kunigami.data.source.local.preferences.provideSettings
import com.rwmobi.kunigami.ui.viewmodels.PlatformMainViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val platformModule = module {
    single<HttpClientEngine> { CIO.create() }
    single<Settings> { provideSettings() }
    factory {
        PlatformMainViewModel(
            userPreferencesRepository = get(),
            dispatcher = get(named("DefaultDispatcher")),
        )
    }
}
