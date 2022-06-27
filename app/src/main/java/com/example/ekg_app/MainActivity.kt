package com.example.ekg_app

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, UtilPair.create(image, "logo_image_transition"), UtilPair.create(logo, "logo_text_transition"))
                startActivity(intent, options.toBundle())
            }

        }, SPLASH_SCREEN.toLong())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                // All below using to hide navigation bar
                val currentApiVersion = Build.VERSION.SDK_INT
                val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

                // This work only for android 4.4+
                if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
                    window.decorView.systemUiVisibility = flags
                    val decorView = window.decorView
                    decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            decorView.systemUiVisibility = flags
                        }
                    }
                }
            }
        }
    }
}