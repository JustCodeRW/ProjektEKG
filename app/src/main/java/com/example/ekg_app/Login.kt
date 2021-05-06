package com.example.ekg_app

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import android.util.Pair as UtilPair

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)

        //Hooks
        val image : ImageView = findViewById(R.id.logo_image)
        val logoText : TextView = findViewById(R.id.logo_text)
        val infoText : TextView = findViewById(R.id.infoText)
        val username : TextInputLayout = findViewById(R.id.username)
        val password : TextInputLayout = findViewById(R.id.password)
        val loginBtn : Button = findViewById(R.id.login_registrationBtn)
        val buttonLoadRegistration : Button = findViewById(R.id.login_registrationBtn)

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


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
}