package com.example.triptrendyapp.temperatura

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface WeatherService {

    @GET("data/2.5/weather")

    suspend fun getCurrentWeather(

        @Query("q") city: String,

        @Query("appid") apiKey: String,

        @Query("units") units: String = "metric"

    ): Response<WeatherData>

    @GET("data/2.5/forecast")

    suspend fun get5DayForecast(

        @Query("q") city: String,

        @Query("appid") apiKey: String,

        @Query("units") units: String = "metric"

    ): Response<ForecastResponse>

}