package com.example.triptrendyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        /**
         * Hilo para el SplashScreen personalizado.
         */

        Thread {

            Thread.sleep(2000)

            startActivity(MainActivity.newIntent(this))

            finish()

        }.start()

    }
}