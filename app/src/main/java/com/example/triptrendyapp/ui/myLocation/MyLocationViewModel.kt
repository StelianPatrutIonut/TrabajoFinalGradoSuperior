package com.example.triptrendyapp.ui.myLocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyLocationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Mapa"

    }

    val text: LiveData<String> = _text

}