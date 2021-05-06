package com.example.ekg_app

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.util.Pair as UtilPair

class MainActivity : AppCompatActivity() {

    var topAnimation : Animation? = null
    var bottomAnimation : Animation? = null
    private val  SPLASH_SCREEN = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //Animation
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        //Hooks
        val image : ImageView  = findViewById(R.id.imageView)
        val logo : TextView = findViewById(R.id.appName)

        image.startAnimation(topAnimation)
        logo.startAnimation(bottomAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, Login::class.java)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, UtilPair.create(image, "logo_image_transition"), UtilPair.create(logo, "logo_text_transition"))
                startActivity(intent, options.toBundle())
            }

        }, SPLASH_SCREEN.toLong())

    }
}