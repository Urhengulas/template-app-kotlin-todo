package com.emission_meter.demo

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class EnergyMeasurementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val wattOfApp = WattOfApp(appContext)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // TODO: make iterations dependent on worker interval (min 15 minutes)
            // TODO: make measurement interval configurable
            for (i in 1..900) {
                delay(2000)
                val energyConsumption = measureEnergyConsumption(applicationContext)
                sendToMongoDB(energyConsumption)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG(), "$e")
            Result.failure()
        }
    }

    private fun measureEnergyConsumption(context: Context): Long {
        // Implement your energy measurement logic here
        // Example: val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return wattOfApp.get()
    }

    private fun sendToMongoDB(energyConsumption: Long) {
        // Implement MongoDB connection and data insertion logic here
        Log.i(TAG(), "$energyConsumption")
    }
}
