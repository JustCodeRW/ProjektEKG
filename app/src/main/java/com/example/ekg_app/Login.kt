package com.example.ekg_app

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import android.util.Pair as UtilPair

//the loginActivity is created for the user to login into the application
class Login : AppCompatActivity() {
    private lateinit var username: TextInputLayout
    private lateinit var password: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //here the animation for the login is created
        val image: ImageView = findViewById(R.id.logo_image)
        val logoText: TextView = findViewById(R.id.logo_text)
        val infoText: TextView = findViewById(R.id.infoText)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        val loginBtn: Button = findViewById(R.id.loginBtn)
        val buttonLoadRegistration: Button = findViewById(R.id.registrationBtn)

        loginBtn.setOnClickListener { loginUser() }

        //at this point a listener is creating that points to the registration Screen and starts an animation
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
    }

    //the onBackPressed method defines what happen if the button is pressed
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure to Exit? ")
            .setCancelable(true)
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Exit ") { _, _ ->
                finishAffinity()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    //methods to check users input
    private fun validateUserName(): Boolean {
        val value: String = username.editText?.text.toString()

        return if (value.isEmpty()) {
            username.error = "Field cannot be empty"
            false
        } else {
            username.error = null
            username.isEnabled = false
            true
        }
    }

    private fun validatePassword(): Boolean {
        val value: String = password.editText?.text.toString()

        return if (value.isEmpty()) {
            password.error = "Field cannot be empty"
            false
        } else {
            password.error = null
            password.isErrorEnabled = false
            true
        }
    }

    //method to login the User
    private fun loginUser() {
        if (!validateUserName() or !validatePassword()) {
            return
        } else {
            isUser()
        }
    }

    /*method to check the data from the users input with the
    *data in the database
    */
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
                            snapshot.child(userEnteredUsername).child("userName")
                                .getValue(String::class.java)
                        val phoneNoFromDB: String? =
                            snapshot.child(userEnteredUsername).child("phoneNo")
                                .getValue(String::class.java)
                        val emailFromDB: String? =
                            snapshot.child(userEnteredUsername).child("email")
                                .getValue(String::class.java)

                        val intent = Intent(applicationContext, MainMenu::class.java)

                        intent.putExtra("name", nameFromDB)
                        intent.putExtra("userName", usernameFromDB)
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
}
