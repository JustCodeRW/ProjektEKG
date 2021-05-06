package com.example.ekg_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout

class Registration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Hooks
        val registeredName : TextInputLayout = findViewById(R.id.name)
        val registeredUsername : TextInputLayout = findViewById(R.id.username)
        val registeredEmail : TextInputLayout = findViewById(R.id.email)
        val registeredPhoneNo : TextInputLayout = findViewById(R.id.phoneNo)
        val registeredPassword : TextInputLayout = findViewById(R.id.password)
        val registerBtn : Button = findViewById(R.id.startBtn)
        val login_registerBtn : Button = findViewById(R.id.login_registrationBtn)


        //save data in firebase (database) on button click

        registerBtn.setOnClickListener {
            
        }
    }
}