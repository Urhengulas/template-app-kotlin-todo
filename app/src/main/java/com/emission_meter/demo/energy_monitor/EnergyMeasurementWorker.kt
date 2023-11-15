package com.emission_meter.demo.energy_monitor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emission_meter.demo.TAG
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mongodb.kbson.ObjectId

class EnergyMeasurementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val energyOfApp = EnergyOfApp(appContext)

    // input
    private val token = "ktSm7GdGAvctv6szOnoBlXhlXkF7lLB5uUoiBNiKrqtLG7bfwiduHJCaxkZxnXFR"
    private val realm_app_id = "energymeasurements-xjoze"

    // connect to mongo
    private var app2: App = App.create(AppConfiguration.Builder(realm_app_id).build())
    private val user = runBlocking { app2.login(Credentials.apiKey(token)) }
    private val config: SyncConfiguration = initConfig(user)
    private val realm: Realm = Realm.open(config)

    init {
        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            realm.subscriptions.waitForSynchronization()
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        // TODO: make iterations dependent on worker interval (min 15 minutes)
        // TODO: make measurement interval configurable
        for (i in 1..900) {
            try {
                delay(5000)
                sendToMongoDB(energyOfApp.energy(), energyOfApp.time())
            } catch (e: Exception) {
                Log.e(TAG(), "$e")
            }
        }
        Result.success()

    }

    private suspend fun sendToMongoDB(power: Double, time: Long) {
        val a = Energy().apply {
            this.power = power
            this.owner_id = user.id
            this.timestamp = RealmInstant.from(time, 0)
        }
        realm.write {
            copyToRealm(a, UpdatePolicy.ERROR)
        }

    }
}

class Energy() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var owner_id: String = ""
    var sensor: String = "android"
    var target: String = ""
    var timestamp =  RealmInstant.MIN
    var power: Double = 0.0
}

fun initConfig(user: User): SyncConfiguration {
    return SyncConfiguration
        .Builder(user, setOf(Energy::class))
        .initialSubscriptions { realm ->
            add(
                // TODO: only subscribe to N most recent elements
                realm.query<Energy>("owner_id == $0", user.id),
                "energy_subscription",
            )
        }
        .build()
}
