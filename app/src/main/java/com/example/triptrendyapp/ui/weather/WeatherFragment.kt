package com.example.triptrendyapp.ui.weather

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.example.triptrendyapp.R
import com.example.triptrendyapp.databinding.ActivityWeatherFragmentBinding
import com.example.triptrendyapp.temperatura.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import kotlin.math.roundToInt

class WeatherFragment : Fragment() {

    private lateinit var binding: ActivityWeatherFragmentBinding

    private lateinit var weatherService: WeatherService

    private val delayMillis = 3600000

    private val handler = Handler(Looper.getMainLooper())


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = ActivityWeatherFragmentBinding.inflate(inflater, container, false)

        val root = binding.root

        val retrofit = Retrofit.Builder().baseUrl("https://api.openweathermap.org/").addConverterFactory(GsonConverterFactory.create()).build()

        weatherService = retrofit.create(WeatherService::class.java)

        loadWeather("Madrid")

        handler.postDelayed(object : Runnable {

            override fun run() {

                loadWeather("Madrid")

                handler.postDelayed(this, delayMillis.toLong())

            }

        }, delayMillis.toLong())

        return root
    }


    @RequiresApi(Build.VERSION_CODES.O)

    private fun loadWeather(city: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val response = weatherService.get5DayForecast(city, "77f77346d62a63cd3dd92fca48570090")

            withContext(Dispatchers.Main) {

                if (response.isSuccessful) {

                    val forecastResponse = response.body()

                    forecastResponse?.let {

                        val forecastItems = it.list

                        val currentDate = LocalDate.now()

                        val futureDates = (0..5).map { currentDate.plusDays(it.toLong()) }

                        val dailyTemps = mutableMapOf<String, Pair<Double, Double>>()
                        val dailyIcons = mutableMapOf<String, MutableMap<String, Int>>()

                        for (forecastItem in forecastItems) {

                            val date = LocalDate.parse(forecastItem.dt_txt.split(" ")[0])

                            if (date in futureDates) {

                                val maxTemp = forecastItem.main.temp_max

                                val minTemp = forecastItem.main.temp_min

                                val icon = forecastItem.weather[0].icon

                                val dateString = date.toString()

                                if (dailyTemps.containsKey(dateString)) {

                                    val currentMax = dailyTemps[dateString]?.first ?: Double.MIN_VALUE

                                    val currentMin = dailyTemps[dateString]?.second ?: Double.MAX_VALUE

                                    dailyTemps[dateString] = Pair(maxOf(currentMax, maxTemp), minOf(currentMin, minTemp))

                                    val iconCounts = dailyIcons[dateString] ?: mutableMapOf()

                                    iconCounts[icon] = (iconCounts[icon] ?: 0) + 1

                                    dailyIcons[dateString] = iconCounts

                                } else {

                                    dailyTemps[dateString] = Pair(maxTemp, minTemp)

                                    dailyIcons[dateString] = mutableMapOf(icon to 1)

                                }

                            }

                        }

                        val predominantIcons = mutableMapOf<String, String>()

                        dailyIcons.forEach { (date, iconCounts) ->

                            val predominantIcon = iconCounts.maxByOrNull { it.value }?.key ?: "unknown"

                            predominantIcons[date] = predominantIcon

                        }

                        var weatherInfo = ""

                        val container = binding.containerCards

                        dailyTemps.forEach { (date, temps) ->

                            val maxTemp = temps.first

                            val minTemp = temps.second

                            val predominantIcon = predominantIcons[date] ?: "unknown"

                            val cardView = layoutInflater.inflate(R.layout.item_weather_card, container, false) as CardView

                            val placeNameTextView = cardView.findViewById<TextView>(R.id.text_place_name)

                            val weatherInfoTextView = cardView.findViewById<TextView>(R.id.text_weather_info)

                            val imageIcon = cardView.findViewById<ImageView>(R.id.imagenFlonatenTemp)

                            placeNameTextView.text = date

                            weatherInfoTextView.text = "Mínima: ${minTemp.roundToInt()} ºC, Máxima : ${maxTemp.roundToInt()} ºC\n"

                            if (predominantIcon == "01d" || predominantIcon == "01n") {

                                imageIcon.setImageResource(R.drawable.uno_d)

                            } else if (predominantIcon == "02d" || predominantIcon == "02n") {

                                imageIcon.setImageResource(R.drawable.dos_d)

                            } else if (predominantIcon == "03d" || predominantIcon == "03n") {

                                imageIcon.setImageResource(R.drawable.tres)

                            } else if (predominantIcon == "04d" || predominantIcon == "04n") {

                                imageIcon.setImageResource(R.drawable.cuatro)

                            } else if (predominantIcon == "09d" || predominantIcon == "09n") {

                                imageIcon.setImageResource(R.drawable.nueve)

                            } else if (predominantIcon == "10d" || predominantIcon == "10n") {

                                imageIcon.setImageResource(R.drawable.diez_d)

                            } else if (predominantIcon == "11d" || predominantIcon == "11n") {

                                imageIcon.setImageResource(R.drawable.once)

                            } else if (predominantIcon == "13d" || predominantIcon == "13n") {

                                imageIcon.setImageResource(R.drawable.trece)

                            } else if (predominantIcon == "50d" || predominantIcon == "50n") {

                                imageIcon.setImageResource(R.drawable.cincuenta)

                            }

                            container.addView(cardView)

                        }

                    }

                }

            }

        }

    }

}
