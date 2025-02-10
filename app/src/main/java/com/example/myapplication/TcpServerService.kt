package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class TcpServerService : Service() {
    private val port = 2006
    private var isRunning = true
    private val executorService = Executors.newCachedThreadPool()
    companion object {
        private val _sensorLiveData = MutableLiveData<FloatArray>()
        val sensorLiveData: LiveData<FloatArray> get() = _sensorLiveData
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        executorService.execute { startServer() }
        return START_STICKY
    }

    private fun startServer() {
        try {
            val serverSocket = ServerSocket(port)
            Log.d("TCP Server", "Server started on port $port")
            while (isRunning) {
                val clientSocket: Socket = serverSocket.accept()
                executorService.execute { handleClient(clientSocket) }
            }
            serverSocket.close()
        } catch (e: Exception) {
            Log.e("TCP Server", "Error: ${e.message}", e)
        }
    }

    private fun handleClient(clientSocket: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val output = PrintWriter(OutputStreamWriter(clientSocket.getOutputStream()), true)
            val message = input.readLine()
            Log.d("TCP Server", "Received: $message")

            val parsedData = SensorsData.processData(message)
            Log.d("TCP Server", "Parsed Data: ${parsedData.joinToString()}")

            _sensorLiveData.postValue(parsedData)

            output.println("Echo: $message")
            clientSocket.close()
        } catch (e: Exception) {
            Log.e("TCP Server", "Client error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        executorService.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

class SensorsData {
    companion object {
        fun processData(data: String): FloatArray {
            return try {
                if (!data.startsWith("MEAS_DATA")) throw IllegalArgumentException("Invalid data format")

                val values = data.substringAfter("MEAS_DATA").split(Regex("[,: ]+"))
                    .drop(1) // Drop "MEAS_DATA"
                    .filter { it.toFloatOrNull() != null }
                    .map { it.toFloat() }
                    .toFloatArray()
                values
            } catch (e: Exception) {
                Log.e("SensorsData", "Data processing error: ${e.message}", e)
                floatArrayOf()
            }
        }
    }
}
