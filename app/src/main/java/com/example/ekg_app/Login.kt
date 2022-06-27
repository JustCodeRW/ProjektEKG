package com.example.ekg_app

import android.Manifest.permission
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import kotlin.system.exitProcess
import android.util.Pair as UtilPair

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class Login : AppCompatActivity() {
    private lateinit var username: TextInputLayout
    private lateinit var password: TextInputLayout

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Hooks
        val image: ImageView = findViewById(R.id.logo_image)
        val logoText: TextView = findViewById(R.id.logo_text)
        val infoText: TextView = findViewById(R.id.infoText)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        val loginBtn: Button = findViewById(R.id.loginBtn)
        val buttonLoadRegistration: Button = findViewById(R.id.registrationBtn)
        val buttonLocation: Button = findViewById(R.id.resetPasswordBtn)

        buttonLocation.setOnClickListener { startBleScan() }

        buttonLoadRegistration.setOnClickListener {
            val intent = Intent(this@Login, Registration::class.java)
            val pair1: android.util.Pair<View, String> =
                UtilPair.create(image, "logo_image_transition")
            val pair2: android.util.Pair<View, String> =
                UtilPair.create(logoText, "logo_text_transition")
            val pair3: android.util.Pair<View, String> =
                UtilPair.create(infoText, "logo_desc_transition")
            val pair4: android.util.Pair<View, String> =
                UtilPair.create(username, "username_transition")
            val pair5: android.util.Pair<View, String> =
                UtilPair.create(password, "password_transition")
            val pair6: android.util.Pair<View, String> =
                UtilPair.create(loginBtn, "startBtn_transition")
            val pair7: android.util.Pair<View, String> =
                UtilPair.create(buttonLoadRegistration, "signIn_transition")


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                val controller = window.insetsController
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                } else {
                    // All below using to hide navigation bar
                    val currentApiVersion = Build.VERSION.SDK_INT
                    val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

                    // This work only for android 4.4+
                    if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
                        window.decorView.systemUiVisibility = flags
                        val decorView = window.decorView
                        decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
                            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                                decorView.systemUiVisibility = flags
                            }
                        }
                    }
                }

                val options = ActivityOptions.makeSceneTransitionAnimation(
                    this@Login,
                    pair1,
                    pair2,
                    pair3,
                    pair4,
                    pair5,
                    pair6,
                    pair7
                )
                startActivity(intent, options.toBundle())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    permission.BLUETOOTH_CONNECT,
                    permission.BLUETOOTH_SCAN
                )
            )
        } else {
            onResume()
        }
    }

    //Bluetooth
    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                val data: Intent? = result.data
                promptEnableBluetooth()
            } else {
                //denied
            }
        }

    private var requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    //Location
    val isLocationPermissionGranted get() = hasPermission(permission.ACCESS_FINE_LOCATION)

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permissionType
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startBleScan() {
        Log.d("Button pressed", "Start Location ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {

        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Location permission required")
            .setMessage("Starting from Android, the System requires apps to be granted " + "location access in order to scan for BLE devices.")
            .setCancelable(false)
            .setPositiveButton(
                "Okay", DialogInterface.OnClickListener { _, _ ->
                    requestPermission(
                        permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            )
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    private fun validateUserName(): Boolean {
        val value: String = username.editText?.text.toString()

        if (value.isEmpty()) {
            username.error = "Field cannot be empty"
            return false
        } else {
            username.error = null
            username.isEnabled = false
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val value: String = password.editText?.text.toString()

        if (value.isEmpty()) {
            password.error = "Field cannot be empty"
            return false
        } else {
            password.error = null
            password.isErrorEnabled = false
            return true
        }
    }

    fun loginUser(view: View) {
        if (!validateUserName() or !validatePassword()) {
            return
        } else {
            isUser()
        }
    }

    private fun isUser() {
        val userEnteredUsername: String = username.editText?.text.toString().trim()
        val userEnteredPassword: String = password.editText?.text.toString().trim()

        val reference: DatabaseReference =
            FirebaseDatabase.getInstance("https://ekg-app-f4d09-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
        val checkUserReference: Query =
            reference.orderByChild("userName").equalTo(userEnteredUsername)

        checkUserReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    username.error = null
                    username.isErrorEnabled = false

                    val passwordFromDB: String =
                        snapshot.child(userEnteredUsername).child("password")
                            .getValue(String::class.java).toString()

                    if (passwordFromDB == userEnteredPassword) {
                        username.error = null
                        username.isErrorEnabled = false

                        val nameFromDB: String? = snapshot.child(userEnteredUsername).child("name")
                            .getValue(String::class.java)
                        val usernameFromDB: String? =
                            snapshot.child(userEnteredUsername).child("username")
                                .getValue(String::class.java)
                        val phoneNoFromDB: String? =
                            snapshot.child(userEnteredUsername).child("phoneNo")
                                .getValue(String::class.java)
                        val emailFromDB: String? =
                            snapshot.child(userEnteredUsername).child("email")
                                .getValue(String::class.java)

                        val intent = Intent(applicationContext, UserProfile::class.java)

                        intent.putExtra("name", nameFromDB)
                        intent.putExtra("username", usernameFromDB)
                        intent.putExtra("email", emailFromDB)
                        intent.putExtra("phoneNo", phoneNoFromDB)
                        intent.putExtra("password", passwordFromDB)

                        startActivity(intent)
                    } else {
                        password.error = "Wrong Password"
                        password.editText?.text?.clear()
                        password.requestFocus()
                    }
                } else {
                    username.error = "Not such User exists"
                    username.editText?.text?.clear()
                    password.editText?.text?.clear()
                    username.isEnabled = true
                    username.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print(error.message)
            }
        })

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure to Exit? ")
            .setCancelable(true)
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })
            .setPositiveButton("Exit ", DialogInterface.OnClickListener { _, _ ->
                finishAffinity()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
}
