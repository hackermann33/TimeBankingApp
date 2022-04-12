package it.polito.timebankingapp.ui.editprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileFragmentVM : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is a Fragment"
    }
    val text: LiveData<String> = _text
}