package com.example.bicel.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log

object UserRepository {
    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn
    fun login() {
        _isLoggedIn.postValue(true) // postValue is safe from any thread
    }
    fun logout() {
        _isLoggedIn.postValue(false)
    }
    fun setLoggedIn(value: Boolean) {
        _isLoggedIn.postValue(value)
    }
}
