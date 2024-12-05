package com.example.triptrendyapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.triptrendyapp.databinding.ActivityMainBinding
import android.Manifest

class MainActivity : AppCompatActivity() {

    /**
     * Inicialización tardía del Databinding.
     */

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /**
         * Inicialización del Databinding.
         */

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        /**
         * Inicialización de componentes.
         */

        val btnIniciarSesion: Button = binding.btnIniciarSesion

        val btnNuevaCuenta: Button = binding.btnCrearCuenta


        verificarPermisos()

        /**
         * Botón para viajar a la activity de inicio de sesión.
         * @see MainActivitySignIn
         */

        btnIniciarSesion.setOnClickListener{

            val intent = Intent(this, MainActivitySignIn::class.java)

            startActivity(intent)

        }

        /**
         * Botón para viajar a la activity de creación de cuenta.
         * @see MainActivitySignUp
         */

        btnNuevaCuenta.setOnClickListener {

            startActivity(MainActivitySignUp.newIntent(this))

        }


    }

    /**
     * Este objeto se utiliza para navegar de una activity a esta activity.
     */

    companion object {

        fun newIntent(context: Context): Intent {

            return Intent(context, MainActivity::class.java)

        }

    }

    private fun verificarPermisos() {

        val permisos = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            permisos.add(Manifest.permission.CAMERA)

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {

            permisos.add(Manifest.permission.READ_PHONE_STATE)

        }

        if (permisos.isNotEmpty()) {

            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 123)

        }

    }

}