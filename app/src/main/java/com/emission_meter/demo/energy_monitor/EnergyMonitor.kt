package com.emission_meter.demo.energy_monitor

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object EnergyMonitor {

    // TODO: After the worker gets killed (after 10 minutes) it won't start again, unless we
    //  change the tag.
    private const val ENERGY_WORK_TAG = "ENERGY_WORK_TAG10"

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
