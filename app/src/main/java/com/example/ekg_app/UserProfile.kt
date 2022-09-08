package com.example.ekg_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout

/*the userProfile Activity shows a
* screen with a overview of the created user
* and its input
*/
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

        val menu: MaterialCardView = findViewById(R.id.menu)
        val startScan: MaterialCardView = findViewById(R.id.startScan)

        //listener that points to the mainMenu Screen
        menu.setOnClickListener {
            startActivity(Intent(this@UserProfile, MainMenu::class.java))
        }

        //listener that points to the BleAndLocationConnection Screen
        startScan.setOnClickListener {
            startActivity(Intent(this@UserProfile, BleAndLocationConnection::class.java))
        }

        showAllUserData()
    }

    override fun onResume() {
        super.onResume()
        showAllUserData()
    }

    //method to get the data from the user and show it
    private fun showAllUserData() {
        val intent: Intent = intent
        val userFullName = intent.getStringExtra("name")
        val userName = intent.getStringExtra("userName")
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