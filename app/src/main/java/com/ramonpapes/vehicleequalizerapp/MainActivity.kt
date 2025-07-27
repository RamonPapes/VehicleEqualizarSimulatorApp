package com.ramonpapes.vehicleequalizerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Button
import android.widget.TextView
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bassSeekBar: SeekBar
    private lateinit var midSeekBar: SeekBar
    private lateinit var trebleSeekBar: SeekBar

    private lateinit var speedLabel: TextView
    private lateinit var readSpeedButton: Button

    private lateinit var canVolumeLabel: TextView
    private lateinit var sendCanVolumeButton: Button

    private lateinit var temperatureLabel: TextView
    private lateinit var readTemperatureButton: Button

    private lateinit var fuelLabel: TextView
    private lateinit var readFuelButton: Button

    private val speedSensorSimulator = VehicleSensorSimulator("Velocidade")
    private val temperatureSensorSimulator = VehicleSensorSimulator("Temperatura Externa")
    private val vehicleCanBusSimulator = VehicleCanBusSimulator()
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Equalizador
        bassSeekBar = findViewById(R.id.bassSeekBar)
        midSeekBar = findViewById(R.id.midSeekBar)
        trebleSeekBar = findViewById(R.id.trebleSeekBar)

        // Velocidade
        speedLabel = findViewById(R.id.speedLabel)
        readSpeedButton = findViewById(R.id.readSpeedButton)

        // Volume CAN
        canVolumeLabel = findViewById(R.id.canVolumeLabel)
        sendCanVolumeButton = findViewById(R.id.sendCanVolumeButton)

        // Temperatura
        temperatureLabel = findViewById(R.id.speedLabel2)
        readTemperatureButton = findViewById(R.id.readSpeedButton2)

        // Combustível
        fuelLabel = findViewById(R.id.speedLabel3)
        readFuelButton = findViewById(R.id.sendCanVolumeButton2)

        // Lê a velocidade
        readSpeedButton.setOnClickListener {
            val currentSpeed = speedSensorSimulator.readSensorData()
            speedLabel.text = "Velocidade Atual: $currentSpeed km/h"
            Log.d(TAG, "Velocidade lida: $currentSpeed km/h")
        }

        // Simula o volume CAN
        sendCanVolumeButton.setOnClickListener {
            val randomVolume = (0..100).random()
            val message = CanMessage(id = 0x123, data = byteArrayOf(randomVolume.toByte()))
            vehicleCanBusSimulator.sendMessage(message)
        }

        // Recebe volume CAN
        activityScope.launch {
            vehicleCanBusSimulator.canMessageFlow.collect { message ->
                if (message.id == 0x123 && message.data.isNotEmpty()) {
                    val volume = message.data[0].toInt() and 0xFF
                    canVolumeLabel.text = "Volume CAN: $volume"
                }
            }
        }

        // Lê temperatura
        readTemperatureButton.setOnClickListener {
            val currentTemp = temperatureSensorSimulator.readSensorData()
            temperatureLabel.text = "Temperatura: $currentTemp ºC"
            Log.d(TAG, "Temperatura lida: $currentTemp ºC")
        }

        // Simula nível de combustível
        readFuelButton.setOnClickListener {
            val fuelLevel = (0..100).random()
            fuelLabel.text = "Gasolina: $fuelLevel%"
            Log.d(TAG, "Nível de combustível lido: $fuelLevel%")
        }
    }

    override fun onStop() {
        super.onStop()
        vehicleCanBusSimulator.stopSimulator()
        activityScope.cancel()
    }
}
