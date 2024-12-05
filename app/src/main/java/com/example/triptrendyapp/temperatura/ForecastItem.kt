package com.example.triptrendyapp.temperatura

data class ForecastItem(
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)