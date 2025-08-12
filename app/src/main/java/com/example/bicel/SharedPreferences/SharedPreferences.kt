package com.example.bicel.SharedPreferences

import android.content.Context

class SharedPreferencesClass(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    fun isFirstLoad(): Boolean {
        return sharedPreferences.getBoolean("isFirstLoad", true)
    }
    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean("isFirstLoad", isFirstTime).apply()
    }
}