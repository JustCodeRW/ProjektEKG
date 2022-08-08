package com.example.ekg_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout

class UserProfile : AppCompatActivity() {
    private lateinit var fullName: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var phoneNo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var fullNameLabel: TextView
    private lateinit var usernameLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        fullName = findViewById(R.id.full_name_profile)
        email = findViewById(R.id.email_profile)
        phoneNo = findViewById(R.id.phoneNo_profile)
        password = findViewById(R.id.password_profile)
        fullNameLabel = findViewById(R.id.full_name_label)
        usernameLabel = findViewById(R.id.username_label)


        showAllUserData()
    }

    private fun showAllUserData() {
        val intent: Intent = intent
        val userFullName = intent.getStringExtra("name")
        val userName = intent.getStringExtra("username")
        val userEmail = intent.getStringExtra("email")
        val userPhoneNo = intent.getStringExtra("phoneNo")
        val userPassword = intent.getStringExtra("password")

        fullName.editText?.setText(userFullName)
        email.editText?.setText(userEmail)
        phoneNo.editText?.setText(userPhoneNo)
        password.editText?.setText(userPassword)
        fullNameLabel.text = userFullName
        usernameLabel.text = userName

    }
}