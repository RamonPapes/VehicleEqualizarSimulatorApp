package com.ramonpapes.vehicleequalizerapp

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class VehicleCanBusSimulator {
    private val TAG = "VehicleCanBusSimulator"
    private val messageChannel = Channel<CanMessage>()
    private val scope = CoroutineScope(Dispatchers.Default)

    // Flow para notificar a UI sobre mensagens CAN recebidas
    private val _canMessageFlow = MutableSharedFlow<CanMessage>()
    val canMessageFlow = _canMessageFlow.asSharedFlow()

    init {
        scope.launch {
            for (message in messageChannel) {
                Log.d(TAG, "Mensagem CAN recebida (ID: 0x%X, Dados: %s)"
                    .format(message.id, message.data.joinToString { "%02X".format(it) }))

                _canMessageFlow.emit(message)

                when (message.id) {
                    0x123 -> {
                        if (message.data.isNotEmpty()) {
                            val volume = message.data[0].toInt() and 0xFF
                            Log.i(TAG, "Processando mensagem CAN: Novo Volume Mestre: $volume")
                        }
                    }
                }
            }
        }
    }

    fun sendMessage(message: CanMessage) {
        scope.launch {
            Log.i(TAG, "Enviando mensagem CAN (ID: 0x%X, Dados: %s)"
                .format(message.id, message.data.joinToString { "%02X".format(it) }))
            messageChannel.send(message)
        }
    }

    fun stopSimulator() {
        scope.cancel()
        messageChannel.close()
        Log.d(TAG, "Simulador CAN parado.")
    }

}