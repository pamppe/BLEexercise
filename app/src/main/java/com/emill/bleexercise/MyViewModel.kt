package com.emill.bleexercise

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
    private val mResults = mutableMapOf<String, ScanResult>()
    val scanResults = MutableLiveData<List<ScanResult>>()
    val isScanning = MutableLiveData<Boolean>(false)

    @SuppressLint("MissingPermission")
    fun scanDevices(bluetoothLeScanner: BluetoothLeScanner?) {
        viewModelScope.launch(Dispatchers.IO) {
            isScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            bluetoothLeScanner?.startScan(null, settings, leScanCallback)
            delay(3500) // Scan for a specific duration
            bluetoothLeScanner?.stopScan(leScanCallback)
            isScanning.postValue(false)
            scanResults.postValue(mResults.values.toList())
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            mResults[device.address] = result
        }
    }
}

