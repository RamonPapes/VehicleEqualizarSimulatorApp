package com.ramonpapes.vehicleequalizerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button
import android.widget.TextView
import android.util.Log
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
        bassSeekBar = findViewById(R.id.bassSeekBar)
        midSeekBar = findViewById(R.id.midSeekBar)
        trebleSeekBar = findViewById(R.id.trebleSeekBar)
        speedLabel = findViewById(R.id.speedLabel)
        readSpeedButton = findViewById(R.id.readSpeedButton)
        canVolumeLabel = findViewById(R.id.canVolumeLabel)
        sendCanVolumeButton = findViewById(R.id.sendCanVolumeButton)

        readSpeedButton.setOnClickListener {
            val currentSpeed = speedSensorSimulator.readSensorData()
            speedLabel.text = "Velocidade Atual: $currentSpeed km/h"
            Log.d(TAG, "Velocidade lida: $currentSpeed km/h")
        }

        sendCanVolumeButton.setOnClickListener {
            val randomVolume = (0..100).random()
            val message = CanMessage(id = 0x123, data = byteArrayOf(randomVolume.toByte()))
            vehicleCanBusSimulator.sendMessage(message)
        }

        activityScope.launch {
            vehicleCanBusSimulator.canMessageFlow.collect { message ->
                if (message.id == 0x123 && message.data.isNotEmpty()) {
                    val volume = message.data[0].toInt() and 0xFF
                    canVolumeLabel.text = "Volume CAN: $volume"
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        vehicleCanBusSimulator.stopSimulator()
        activityScope.cancel()
    }

}