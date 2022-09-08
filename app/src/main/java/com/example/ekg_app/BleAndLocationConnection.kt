package com.example.ekg_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.ekg_app.ble.BleConnectionManager
import com.example.ekg_app.ble.ConnectionEventListener
import com.example.ekg_app.ble.ScanResultsAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

//this Activity scans for the BLE Devices (ESP32) and connects to them
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

@SuppressLint("MissingPermission")
class BleAndLocationConnection : AppCompatActivity() {
    private lateinit var scanButton: Button
    private lateinit var locationManager: LocationManager

    private val scanResults = mutableListOf<ScanResult>()
    private var gpsStatus = false

    private val scanResultsAdapter: ScanResultsAdapter by lazy {
        ScanResultsAdapter(scanResults) { result ->
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to %address")
                BleConnectionManager.connect(this, this@BleAndLocationConnection)
            }
        }
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scanButton.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private lateinit var recyclerview: RecyclerView

    private val isLocationPermissionGranted get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_and_location_connection)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        scanButton = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }

        recyclerview = findViewById(R.id.scan_results_recycler_view)
        setUpRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        BleConnectionManager.registerListener(connectionEventListener)
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        }

        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        } else if (!gpsStatus) {
            enableLocation()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@BleAndLocationConnection, MainMenu::class.java))
    }

    //method for checking if location permission is granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                Log.d("Permission: ", grantResults[0].toString())
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    private var requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("BLUETOOTH TEST", "${it.key} = ${it.value}")
            }
        }

    //these methods asking for Bluetooth permission
    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                val data: Intent? = result.data
                promptEnableBluetooth()
            } else {
                //not granted
            }
        }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    //this method ask for the Location
    private fun enableLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2

        val locationSettingsBuilder = LocationSettingsRequest.Builder()
        locationSettingsBuilder.addLocationRequest(locationRequest)
        locationSettingsBuilder.setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(this)

        val task = settingsClient.checkLocationSettings(locationSettingsBuilder.build())
        task.addOnSuccessListener(this) {
            Log.i("GPS STATUS", "GPS IS ON")
        }

        task.addOnFailureListener(this) { e ->
            Log.i("GPS STATUS", "GPS IS OFF")
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@BleAndLocationConnection, 0x1)
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    sendIntentException.printStackTrace()
                }
            }
        }
    }

    //view for (scanned) bluetooth devices
    private fun setUpRecyclerView() {
        recyclerview.apply {
            adapter = scanResultsAdapter
            layoutManager = LinearLayoutManager(
                this@BleAndLocationConnection,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = recyclerview.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    //this method starts a BleScan
    private fun startBleScan() {
        Log.d("Button pressed", "Start Location ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultsAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    //the scanCallback object saves the scan state and shows if devices are founded
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) {
                scanResults[indexQuery] = result
                scanResultsAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE Device Name:  ${name ?: "Unnamed"}, address: $address"
                    )
                }
                scanResults.add(result)
                scanResultsAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallBack Failed", "onScanFailed: code $errorCode")
        }
    }

    //the listener points to the record Screen if the connection was successful
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                Intent(this@BleAndLocationConnection, RecordScreen::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it)
                }
                BleConnectionManager.unregisterListener(this)
            }
            onDisconnect = {
                onDisconnect = {
                    val builder = AlertDialog.Builder(this@BleAndLocationConnection)
                    builder.setMessage("disconnected or can't connect to device")
                        .setPositiveButton("okay") { _,_ -> }
                    val alertDialog = builder.create()
                    alertDialog.show()
                }
            }
        }
    }

    //the next methods checking if the needed permissions are there
    private fun requestLocationPermission() {
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permissionType
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}
