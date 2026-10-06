package com.liquilabs.vankoo.investor

import android.app.Application
import com.liquilabs.vankoo.investor.core.di.startKoinAndroid

class VankooApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinAndroid(this)
    }
}
