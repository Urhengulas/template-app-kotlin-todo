package com.emission_meter.demo

import android.content.Context
import io.realm.kotlin.Realm
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject

class EmissionMeter suspend fun constructor(context: Context?) {
    val wattOfApp = WattOfApp(context)

    // input
    val token = "uCiwQLqFfiJyROrlsmleMR923y8dvDf5ZmiIrc19stk7gew0B7fAVYVux2dc5j1D"

    // connect to mongo
    val user = app.login(Credentials.apiKey(token))
    val config = SyncConfiguration.create(user, setOf(EnergyMeasurement::class))
    val realm = Realm.open(config)

    suspend fun insertData() {
        val a = EnergyMeasurement().apply {
//            time = wattOfApp.timeReal
            energy = wattOfApp.get()
        };
        realm.write {
            copyToRealm(a)
        }
    }
}

class EnergyMeasurement() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var time = 0
    var energy: Long = 0
}