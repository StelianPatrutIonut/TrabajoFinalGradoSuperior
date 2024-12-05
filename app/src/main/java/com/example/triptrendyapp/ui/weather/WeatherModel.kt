package com.example.triptrendyapp.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WeatherModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Tiempo"

    }

    val text: LiveData<String> = _text

}