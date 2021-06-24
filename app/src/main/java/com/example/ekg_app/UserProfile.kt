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
        val user_Name = intent.getStringExtra("name")
        val user_Username = intent.getStringExtra("username")
        val user_Email = intent.getStringExtra("email")
        val user_PhoneNo = intent.getStringExtra("phoneNo")
        val user_Password = intent.getStringExtra("password")

        fullName.editText?.setText(user_Name)
        email.editText?.setText(user_Email)
        phoneNo.editText?.setText(user_PhoneNo)
        password.editText?.setText(user_Password)
        fullNameLabel.text = user_Name
        usernameLabel.text = user_Username

    }


}