package com.emill.bleexercise

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val viewModel: MyViewModel by viewModels()

    // Handling permission result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                startBluetoothScan()
            } else {
                Toast.makeText(this, "Permissions are required to scan Bluetooth devices", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLEScannerApp(viewModel)
        }
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        when {
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                // Requesting permissions
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))
            }
            else -> {
                startBluetoothScan()
            }
        }
    }

    private fun startBluetoothScan() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
        viewModel.scanDevices(bluetoothLeScanner)
    }

}

@Composable
fun BLEScannerApp(viewModel: MyViewModel) {
    val scanResults by viewModel.scanResults.observeAsState()
    val isScanning by viewModel.isScanning.observeAsState(false)

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isScanning == true) {
                Text("Scanning...")
            } else {
                scanResults?.let { results ->
                    DeviceList(devices = results)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceList(devices: List<ScanResult>?) {
    devices?.let {
        LazyColumn {
            items(it) { device ->
                val name = device.device.name ?: "Unknown Device"
                val address = device.device.address
                val rssi = device.rssi
                DeviceItem(name = name, address = address, rssi = rssi)
            }
        }
    }
}


@Composable
fun DeviceItem(name: String, address: String, rssi: Int) {
    Text(
        text = "$name ($address) RSSI: $rssi",
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray)
    )
}
