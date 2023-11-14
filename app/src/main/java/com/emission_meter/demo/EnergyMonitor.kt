package com.emission_meter.demo

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object EnergyMonitor {

    private const val ENERGY_WORK_TAG = "ENERGY_WORK_TAG"

    fun initialize(context: Context, measurementIntervalMs: Long = 1000) {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        val energyWorkRequest = PeriodicWorkRequestBuilder<EnergyMeasurementWorker>(
            measurementIntervalMs, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                ENERGY_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                energyWorkRequest
            )
    }

    fun stop(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ENERGY_WORK_TAG)
    }
}
