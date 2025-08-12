package com.example.bicel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bicel.SharedPreferences.SharedPreferencesClass
import com.example.bicel.Utility.UtilityClass
import com.example.bicel.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var utilityClass: UtilityClass
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Variables
        utilityClass = UtilityClass()
        firebaseAuth = Firebase.auth
        firebaseFirestore = FirebaseFirestore.getInstance()
        //Ends
        binding.createAccountBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if (utilityClass.emailIsValid(email) && utilityClass.passwordIsValid(password) && utilityClass.nameIsValid(
                    binding.name.text.toString()
                )
                && utilityClass.phoneIsValid(binding.phone.text.toString())
            ) { // if everything is okay then make account
                binding.progressBar.visibility = View.VISIBLE
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                    val userNameandPhone = hashMapOf(
                        "name" to binding.name.text.toString(),
                        "phone" to binding.phone.text.toString(),
                        "email" to email,
                        "password" to password,
                        "contactList" to ArrayList<String>(),
                        "profilePicUri" to "https://firebasestorage.googleapis.com/v0/b/firstproject-d0041.appspot.com/o/login.png?alt=media&token=2cb32276-f24b-4db1-a279-60e1cd4f9ac3"
                    )

                    firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                        .set(userNameandPhone).addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()

                        }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.email.error = it.localizedMessage
                }
            } else { // if something is wrong then show error
                if (binding.name.text.toString().isEmpty() || binding.phone.text.toString()
                        .isEmpty()
                    || binding.email.text.toString().isEmpty() || binding.password.text.toString()
                        .isEmpty()
                ) { // check if any field is empty


                    if (binding.password.text.toString().isEmpty()) {
                        binding.password.error = "Please fill the required field"
                        binding.password.requestFocus()
                    } else binding.password.error = null
                    if (binding.phone.text.toString().isEmpty()) {
                        binding.phone.error = "Please fill the required field"
                        binding.phone.requestFocus()
                    } else binding.phone.error = null
                    if (binding.email.text.toString().isEmpty()) {
                        binding.email.error = "Please fill the required field"
                        binding.email.requestFocus()
                    } else binding.email.error = null
                    if (binding.name.text.toString().isEmpty()) {
                        binding.name.error = "Please fill the required field"
                        binding.name.requestFocus()
                    } else binding.name.error = null
                } else { // check if any field is invalid
                    if (!utilityClass.emailIsValid(email)) {
                        binding.email.error = "Invalid Email"
                    }
                    if (!utilityClass.passwordIsValid(password)) {
                        if (password.length < 6) binding.password.error =
                            "Password must consist of at least 6 characters"
                        if (password.length > 18) binding.password.error =
                            "Password must consist of maximum 18 characters"
                        if (!password.any { it.isDigit() }) binding.password.error =
                            "Password must contains at least one digit"
                    }
                    if (!utilityClass.nameIsValid(binding.name.text.toString())) {
                        if (binding.name.text.toString().length < 3) binding.name.error =
                            "Name must consist of at least 3 characters"
                        if (binding.name.text.toString().length > 30) binding.name.error =
                            "Name must consist of maximum 30 characters"
                        if (binding.name.text.toString().any { it.isDigit() }) binding.name.error =
                            "Name must not contains digits"
                    }
                    if (!utilityClass.phoneIsValid(binding.phone.text.toString())) {
                        binding.phone.error = "Please enter a valid phone number"
                    }
                }

            }


        }
        binding.loginNavigate.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


}
