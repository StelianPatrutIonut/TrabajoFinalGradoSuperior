package com.example.triptrendyapp.ui.pulication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PublicationModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Publicaciones"

    }

    val text: LiveData<String> = _text

}