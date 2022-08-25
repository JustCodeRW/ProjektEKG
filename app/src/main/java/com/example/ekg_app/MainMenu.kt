package com.example.ekg_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val userProfileMenu: CardView = findViewById(R.id.userProfile)
        val startScan: CardView = findViewById(R.id.startScan)
        val logout: CardView = findViewById(R.id.logout)

        val intent: Intent = intent
        val userFullName = intent.getStringExtra("name")
        val userName = intent.getStringExtra("userName")
        val userEmail = intent.getStringExtra("email")
        val userPhoneNo = intent.getStringExtra("phoneNo")
        val userPassword = intent.getStringExtra("password")

        userProfileMenu.setOnClickListener {
            val intentUserProfile = Intent(this@MainMenu, UserProfile::class.java)
            intentUserProfile.putExtra("name", userFullName)
            intentUserProfile.putExtra("userName", userName)
            intentUserProfile.putExtra("email", userEmail)
            intentUserProfile.putExtra("phoneNo", userPhoneNo)
            intentUserProfile.putExtra("password", userPassword)
            startActivity(intentUserProfile)
        }

        startScan.setOnClickListener {
            startActivity(Intent(this@MainMenu, BleAndLocationConnection::class.java))
        }

        logout.setOnClickListener {
            startActivity(Intent(this@MainMenu, Login::class.java))
        }
    }

    override fun onBackPressed() {}
}