package com.example.ekg_app

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Pair as UtilPair

//the MainActivity starts the application and shows a animation
private const val SPLASH_SCREEN = 3000

class MainActivity : AppCompatActivity() {
    private var topAnimation: Animation? = null
    private var bottomAnimation: Animation? = null
    private val image: ImageView = findViewById(R.id.imageView)
    private val logo: TextView = findViewById(R.id.appName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //here the animation for the MainActivity is created
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        image.startAnimation(topAnimation)
        logo.startAnimation(bottomAnimation)

        //in the handler the MainActivity points to the next screen (loginScreen) and starts the animation
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, Login::class.java)

            val options = ActivityOptions.makeSceneTransitionAnimation(
                this@MainActivity,
                UtilPair.create(image, "logo_image_transition"),
                UtilPair.create(logo, "logo_text_transition")
            )
            startActivity(intent, options.toBundle())

        }, SPLASH_SCREEN.toLong())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }
}