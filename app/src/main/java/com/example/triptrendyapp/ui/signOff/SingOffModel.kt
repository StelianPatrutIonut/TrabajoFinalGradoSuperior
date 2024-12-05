package com.example.triptrendyapp.ui.signOff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SingOffModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Cerrar Sesión"

    }

    val text: LiveData<String> = _text

}