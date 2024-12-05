package com.example.triptrendyapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

        value = "Perfil"

    }

    val text: LiveData<String> = _text

}