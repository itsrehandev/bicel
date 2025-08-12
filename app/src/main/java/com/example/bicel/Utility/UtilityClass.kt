package com.example.bicel.Utility

import android.util.Patterns

class UtilityClass {
    fun phoneIsValid(phone: String): Boolean {
        if (phone.length == 11 && Patterns.PHONE.matcher(phone)
                .matches() && phone[0] == '0' && phone[1] == '3') {
            return true
        } else return false
    }

    fun nameIsValid(name: String): Boolean {
        if (name.length in 3..30 && !name.any { it.isDigit() }) {
            return true
        } else return false
    }

    fun emailIsValid(email: String): Boolean {
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        } else return false

    }

    fun passwordIsValid(password: String): Boolean {
        if (password.length in 6..18 && password.any { it.isDigit() }) {
            return true
        } else return false
    }
}