package com.emission_meter.demo

import android.app.Application
import android.util.Log
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration

lateinit var app: App

// global Kotlin extension that resolves to the short version
// of the name of the current class. Used for labelling logs.
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
*  Sets up the App and enables Realm-specific logging in debug mode.
*/
class TemplateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        app = App.create(
            AppConfiguration.Builder(getString(R.string.realm_app_id))
                .baseUrl(getString(R.string.realm_base_url))
                .build()
        )
        Log.v(TAG(), "Initialized the App configuration for: ${app.configuration.appId}")
        Log.v(TAG(), "To see your data in Atlas, follow this link:" + getString(R.string.realm_data_explorer_link))

        // Initialize the energy monitor when the application starts
        EnergyMonitor.initialize(applicationContext)
        Log.i(TAG(), "Initialized Energy Monitor")
    }

    override fun onTerminate() {
        super.onTerminate()

        // Stop the energy monitor when the application terminates
        EnergyMonitor.stop(applicationContext)
    }
}
