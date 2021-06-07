package com.example.ekg_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Registration : AppCompatActivity() {
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Hooks
        val registeredName: TextInputLayout = findViewById(R.id.name)
        val registeredUsername: TextInputLayout = findViewById(R.id.username)
        val registeredEmail: TextInputLayout = findViewById(R.id.email)
        val registeredPhoneNo: TextInputLayout = findViewById(R.id.phoneNo)
        val registeredPassword: TextInputLayout = findViewById(R.id.password)
        val registerBtn: Button = findViewById(R.id.startBtn)
        val login_registerBtn: Button = findViewById(R.id.login_registrationBtn)


        //save data in firebase (database) on button click

        registerBtn.setOnClickListener {
            rootNode = FirebaseDatabase.getInstance("https://ekg-app-f4d09-default-rtdb.europe-west1.firebasedatabase.app/")
            reference = rootNode.getReference("users")

            val name = registeredName.editText?.text.toString()
            val userName = registeredUsername.editText?.text.toString()
            val email = registeredEmail.editText?.text.toString()
            val phoneNo = registeredPhoneNo.editText?.text.toString()
            val password = registeredPassword.editText?.text.toString()

            val dataModel = UserDataModel(name, userName, email, phoneNo, password)

            reference.child(phoneNo).setValue(dataModel)

        }
    }
}