package com.hs.accessibility

import android.app.Application
import timber.log.Timber
import kotlin.properties.Delegates

class AccessibilityServiceApp : Application() {

    companion object {
        var instance: AccessibilityServiceApp by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
