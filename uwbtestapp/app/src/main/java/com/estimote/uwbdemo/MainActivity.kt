package com.estimote.uwbdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.estimote.uwb.api.EstimoteUWBFactory
import com.estimote.uwb.api.scanning.EstimoteUWBScanResult
import com.estimote.uwb.api.ranging.EstimoteUWBRangingResult

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {
    private val uwbManager = EstimoteUWBFactory.create()
    private var job: Job? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uwbManager.init(this)

        uwbManager.uwbDevices.onEach { scanResult ->
            when (scanResult) {
                is EstimoteUWBScanResult.Devices -> {
                    Log.i("UWB", "Found ${scanResult.devices.size} UWB Beacons")

                    if (!isConnected) {
                        job = lifecycleScope.launch {
                            scanResult.devices.first().device?.let { beacon ->
                                uwbManager.connectSuspend(beacon, this@MainActivity)
                                isConnected = true
                            }
                        }
                    }
                }
                is EstimoteUWBScanResult.Error -> {
                    Log.e("UWB", "Error: ${scanResult.errorCode}")
                }
                EstimoteUWBScanResult.ScanNotStarted -> {
                    Log.i("UWB", "Error: scan not started")
                }
            }

        }.launchIn(lifecycleScope)

        uwbManager.rangingResult.onEach { rangingResult ->
            when (rangingResult) {
                is EstimoteUWBRangingResult.Position -> {
                    Log.i("UWB", rangingResult.position.distance?.value.toString())
                }
                is EstimoteUWBRangingResult.Error -> {
                    Log.i("UWB", "Error: ${rangingResult.message}")
                }
                else -> Unit
            }
        }.launchIn(lifecycleScope)

        uwbManager.startDeviceScanning(this)

    }
}