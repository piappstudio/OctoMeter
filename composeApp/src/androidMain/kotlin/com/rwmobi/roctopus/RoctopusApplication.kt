/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.roctopus

import android.app.Application
import com.rwmobi.roctopus.di.appModule
import com.rwmobi.roctopus.di.ktorModule
import com.rwmobi.roctopus.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class RoctopusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RoctopusApplication)
            modules(
                appModule,
                ktorModule,
                repositoryModule,
            )
        }
    }
}