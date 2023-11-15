package com.emission_meter.demo.energy_monitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Process
import android.util.Log
import com.emission_meter.demo.TAG
import kotlin.math.abs


class EnergyOfApp constructor(context: Context?) {
    private val cpuUsage = CpuUsage()
    private val totalWatt = TotalWatt(context)

    /**
     * Return the energy consumption of the current app in mW.
     */
    fun energy(): Double {
        val wattTotal = totalWatt.get().toDouble()
        val cpu = cpuUsage.get()
        val wattOfApp = wattTotal * cpu
        Log.d(TAG(), "watt_total=$wattTotal cpu=$cpu watt_app=$wattOfApp time=${time()}")
        return wattOfApp
    }

    /**
     * Unix time of last measurement in seconds
     */
    fun time(): Long {
        return cpuUsage.timeUnix / 1000
    }
}


class CpuUsage {
    private val numCpu = 1 // TODO: Runtime.getRuntime().availableProcessors()
    private var timeCpu = Process.getElapsedCpuTime() // ms
    var timeUnix = System.currentTimeMillis() // ms

    /**
     * Get the average CPU usage of the current app.
     *
     * It is the average usage since the last time `.get` was called or since the creation of the
     * object, if it is the first time `.get` is called.
     */
    fun get(): Double {
        val newTimeCpu = Process.getElapsedCpuTime()
        val newTimeUnix = System.currentTimeMillis()

        val dTimeCpu = newTimeCpu - timeCpu
        val dTimeUnix = newTimeUnix - timeUnix

        timeCpu = newTimeCpu
        timeUnix = newTimeUnix

        return dTimeCpu.toDouble() / dTimeUnix.toDouble() / numCpu.toDouble()
    }
}

class TotalWatt constructor(context: Context?) {
    private val bm = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val intent =
        context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    /**
     * Instantaneous battery power consumption in mW (milli Watt).
     *
     * The returned value is always positive.
     * Returns `Long.MIN_VALUE` in case of an error.
     * There is an error if necessary values cannot be accessed, or if the battery is charging.
     */
    fun get(): Long {
        val microAmpere = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val milliVolt = getVoltage()

        // Error handling
        if (microAmpere == Long.MIN_VALUE) {
            // the platform does not provide BATTERY_PROPERTY_CURRENT_NOW
            return Long.MIN_VALUE
        } else if (microAmpere > 0) {
            // the battery is charging, therefore we cannot measure power consumption
            return Long.MIN_VALUE
        } else if (milliVolt == -1) {
            // intent does not contain EXTRA_VOLTAGE
            return Long.MIN_VALUE
        }

        val nanoWatt = microAmpere * milliVolt
        val milliWatt = nanoWatt / 1_000_000
        return abs(milliWatt)
    }

    private fun getVoltage(): Int {
        return intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
    }
}



