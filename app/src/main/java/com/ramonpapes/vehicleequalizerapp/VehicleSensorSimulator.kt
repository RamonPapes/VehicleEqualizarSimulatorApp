package com.ramonpapes.vehicleequalizerapp

import android.util.Log
import java.util.Random

class VehicleSensorSimulator(private val sensorName: String) {
    private val TAG = "VehicleSensorSimulator"
    private val random = Random()

    fun readSensorData(): Int {
        val data = when (sensorName) {
            "Velocidade" -> random.nextInt(201) // 0-200 km/h
            "Temperatura Externa" -> random.nextInt(50) - 10 // -10 a 39 ºC
            "Nível Combustível" -> random.nextInt(101) // 0-100%
            else -> random.nextInt(101)
        }
        Log.d(TAG, "[$sensorName] Lendo dados do sensor: $data")
        return data
    }

    fun calibrateSensor() {
        Log.i(TAG, "[$sensorName] Calibrando sensor...")
        Thread.sleep(1000)
        Log.i(TAG, "[$sensorName] Sensor calibrado.")
    }
}
