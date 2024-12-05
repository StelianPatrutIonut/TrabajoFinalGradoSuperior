package com.example.triptrendyapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.triptrendyapp.databinding.ActivityPantallaPrincipalBinding
import com.example.triptrendyapp.temperatura.WeatherService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class MainActivityScreen : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityPantallaPrincipalBinding

    private lateinit var weatherService: WeatherService

    private val handler = Handler(Looper.getMainLooper())

    private val delayMillis = 3600000

    private lateinit var fotoPerfil: ImageView

    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val nombreUsuario = intent.getStringExtra("usuario")

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView).text = nombreUsuario

        val nombreImagen = intent.getStringExtra("nombreImagen")

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView2).text = nombreImagen

        fotoPerfil = binding.navView.getHeaderView(0).findViewById(R.id.imageView)

        var nombreConExtension = nombreImagen ?: ""

        if (!nombreConExtension.toLowerCase().endsWith(".jpg")) {

            nombreConExtension += ".jpg"

        }

        val carpetaImagenes = "img_perfil"

        val storageReference = FirebaseStorage.getInstance().reference.child(carpetaImagenes).child(nombreConExtension)

        storageReference.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                fotoPerfil.setImageBitmap(bitmap)

            }

        val retrofit = Retrofit.Builder().baseUrl("https://api.openweathermap.org/").addConverterFactory(GsonConverterFactory.create()).build()

        weatherService = retrofit.create(WeatherService::class.java)

        loadWeather("Madrid")

        handler.postDelayed(object : Runnable {

            override fun run() {

                loadWeather("Madrid")

                handler.postDelayed(this, delayMillis.toLong())

            }

        }, delayMillis.toLong())

        val drawerLayout = binding.drawerLayout

        val navView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_map, R.id.nav_route, R.id.nav_ramdom, R.id.nav_favorite, R.id.nav_pulication, R.id.nav_myLocation, R.id.nav_weather, R.id.nav_profile, R.id.nav_signOff ), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        navController.graph.forEach { destination ->

            destination.addArgument("usuario", NavArgument.Builder().setDefaultValue(nombreUsuario).build())

        }

    }

    private fun ByteArray.decodeBitmap(): Bitmap {

        return BitmapFactory.decodeByteArray(this, 0, size)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.pantalla_principal, menu)

        return true

    }

    override fun onSupportNavigateUp(): Boolean {

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }

    private fun loadWeather(city: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val response = weatherService.getCurrentWeather(city, "77f77346d62a63cd3dd92fca48570090")

            withContext(Dispatchers.Main) {

                if (response.isSuccessful) {

                    val weatherData = response.body()

                    binding.appBarMain.textoFlotanteTemp.text = "  ${weatherData?.main?.temp?.roundToInt()}°C"

                    val weatherIcon = weatherData?.weather?.firstOrNull()?.icon ?: ""

                    if (weatherIcon == "01d") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.uno_d)

                    } else if (weatherIcon == "01n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.uno_n)

                    } else if (weatherIcon == "02d") {
                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.dos_d)

                    } else if (weatherIcon == "02n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.dos_n)

                    } else if (weatherIcon == "03d" || weatherIcon == "03n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.tres)

                    } else if (weatherIcon == "04d" || weatherIcon == "04n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.cuatro)

                    } else if (weatherIcon == "09d" || weatherIcon == "09n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.nueve)

                    } else if (weatherIcon == "10d") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.diez_d)

                    } else if (weatherIcon == "10n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.diez_n)

                    } else if (weatherIcon == "11d" || weatherIcon == "11n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.once)

                    } else if (weatherIcon == "13d" || weatherIcon == "13n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.trece)

                    } else if (weatherIcon == "50d" || weatherIcon == "50n") {

                        binding.appBarMain.imagenFlonatenTemp.setImageResource(R.drawable.cincuenta)

                    }

                }

            }

        }

    }

}
