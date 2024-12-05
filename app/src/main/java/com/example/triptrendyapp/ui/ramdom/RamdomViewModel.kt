package com.example.triptrendyapp.ui.ramdom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RamdomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Ubicación Actual"

    }

    val text: LiveData<String> = _text
}