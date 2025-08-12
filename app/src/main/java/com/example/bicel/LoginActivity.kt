package com.example.bicel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.bicel.SharedPreferences.SharedPreferencesClass
import com.example.bicel.Utility.UtilityClass
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signupClass: SignupActivity
    private lateinit var sharedPreferences: SharedPreferencesClass
    private lateinit var utilityClass: UtilityClass
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        Variables
        sharedPreferences = SharedPreferencesClass(this)
        signupClass = SignupActivity()
        firebaseAuth = Firebase.auth
        utilityClass = UtilityClass()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        // Ends
        // Navigate to Signup Activity
        binding.signupNavigation.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        // Login Button listener
        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
//            Validate Email and Password
            if (utilityClass.emailIsValid(email) && utilityClass.passwordIsValid(password)) {
//                show loading
                binding.progressBar.visibility = View.VISIBLE
//                Sign In
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    sharedPreferences.setFirstTimeLaunch(false)
                    userViewModel.login()
                    finish()
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTv.text = it.localizedMessage
                }

            } else if (!utilityClass.passwordIsValid(password) || !utilityClass.emailIsValid(email)) {
                if (!utilityClass.passwordIsValid(password)) {
                    binding.password.error = "Please Enter a valid Password"
                    binding.password.requestFocus()
                }
                if (!utilityClass.emailIsValid(email)) {
                    binding.email.error = "Please Enter a valid Email"
                    binding.email.requestFocus()
                }
            }
        }
    }
}