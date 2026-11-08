package com.athimue.pleinmalin

import android.app.Application
import com.athimue.pleinmalin.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PleinMalinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            // Le Context servira dès l'étape géolocalisation / Room.
            androidContext(this@PleinMalinApplication)
        }
    }
}
