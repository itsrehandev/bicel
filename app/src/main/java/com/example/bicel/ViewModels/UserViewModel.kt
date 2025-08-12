package com.example.bicel.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.bicel.R
import com.example.bicel.databinding.FragmentMainBinding
import com.example.bicel.main
import com.example.bicel.repositories.UserRepository

class UserViewModel : ViewModel() {
    val isLoggedIn: LiveData<Boolean> = UserRepository.isLoggedIn
    val isUploadingAd = MutableLiveData<Boolean>().apply { value = false }
    val isHomeContentLoaded = MutableLiveData<Boolean>().apply { value = false }
    val isAdUploaded = MutableLiveData<Boolean>().apply { value = false }
    fun login() = UserRepository.login()
    fun logout() = UserRepository.logout()
    fun uploadingAd(){
        isUploadingAd.value = true
    }
    fun cancellingAd(){
        isUploadingAd.value = false
    }
    fun adUploaded(){
        isAdUploaded.value = true
    }
    fun homeContentUploaded(){
        isHomeContentLoaded.value = true
    }


}